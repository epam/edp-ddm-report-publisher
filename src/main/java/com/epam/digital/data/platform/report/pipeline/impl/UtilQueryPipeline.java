package com.epam.digital.data.platform.report.pipeline.impl;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.epam.digital.data.platform.report.pipeline.AbstractPipeline;
import com.epam.digital.data.platform.report.pipeline.PipelineOrder;
import com.epam.digital.data.platform.report.service.QueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.epam.digital.data.platform.report.model.Context;
import com.epam.digital.data.platform.report.model.Query;
import com.epam.digital.data.platform.report.model.Visualization;
import com.epam.digital.data.platform.report.util.IOUtils;

@Order(PipelineOrder.UTIL_QUERY_PIPELINE)
@Component
public class UtilQueryPipeline extends AbstractPipeline {

  private final QueryService queryService;

  private final Predicate<? super File> QUERY_FILTER =
      file -> file.isDirectory() && file.getName().equals("queries");

  public UtilQueryPipeline(ObjectMapper objectMapper, QueryService queryService) {
    super(objectMapper);
    this.queryService = queryService;
  }

  public void process(List<File> files, Context context) {
    var queries = getUtilQueries(files);

    queryService.archive(queries);
    var saved = queryService.save(visualizationsByQuery(queries), context);
    queryService.publish(saved);
    queryService.execute(saved);

    context.setMappedIds(mapToNewIds(saved, queries));
  }

  public boolean isApplicable(List<File> files) {
    return files.stream().anyMatch(QUERY_FILTER);
  }

  private List<Query> getUtilQueries(List<File> files) {
    var queries = getQueries(files);
    var utilIds = getUtilQueryIds(queries);

    return queries.stream()
        .filter(query -> utilIds.contains(query.getId()))
        .collect(toList());
  }

  private Set<Integer> getUtilQueryIds(List<Query> queries) {
    var utilIds = new HashSet<Integer>();

    for (var query : queries) {
      var params = (List<Map<String, Object>>) query.getOptions().get("parameters");

      for (var parameter : params) {
        var utilId = parameter.get("queryId");
        if (utilId != null) {
          utilIds.add((Integer) utilId);
        }
      }
    }
    return utilIds;
  }

  private Map<Integer, Integer> mapToNewIds(Set<Query> queries, List<Query> oldQueries) {
    var mappedIds = new HashMap<Integer, Integer>();

    var old = oldQueries.stream()
        .collect(Collectors.toMap(Query::getQuery, Query::getId));

    queries.forEach(query -> {
      var oldId = old.get(query.getQuery());
      var newId = query.getId();
      mappedIds.put(oldId, newId);
    });

    return mappedIds;
  }

  private Map<Query, List<Visualization>> visualizationsByQuery(List<Query> queries) {
    var queryToVisualizations = new HashMap<Query, List<Visualization>>();
    queries.forEach(query -> queryToVisualizations.put(query, query.getVisualizations()));
    return queryToVisualizations;
  }

  private List<Query> getQueries(List<File> files) {
    return files.stream()
        .filter(QUERY_FILTER)
        .map(IOUtils::getFileList)
        .flatMap(Arrays::stream)
        .filter(file -> file.getName().endsWith(".json"))
        .map(file -> readPage(file, Query.class))
        .findFirst()
        .orElse(emptyList());
  }
}
