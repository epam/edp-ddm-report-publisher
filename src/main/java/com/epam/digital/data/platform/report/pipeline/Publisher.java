package com.epam.digital.data.platform.report.pipeline;

import static com.epam.digital.data.platform.report.util.ResponseHandler.handleResponse;

import com.epam.digital.data.platform.report.service.QueryService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.epam.digital.data.platform.report.client.DashboardClient;
import com.epam.digital.data.platform.report.model.Context;
import com.epam.digital.data.platform.report.model.Dashboard;
import com.epam.digital.data.platform.report.model.Query;
import com.epam.digital.data.platform.report.model.Visualization;
import com.epam.digital.data.platform.report.model.Widget;
import com.epam.digital.data.platform.report.service.WidgetService;

@Component
public class Publisher {

    private final DashboardClient dashboardClient;
    private final WidgetService widgetService;
    private final QueryService queryService;

    private final Logger log = LoggerFactory.getLogger(Publisher.class);

    public Publisher(DashboardClient dashboardClient, WidgetService widgetService,
        QueryService queryService) {
        this.dashboardClient = dashboardClient;
        this.widgetService = widgetService;
        this.queryService = queryService;
    }

    public void publish(Dashboard dashboard, Context context) {
        Dashboard created = saveDashboard(dashboard);
        dashboard.setId(created.getId());
        log.info("Created new dashboard {} with id {}", created.getName(), created.getId());

        processQueries(visualizationsByQuery(dashboard), context);

        widgetService.save(dashboard);
        publish(dashboard);
    }

    private void publish(Dashboard dashboard) {
        Dashboard dashboardStub = new Dashboard();
        dashboardClient.updateDashboard(dashboard.getId(), dashboardStub);
    }

    private Dashboard saveDashboard(Dashboard dashboard) {
        return handleResponse(dashboardClient.createDashboard(dashboard));
    }

    private Set<Query> processQueries(Map<Query, List<Visualization>> queries, Context context) {
        var saved = queryService.save(queries, context);
        queryService.publish(saved);
        return saved;
    }

    private Map<Query, List<Visualization>> visualizationsByQuery(Dashboard dashboard) {
        return dashboard.getWidgets().stream()
            .map(Widget::getVisualization)
            .collect(Collectors.groupingBy(Visualization::getQuery));
    }
}
