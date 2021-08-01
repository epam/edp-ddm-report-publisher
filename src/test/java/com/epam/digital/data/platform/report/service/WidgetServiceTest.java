package com.epam.digital.data.platform.report.service;

import static java.util.Set.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.WidgetClient;
import com.epam.digital.data.platform.report.model.Dashboard;
import com.epam.digital.data.platform.report.model.Visualization;
import com.epam.digital.data.platform.report.model.Widget;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = {WidgetService.class})
public class WidgetServiceTest {

    @MockBean
    private WidgetClient widgetClient;
    @Autowired
    private WidgetService widgetService;

    @Test
    void shouldSaveWidgets() {
        when(widgetClient.createWidget(any())).thenReturn(mockResponse(Widget.class));

        widgetService.save(mockDashboard());

        verify(widgetClient).createWidget(any());
    }

    private <T> ResponseEntity<T> mockResponse(Class<T> clazz) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Dashboard mockDashboard() {
        var dashboard = new Dashboard();
        var widget = new Widget();
        var visualization = new Visualization();

        dashboard.setId(1);
        visualization.setId(1);

        widget.setVisualization(visualization);
        dashboard.setWidgets(of(widget));

        return dashboard;
    }
}
