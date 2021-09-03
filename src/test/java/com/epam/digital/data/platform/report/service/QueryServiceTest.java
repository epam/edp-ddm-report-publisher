package com.epam.digital.data.platform.report.service;

import static com.epam.digital.data.platform.report.util.TestUtils.queries;
import static com.epam.digital.data.platform.report.util.TestUtils.query;
import static com.epam.digital.data.platform.report.util.TestUtils.visualization;
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
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class QueryServiceTest {

    @Mock
    private QueryClient queryClient;
    @Mock
    private VisualizationService visualizationService;

    private QueryService instance;

    @BeforeEach
    void init() {
        instance = new QueryService(queryClient, visualizationService);
    }

    @Test
    void shouldSaveQueries() {
        when(queryClient.postQuery(any())).thenReturn(mockQueryResponse());

        instance.save(mockQueryWithOneVisualization(visualization("TABLE")), TestUtils.context());

        verify(queryClient).postQuery(any());
        verify(visualizationService).save(any());
    }

    @Test
    void shouldPublishQueries() {
        when(queryClient.updateQuery(anyInt(), any())).thenReturn(mockResponse(Query.class));

        instance.publish(queries("first", "second"));

        verify(queryClient, times(2)).updateQuery(anyInt(), any());
    }

    @Test
    void shouldExecuteQueries() {
        when(queryClient.executeQuery(anyInt())).thenReturn(mockResponse(String.class));

        instance.execute(queries("first", "second"));

        verify(queryClient, times(2)).executeQuery(anyInt());
    }

    @Test
    void shouldArchiveQueries() {
        when(queryClient.archiveQuery(anyInt())).thenReturn(mockResponse(Void.class));
        when(queryClient.getQueries(any())).thenReturn(mockPageResponse());

        instance.archive(new ArrayList<>(queries("first", "second")));

        verify(queryClient, times(2)).getQueries(any());
        verify(queryClient, times(2)).archiveQuery(anyInt());
    }

    private <T> ResponseEntity<T> mockResponse(Class<T> clazz) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<Query> mockQueryResponse() {
        return new ResponseEntity<>(query("query name"), HttpStatus.OK);
    }

    private ResponseEntity<Page<Query>> mockPageResponse() {
        var page = new Page<Query>();
        var query = new Query();
        query.setId(1);
        page.setResults(List.of(query));

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    private Map<Query, List<Visualization>> mockQueryWithOneVisualization(Visualization visualization) {
        var query = query("query name");
        var visualizations = new ArrayList<Visualization>();

        visualizations.add(visualization);
        query.setVisualizations(visualizations);

        return Map.of(query, visualizations);
    }
}
