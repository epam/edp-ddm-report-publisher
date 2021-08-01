package com.epam.digital.data.platform.report.pipeline;

import static java.util.stream.Collectors.toList;
import static com.epam.digital.data.platform.report.util.ResponseHandler.handleResponse;

import com.epam.digital.data.platform.report.service.QueryService;
import java.util.List;

import org.springframework.stereotype.Component;
import com.epam.digital.data.platform.report.client.DashboardClient;
import com.epam.digital.data.platform.report.model.Dashboard;
import com.epam.digital.data.platform.report.model.Query;
import com.epam.digital.data.platform.report.model.Visualization;
import com.epam.digital.data.platform.report.model.Widget;

@Component
public class Archiver {

    private final DashboardClient dashboardClient;
    private final QueryService queryService;

    public Archiver(DashboardClient dashboardClient, QueryService queryService) {
        this.dashboardClient = dashboardClient;
        this.queryService = queryService;
    }

    public void archive(Dashboard dashboard) {
        archiveDashboard(dashboard.getName());
        archiveQueries(getQueries(dashboard));
    }

    private void archiveDashboard(String name) {
        var response = handleResponse(dashboardClient.getDashboards(name));

        response.getResults().stream()
            .map(Dashboard::getSlug)
            .forEach(dashboardClient::archiveDashboard);
    }

    private void archiveQueries(List<Query> queries) {
        queryService.archive(queries);
    }

    private List<Query> getQueries(Dashboard dashboard) {
        return dashboard.getWidgets().stream()
            .map(Widget::getVisualization)
            .map(Visualization::getQuery)
            .collect(toList());
    }
}
