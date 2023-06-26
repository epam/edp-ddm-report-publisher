/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.report.service;

import static com.epam.digital.data.platform.report.util.ResponseHandler.handleResponse;
import static java.util.stream.Collectors.toSet;

import com.epam.digital.data.platform.report.client.QueryClient;
import com.epam.digital.data.platform.report.model.Context;
import com.epam.digital.data.platform.report.model.Page;
import com.epam.digital.data.platform.report.model.Query;
import com.epam.digital.data.platform.report.model.Visualization;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QueryService {
    public static final String MISSING_PARAMETER_ON_QUERY_PREPOPULATION_ERROR_PREFIX = "Missing parameter value for:";

    private final QueryClient queryClient;
    private final VisualizationService visualizationService;

    private final Logger log = LoggerFactory.getLogger(QueryService.class);

    public QueryService(QueryClient queryClient,
        VisualizationService visualizationService) {
        this.queryClient = queryClient;
        this.visualizationService = visualizationService;
    }

    public Set<Query> save(Map<Query, List<Visualization>> queries, Context context) {
        var saved = saveQueries(queries, context);
        visualizationService.save(saved);
        return saved.keySet();
    }

    public void publish(Set<Query> queries) {
        Query queryStub = new Query();
        queries.forEach(query -> handleResponse(queryClient.updateQuery(query.getId(), queryStub)));
    }

    public void execute(Set<Query> queries) {
        queries.forEach(query -> {
            try {
                queryClient.executeQuery(query.getId());
                log.info("Executed query {}", query.getName());
            } catch (FeignException e) {
                if(!e.getMessage().contains(MISSING_PARAMETER_ON_QUERY_PREPOPULATION_ERROR_PREFIX)) {
                    throw e;
                }
                log.warn("Couldn't execute query, error: {}", e.getMessage());
            }
        });
    }

    public void archive(List<Query> queries) {
        for (String name : getQueryNames(queries)) {
            log.info("Processing query '{}'", name);
            int page = 0;
            List<Query> foundQueries = new ArrayList<>();
            Page<Query> response;
            do {
                response = handleResponse(queryClient.getQueries(name, ++page));
                foundQueries.addAll(response.getResults());
            } while (page * response.getPageSize() < response.getCount());

            log.info("Found {} queries: {}", foundQueries.size(),
                foundQueries.stream()
                    .map(q -> "(id = " + q.getId() + ", name = '" + q.getName() + "')")
                    .collect(Collectors.toList()));

            foundQueries.stream()
                .filter(query -> query.getName().equals(name))
                .map(query -> {
                    log.info("Archiving query: id = {}, name = '{}'", query.getId(), name);
                    return query.getId();
                })
                .forEach(q -> handleResponse(queryClient.archiveQuery(q)));
        }
    }

    private Set<String> getQueryNames(List<Query> queries) {
        return queries.stream()
            .map(Query::getName)
            .collect(toSet());
    }

    private Map<Query, List<Visualization>> saveQueries(
        Map<Query, List<Visualization>> queryToVisualizations, Context context) {
        Map<Query, List<Visualization>> created = new HashMap<>();

        for (Query query : queryToVisualizations.keySet()) {
            log.info("Publishing query '{}'", query.getName());
            prepareQuery(query, context);
            Query published = handleResponse(queryClient.postQuery(query));
            log.info("Query was published with id = {}", published.getId());
            created.put(published, queryToVisualizations.get(query));
        }

        return created;
    }

    private void prepareQuery(Query query, Context context) {
        modifyOptions(query, context);
        query.setDataSourceId(context.getDataSourceId());
    }

    private void modifyOptions(Query query, Context context) {
        var parameters = (List<Map<String, Object>>) query.getOptions().get("parameters");

        if (parameters.isEmpty()) {
            return;
        }

        for (var parameter : parameters) {
            if (parameter.get("type").equals("query")) {
                Integer old = (Integer) parameter.get("queryId");
                parameter.replace("queryId", context.getMappedIds().get(old));
            }
            parameter.remove("parentQueryId");
        }
    }
}
