package com.epam.digital.data.platform.report.pipeline.impl;

import static com.epam.digital.data.platform.report.util.TestUtils.mockContext;
import static java.util.List.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.report.config.TestConfig;
import com.epam.digital.data.platform.report.service.SnippetService;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.util.ResourceUtils;

@SpringBootTest(classes = {SnippetPipeline.class})
@Import(TestConfig.class)
public class SnippetPipelineTest {

  @MockBean
  private SnippetService service;
  @Autowired
  private SnippetPipeline pipeline;

  @Test
  void shouldSuccessfullyPublishSnippets() throws IOException {
    var dir = ResourceUtils.getFile("classpath:pipeline/admin/snippets");
    var files = of(dir);

    pipeline.process(files, mockContext());

    verify(service).publish(any());
  }
}
