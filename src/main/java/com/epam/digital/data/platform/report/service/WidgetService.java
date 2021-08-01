package com.epam.digital.data.platform.report.service;

import org.springframework.stereotype.Service;
import com.epam.digital.data.platform.report.client.WidgetClient;
import com.epam.digital.data.platform.report.model.Dashboard;
import com.epam.digital.data.platform.report.model.Widget;

@Service
public class WidgetService {
    private final WidgetClient widgetClient;

    public WidgetService(WidgetClient widgetClient) {
        this.widgetClient = widgetClient;
    }

    public void save(Dashboard dashboard) {
        for (Widget widget : dashboard.getWidgets()) {
            widget.setVisualizationId(widget.getVisualization().getId());
            widget.setDashboardId(dashboard.getId());
            widgetClient.createWidget(widget);
        }
    }
}
