package com.epam.digital.data.platform.report.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.QueryClient;
import com.epam.digital.data.platform.report.model.Page;
import com.epam.digital.data.platform.report.model.Query;
import com.epam.digital.data.platform.report.model.Visualization;
import com.epam.digital.data.platform.report.util.TestUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = {QueryService.class})
public class QueryServiceTest {

    @MockBean
    private QueryClient queryClient;
    @MockBean
    private VisualizationService visualizationService;
    @Autowired
    private QueryService queryService;

    @Test
    void shouldSaveQueries() {
        when(queryClient.postQuery(any())).thenReturn(mockQueryResponse());

        queryService.save(mockQueryWithOneVisualization(tableVisualization()), TestUtils.mockContext());

        verify(queryClient).postQuery(any());
        verify(visualizationService).save(any());
    }

    @Test
    void shouldPublishQueries() {
        when(queryClient.updateQuery(anyInt(), any())).thenReturn(mockResponse(Query.class));

        queryService.publish(getMockQueries());

        verify(queryClient, times(2)).updateQuery(anyInt(), any());
    }

    @Test
    void shouldExecuteQueries() {
        when(queryClient.executeQuery(anyInt())).thenReturn(mockResponse(String.class));

        queryService.execute(getMockQueries());

        verify(queryClient, times(2)).executeQuery(anyInt());
    }

    @Test
    void shouldArchiveQueries() {
        when(queryClient.archiveQuery(anyInt())).thenReturn(mockResponse(Void.class));
        when(queryClient.getQueries(any())).thenReturn(mockPageResponse());

        queryService.archive(new ArrayList<>(getMockQueries()));

        verify(queryClient, times(2)).getQueries(any());
        verify(queryClient, times(2)).archiveQuery(anyInt());
    }

    private <T> ResponseEntity<T> mockResponse(Class<T> clazz) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<Query> mockQueryResponse() {
        return new ResponseEntity<>(mockQuery(), HttpStatus.OK);
    }

    private ResponseEntity<Page<Query>> mockPageResponse() {
        var page = new Page<Query>();
        var query = new Query();
        query.setId(1);
        page.setResults(List.of(query));

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    private Set<Query> getMockQueries() {
        var first = new Query();
        first.setId(1);
        first.setName("first");

        var second = new Query();
        second.setId(2);
        second.setName("second");

        return Set.of(first, second);
    }

    private Map<Query, List<Visualization>> mockQueryWithOneVisualization(Visualization visualization) {
        var query = mockQuery();
        var visualizations = new ArrayList<Visualization>();

        visualizations.add(visualization);
        query.setVisualizations(visualizations);

        return Map.of(query, visualizations);
    }

    private Query mockQuery() {
        var query = new Query();
        query.setId(1);
        query.setOptions(options());
        return query;
    }

    private Map<String, Object> options() {
        var options = new HashMap<String, Object>();
        options.put("parameters", List.of(parameters()));
        return options;
    }

    private Map<String, Object> parameters() {
        var parameters = new HashMap<String, Object>();
        parameters.put("type", "query");
        parameters.put("queryId", 0);
        parameters.put("parentQueryId", 0);
        return parameters;
    }

    private Visualization tableVisualization() {
        var visualization = new Visualization();
        visualization.setId(1);
        visualization.setType("TABLE");
        return visualization;
    }
}
