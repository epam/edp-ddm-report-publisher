package com.epam.digital.data.platform.report.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.DashboardClient;
import com.epam.digital.data.platform.report.model.Dashboard;
import com.epam.digital.data.platform.report.model.Query;
import com.epam.digital.data.platform.report.model.Visualization;
import com.epam.digital.data.platform.report.model.Widget;
import com.epam.digital.data.platform.report.service.QueryService;
import com.epam.digital.data.platform.report.service.WidgetService;
import com.epam.digital.data.platform.report.util.TestUtils;
import java.util.HashSet;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Publisher.class})
public class PublisherTest {

    @MockBean
    private DashboardClient dashboardClient;
    @MockBean
    private WidgetService widgetService;
    @MockBean
    private QueryService queryService;
    @Autowired
    private Publisher publisher;

    @Test
    void shouldPublishDashboard() {
        var input = mockDashboard();
        var created = mockResponse();
        when(dashboardClient.createDashboard(any())).thenReturn(created);
        when(queryService.save(any(), any())).thenReturn(new HashSet<>());

        publisher.publish(input, TestUtils.mockContext());

        Assertions.assertThat(input.getId()).isEqualTo(created.getBody().getId());
        verify(dashboardClient).createDashboard(input);
        verify(queryService).save(any(), any());
        verify(queryService).publish(any());
        verify(widgetService).save(any());
        verify(dashboardClient).updateDashboard(anyInt(), any());
    }

    private Dashboard mockDashboard() {
        var dashboard = new Dashboard();
        var widget = new Widget();
        var visualization = new Visualization();
        var query = new Query();

        query.setName("stub_query");
        visualization.setQuery(query);
        widget.setVisualization(visualization);
        dashboard.setWidgets(Set.of(widget));

        return dashboard;
    }

    private ResponseEntity<Dashboard> mockResponse() {
        var created = new Dashboard();
        created.setId(999);
        return new ResponseEntity<>(created, HttpStatus.OK);
    }
}
