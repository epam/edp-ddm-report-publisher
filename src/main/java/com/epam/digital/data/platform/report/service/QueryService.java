package com.epam.digital.data.platform.report.service;

import static java.util.stream.Collectors.toSet;
import static com.epam.digital.data.platform.report.util.ResponseHandler.handleResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.epam.digital.data.platform.report.client.QueryClient;
import com.epam.digital.data.platform.report.model.Context;
import com.epam.digital.data.platform.report.model.Query;
import com.epam.digital.data.platform.report.model.Visualization;

@Service
public class QueryService {

    private final QueryClient queryClient;
    private final VisualizationService visualizationService;

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
        queries.forEach(query -> queryClient.updateQuery(query.getId(), queryStub));
    }

    public void execute(Set<Query> queries) {
        queries.forEach(query -> queryClient.executeQuery(query.getId()));
    }

    public void archive(List<Query> queries) {
        for (String name : getQueryNames(queries)) {
            var response = handleResponse(queryClient.getQueries(name));

            response.getResults().stream()
                .map(Query::getId)
                .forEach(queryClient::archiveQuery);
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
            prepareQuery(query, context);
            Query published = handleResponse(queryClient.postQuery(query));
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
