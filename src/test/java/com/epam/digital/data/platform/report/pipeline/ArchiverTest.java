package com.epam.digital.data.platform.report.pipeline;

import static com.epam.digital.data.platform.report.util.TestUtils.dashboard;
import static java.util.List.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.DashboardClient;
import com.epam.digital.data.platform.report.model.Dashboard;
import com.epam.digital.data.platform.report.model.Page;
import com.epam.digital.data.platform.report.service.QueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class ArchiverTest {

    @Mock
    DashboardClient dashboardClient;

    @Mock
    QueryService queryService;

    Archiver instance;

    @BeforeEach
    void init() {
        instance = new Archiver(dashboardClient, queryService);
    }

    @Test
    void shouldSuccessfullyArchiveDashboardAndQueries() {
        ResponseEntity<Page<Dashboard>> dashboardResponse = mockDashboardResponse();

        when(dashboardClient.getDashboards(any())).thenReturn(dashboardResponse);

        instance.archive(dashboard("stub"));

        verify(dashboardClient).getDashboards("stub");
        verify(dashboardClient).archiveDashboard("stub");
        verify(queryService).archive(any());
    }

    private ResponseEntity<Page<Dashboard>> mockDashboardResponse() {
        Page<Dashboard> page = new Page<>();
        page.setResults(of(dashboard("stub")));

        return ResponseEntity.status(200)
            .body(page);
    }
}
