package com.epam.digital.data.platform.report;

import static com.epam.digital.data.platform.report.util.TestUtils.dataSources;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.DataSourceClient;
import com.epam.digital.data.platform.report.config.properties.AppProperties;
import com.epam.digital.data.platform.report.model.DataSource;
import com.epam.digital.data.platform.report.pipeline.AbstractPipeline;
import com.epam.digital.data.platform.report.service.ExcerptService;
import com.epam.digital.data.platform.report.service.RoleService;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.ApplicationArguments;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportPublisherApplicationTest {

  @Mock
  private ApplicationArguments args;
  @Mock
  private DataSourceClient dataSourceClient;
  @Mock
  private AbstractPipeline skippedPipeline;
  @Mock
  private AbstractPipeline appliedPipeline;
  @Mock
  private ExcerptService excerptService;
  @Mock
  private RoleService roleService;

  private AppProperties appProperties;
  private String reportsDirectoryName;
  private String excerptsDirectoryName;

  private ReportPublisherApplication reportPublisherApplication;

  @BeforeEach
  void init() throws FileNotFoundException {
    reportsDirectoryName = ResourceUtils.getFile("classpath:pipeline").getAbsolutePath();
    excerptsDirectoryName = ResourceUtils.getFile("classpath:excerpts").getAbsolutePath();

    var pipelines = new ArrayList<AbstractPipeline>();
    pipelines.add(skippedPipeline);
    pipelines.add(appliedPipeline);

    appProperties = new AppProperties();
    appProperties.setReportsDirectoryName(reportsDirectoryName);

    reportPublisherApplication = new ReportPublisherApplication(
        appProperties, dataSourceClient, pipelines, excerptService, roleService, "registry");
  }

  @Test
  void shouldApplyOnlyOnePipeline() {
    when(args.containsOption("reports")).thenReturn(true);
    when(skippedPipeline.isApplicable(any())).thenReturn(false);
    when(appliedPipeline.isApplicable(any())).thenReturn(true);
    when(dataSourceClient.getDataSources()).thenReturn(mockResponse("registry"));

    reportPublisherApplication.run(args);

    verify(skippedPipeline).isApplicable(any());
    verify(skippedPipeline, never()).process(any(), any());
    verify(appliedPipeline).isApplicable(any());
    verify(appliedPipeline).process(any(), any());
  }

  @Test
  void shouldCallExcerptServiceForEachFolder() {
    when(args.containsOption("excerpts")).thenReturn(true);
    appProperties.setExcerptsDirectoryName(excerptsDirectoryName);

    reportPublisherApplication.run(args);

    verify(excerptService, times(4)).loadDir(any());
  }

  @Test
  void shouldReturnEmptyListOfFilesWhenExcerptFolderAbsent() {
    when(args.containsOption("excerpts")).thenReturn(true);
    appProperties.setExcerptsDirectoryName(excerptsDirectoryName + "a");

    reportPublisherApplication.run(args);

    verify(excerptService, never()).loadDir(any());
  }

  @Test
  void createAdminServiceGroupWhenAdminOptionGiven() {
    when(args.containsOption("admin")).thenReturn(true);

    reportPublisherApplication.run(args);

    verify(roleService).createAdminRegistry();
  }

  @Test
  void createAuditorServiceGroupWhenAuditorOptionGiven() {
    when(args.containsOption("auditor")).thenReturn(true);

    reportPublisherApplication.run(args);

    verify(roleService).createAuditorGroup();
  }

  private ResponseEntity<List<DataSource>> mockResponse(String name) {
    return new ResponseEntity<>(dataSources(name), HttpStatus.OK);
  }
}

