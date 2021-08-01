package com.epam.digital.data.platform.report.pipeline;

import static java.util.List.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.DashboardClient;
import com.epam.digital.data.platform.report.model.Dashboard;
import com.epam.digital.data.platform.report.model.Page;
import com.epam.digital.data.platform.report.model.Query;
import com.epam.digital.data.platform.report.model.Visualization;
import com.epam.digital.data.platform.report.model.Widget;
import com.epam.digital.data.platform.report.service.QueryService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = {Archiver.class})
public class ArchiverTest {

    @MockBean
    private DashboardClient dashboardClient;
    @MockBean
    private QueryService queryService;
    @Autowired
    private Archiver archiver;

    @Test
    void shouldSuccessfullyArchiveDashboardAndQueries() {
        ResponseEntity<Page<Dashboard>> dashboardResponse = mockDashboardResponse();

        when(dashboardClient.getDashboards(any())).thenReturn(dashboardResponse);

        archiver.archive(buildDashboardStub());

        verify(dashboardClient).getDashboards("stub");
        verify(dashboardClient).archiveDashboard("stub");
        verify(queryService).archive(any());
    }

    private ResponseEntity<Page<Dashboard>> mockDashboardResponse() {
        Page<Dashboard> page = new Page<>();
        page.setResults(of(buildDashboardStub()));

        return ResponseEntity.status(200)
            .body(page);
    }

    private Dashboard buildDashboardStub() {
        Dashboard dashboard = new Dashboard();
        Widget widget = new Widget();
        Visualization visualization = new Visualization();
        Query query = new Query();

        query.setName("stub_query");
        visualization.setQuery(query);
        widget.setVisualization(visualization);
        dashboard.setWidgets(Set.of(widget));
        dashboard.setSlug("stub");
        dashboard.setName("stub");

        return dashboard;
    }
}
