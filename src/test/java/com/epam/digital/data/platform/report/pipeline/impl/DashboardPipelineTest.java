package com.epam.digital.data.platform.report.pipeline.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static java.util.List.of;
import static com.epam.digital.data.platform.report.util.TestUtils.context;

import com.epam.digital.data.platform.report.pipeline.Archiver;
import com.epam.digital.data.platform.report.pipeline.Publisher;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.util.ResourceUtils;
import com.epam.digital.data.platform.report.config.TestConfig;

@SpringBootTest(classes = {DashboardPipeline.class})
@Import(TestConfig.class)
public class DashboardPipelineTest {

    @MockBean
    private Publisher publisher;
    @MockBean
    private Archiver archiver;
    @Autowired
    private DashboardPipeline dashboardPipeline;

    @Test
    void shouldSuccessfullyArchiveAndPublishDashboard() throws IOException {
        var dashboard = ResourceUtils.getFile("classpath:pipeline/admin/correct_dashboard.json");
        var files = of(dashboard);

        dashboardPipeline.process(files, context());

        verify(archiver).archive(any());
        verify(publisher).publish(any(), any());
    }

    @Test
    void shouldNotArchiveAndPublishDashboardIfNoneFound() throws FileNotFoundException {
        var dashboard = ResourceUtils.getFile("classpath:pipeline");
        var files = of(dashboard);

        dashboardPipeline.process(files, context());

        verify(archiver, times(0)).archive(any());
        verify(publisher, times(0)).publish(any(), any());
    }
}
