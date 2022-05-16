/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.report;

import static com.epam.digital.data.platform.report.util.TestUtils.dataSources;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.DataSourceClient;
import com.epam.digital.data.platform.report.config.properties.AppProperties;
import com.epam.digital.data.platform.report.exception.NoFilesFoundException;
import com.epam.digital.data.platform.report.model.DataSource;
import com.epam.digital.data.platform.report.pipeline.AbstractPipeline;
import com.epam.digital.data.platform.report.service.ExcerptCsvService;
import com.epam.digital.data.platform.report.service.ExcerptDocxService;
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
  private ExcerptDocxService excerptDocxService;
  @Mock
  private ExcerptCsvService excerptCsvService;
  @Mock
  private RoleService roleService;

  private AppProperties appProperties;
  private String reportsDirectoryName;
  private String excerptsDirectoryName;
  private String excerptsDocxDirectoryName;
  private String excerptsCsvDirectoryName;

  private ReportPublisherApplication reportPublisherApplication;

  @BeforeEach
  void init() throws FileNotFoundException {
    reportsDirectoryName = ResourceUtils.getFile("classpath:pipeline").getAbsolutePath();
    excerptsDirectoryName = ResourceUtils.getFile("classpath:excerpts").getAbsolutePath();
    excerptsDocxDirectoryName = ResourceUtils.getFile("classpath:excerpts-docx").getAbsolutePath();
    excerptsCsvDirectoryName = ResourceUtils.getFile("classpath:excerpts-csv").getAbsolutePath();

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
  void shouldCallExcerptDocxServiceOnce() {
    when(args.containsOption("excerpts-docx")).thenReturn(true);
    reportPublisherApplication.setExcerptDocxService(excerptDocxService);
    appProperties.setExcerptsDocxDirectoryName(excerptsDocxDirectoryName);

    reportPublisherApplication.run(args);

    verify(excerptDocxService, times(1)).processFile(any());
  }

  @Test
  void shouldNotProcessIfIncorrectDocxFolder() {
    when(args.containsOption("excerpts-docx")).thenReturn(true);
    appProperties.setExcerptsDocxDirectoryName(excerptsDocxDirectoryName + "a");

    reportPublisherApplication.run(args);

    verify(excerptDocxService, never()).processFile(any());
  }

  @Test
  void shouldCallExcerptCsvServiceOnce() {
    when(args.containsOption("excerpts-csv")).thenReturn(true);
    reportPublisherApplication.setExcerptCsvService(excerptCsvService);
    appProperties.setExcerptsCsvDirectoryName(excerptsCsvDirectoryName);

    reportPublisherApplication.run(args);

    verify(excerptCsvService, times(1)).processFile(any());
  }

  @Test
  void shouldNotProcessIfIncorrectCsvFolder() {
    when(args.containsOption("excerpts-csv")).thenReturn(true);
    appProperties.setExcerptsCsvDirectoryName(excerptsCsvDirectoryName + "a");

    reportPublisherApplication.run(args);

    verify(excerptDocxService, never()).processFile(any());
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

