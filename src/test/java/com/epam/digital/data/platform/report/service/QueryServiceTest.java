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
        when(queryClient.getQueries(any(), anyInt())).thenReturn(mockPageResponse());

        instance.archive(new ArrayList<>(queries("first", "second")));

        verify(queryClient, times(2)).getQueries(any(), anyInt());
        verify(queryClient, times(2)).archiveQuery(anyInt());
    }

    @Test
    void shouldCollectQueriesFromThreePages() {
        when(queryClient.archiveQuery(anyInt())).thenReturn(mockResponse(Void.class));
        when(queryClient.getQueries("first", 1)).thenReturn(mockPageResponse("first-abc"));
        when(queryClient.getQueries("first", 2)).thenReturn(mockPageResponse("first"));
        when(queryClient.getQueries("first", 3)).thenReturn(mockPageResponse("first"));

        instance.archive(new ArrayList<>(queries("first")));

        verify(queryClient, times(3)).getQueries(any(), anyInt());
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
        page.setCount(2);
        page.setPageSize(25);
        var query1 = new Query();
        query1.setId(1);
        query1.setName("first");
        var query2 = new Query();
        query2.setId(1);
        query2.setName("second");
        page.setResults(List.of(query1, query2));
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    private ResponseEntity<Page<Query>> mockPageResponse(String name) {
        var page = new Page<Query>();
        page.setCount(3);
        page.setPageSize(1);
        var query = new Query();
        query.setId(1);
        query.setName(name);
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
