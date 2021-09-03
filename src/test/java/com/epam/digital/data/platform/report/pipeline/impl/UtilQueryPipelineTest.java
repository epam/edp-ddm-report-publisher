package com.epam.digital.data.platform.report.pipeline.impl;

import static java.util.List.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static com.epam.digital.data.platform.report.util.TestUtils.context;

import com.epam.digital.data.platform.report.service.QueryService;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.util.ResourceUtils;
import com.epam.digital.data.platform.report.config.TestConfig;

@SpringBootTest(classes = {UtilQueryPipeline.class})
@Import(TestConfig.class)
public class UtilQueryPipelineTest {

    @MockBean
    private QueryService queryService;
    @Autowired
    private UtilQueryPipeline utilQueryPipeline;

    @Test
    void shouldSuccessfullyArchiveAndPublishDashboard() throws IOException {
        var dir = ResourceUtils.getFile("classpath:pipeline/admin/queries");
        var files = of(dir);

        utilQueryPipeline.process(files, context());

        verify(queryService).archive(any());
        verify(queryService).save(any(), any());
        verify(queryService).publish(any());
        verify(queryService).execute(any());
    }
}
