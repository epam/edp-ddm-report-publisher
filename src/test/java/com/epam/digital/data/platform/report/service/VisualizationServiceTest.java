package com.epam.digital.data.platform.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.VisualizationClient;
import com.epam.digital.data.platform.report.model.Query;
import com.epam.digital.data.platform.report.model.Visualization;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = {VisualizationService.class})
public class VisualizationServiceTest {

    @MockBean
    private VisualizationClient visualizationClient;
    @Autowired
    private VisualizationService visualizationService;

    @Test
    void shouldUpdateDefaultVisualization() {
        when(visualizationClient.updateVisualization(anyInt(), any())).thenReturn(mockResponse());
        var input = mockQueryWithOneVisualization(tableVisualization());

        visualizationService.save(input);

        var updatedVisualization = input.values().stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        Assertions.assertThat(updatedVisualization.isEmpty()).isEqualTo(true);
        verify(visualizationClient).updateVisualization(anyInt(), any());
        verify(visualizationClient, times(0)).createVisualization(any());
    }

    @Test
    void shouldUpdateDefaultVisualizationAndCreateNewOne() {
        when(visualizationClient.updateVisualization(anyInt(), any())).thenReturn(mockResponse());
        when(visualizationClient.createVisualization(any())).thenReturn(mockResponse());
        var input = mockQueryWithTwoVisualizations();

        visualizationService.save(input);

        var updatedVisualization = input.values().stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        Assertions.assertThat(updatedVisualization.size()).isEqualTo(1);
        Assertions.assertThat(updatedVisualization.get(0).getId()).isEqualTo(2);
        verify(visualizationClient).updateVisualization(anyInt(), any());
        verify(visualizationClient).createVisualization(any());
    }

    @Test
    void shouldNotUpdateDefaultVisualizationIfNoTableVisualizations() {
        when(visualizationClient.createVisualization(any())).thenReturn(mockResponse());
        var input = mockQueryWithOneVisualization(otherVisualization());

        visualizationService.save(input);

        var updatedVisualization = input.values().stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        Assertions.assertThat(updatedVisualization.size()).isEqualTo(1);
        Assertions.assertThat(updatedVisualization.get(0).getId()).isEqualTo(2);
        verify(visualizationClient, times(0)).updateVisualization(anyInt(), any());
        verify(visualizationClient).createVisualization(any());
    }

    private Map<Query, List<Visualization>> mockQueryWithOneVisualization(Visualization visualization) {
        var query = mockQuery();
        var visualizations = new ArrayList<Visualization>();

        visualizations.add(visualization);
        query.setVisualizations(visualizations);

        return Map.of(query, visualizations);
    }

    private Map<Query, List<Visualization>> mockQueryWithTwoVisualizations() {
        var query = mockQuery();
        var first = tableVisualization();
        var second = new Visualization();
        var visualizations = new ArrayList<Visualization>();

        visualizations.add(first);
        visualizations.add(second);
        query.setVisualizations(visualizations);
        second.setId(3);

        return Map.of(query, visualizations);
    }

    private Query mockQuery() {
        var query = new Query();
        query.setId(1);
        return query;
    }

    private Visualization tableVisualization() {
        var visualization = new Visualization();
        visualization.setId(1);
        visualization.setType("TABLE");
        return visualization;
    }

    private Visualization otherVisualization() {
        var visualization = new Visualization();
        visualization.setId(1);
        visualization.setType("OTHER");
        return visualization;
    }

    private ResponseEntity<Visualization> mockResponse() {
        var created = new Visualization();
        created.setId(2);
        return new ResponseEntity<>(created, HttpStatus.OK);
    }
}
