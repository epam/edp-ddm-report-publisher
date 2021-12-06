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

import static com.epam.digital.data.platform.report.service.RoleService.ADMIN_REGISTRY_NAME;
import static com.epam.digital.data.platform.report.service.RoleService.ADMIN_ROLE_NAME;
import static com.epam.digital.data.platform.report.service.RoleService.AUDITOR_ROLE_NAME;
import static com.epam.digital.data.platform.report.util.IOUtils.getFileList;
import static com.epam.digital.data.platform.report.util.ResponseHandler.handleResponse;
import static java.util.stream.Collectors.toList;

import com.epam.digital.data.platform.report.client.DataSourceClient;
import com.epam.digital.data.platform.report.config.properties.AppProperties;
import com.epam.digital.data.platform.report.model.Context;
import com.epam.digital.data.platform.report.model.DataSource;
import com.epam.digital.data.platform.report.pipeline.AbstractPipeline;
import com.epam.digital.data.platform.report.service.ExcerptService;
import com.epam.digital.data.platform.report.service.RoleService;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReportPublisherApplication implements ApplicationRunner {

  private final Logger log = LoggerFactory.getLogger(ReportPublisherApplication.class);

  private final AppProperties appProperties;
  private final DataSourceClient dataSourceClient;
  private final List<AbstractPipeline> pipelines;
  private final ExcerptService excerptService;
  private final RoleService roleService;

  private final String dataSourcePrefix;

  public ReportPublisherApplication(AppProperties appProperties,
      DataSourceClient dataSourceClient, List<AbstractPipeline> pipelines,
      ExcerptService excerptService, RoleService roleService,
      @Value("${DB_NAME}") String prefix) {
    this.appProperties = appProperties;
    this.dataSourceClient = dataSourceClient;
    this.pipelines = pipelines;
    this.excerptService = excerptService;
    this.roleService = roleService;
    this.dataSourcePrefix = prefix;
  }

  public static void main(String[] args) {
    SpringApplication.run(ReportPublisherApplication.class, args);
  }

  @Override
  public void run(ApplicationArguments args) {
    if (args.containsOption(ADMIN_ROLE_NAME)) {
      roleService.createAdminRegistry();
    }

    if (args.containsOption(AUDITOR_ROLE_NAME)) {
      roleService.createAuditorGroup();
    }

    if (args.containsOption("roles")) {
      handleRoles();
    }

    if (args.containsOption("reports")) {
      handleReports();
    }

    if (args.containsOption("excerpts")) {
      handleExcerpts();
    }
  }

  private void handleReports() {
    List<DataSource> dataSources = handleResponse(dataSourceClient.getDataSources());

    for (File reportsDir : getDirectories(appProperties.getReportsDirectoryName())) {
      log.info("Processing {} directory", reportsDir.getName());

      getDataSource(dataSources, reportsDir.getName())
          .ifPresent(dataSourceId -> {
            var context = createContext(dataSourceId);
            var files = getFiles(reportsDir);

            processPipelines(context, files);
          });
    }
  }

  private void handleRoles() {
    var directory = FileUtils.getFile(appProperties.getRolesDirectoryName());
    var files = getFiles(directory);

    processPipelines(null, files);
  }

  private void handleExcerpts() {
    for (File templateDir : getDirectories(appProperties.getExcerptsDirectoryName())) {
      log.info("Processing {} directory", templateDir.getName());
      excerptService.loadDir(templateDir);
    }
  }

  private void processPipelines(Context context, List<File> files) {
    for (var pipeline : pipelines) {
      if (pipeline.isApplicable(files)) {
        pipeline.process(files, context);
      }
    }
  }

  private Context createContext(Integer dataSourceId) {
    var context = new Context();
    context.setDataSourceId(dataSourceId);
    return context;
  }

  private Optional<Integer> getDataSource(List<DataSource> dataSources, String name) {
    return name.equals(ADMIN_ROLE_NAME)
        ? getDataSourceId(dataSources,
            dataSource -> dataSource.getName().equalsIgnoreCase(ADMIN_REGISTRY_NAME))
        : getDataSourceId(dataSources,
            dataSource -> dataSource.getName().equalsIgnoreCase(dataSourceNameForRole(name)));
  }

  private Optional<Integer> getDataSourceId(List<DataSource> dataSources, Predicate<? super DataSource> rule) {
    return dataSources.stream()
        .filter(rule)
        .map(DataSource::getId)
        .findFirst();
  }

  private String dataSourceNameForRole(String role) {
    return dataSourcePrefix + "_" + role;
  }

  private List<File> getDirectories(String root) {
    if(!FileUtils.getFile(root).exists()) {
      log.error("Directory {} does not exist", root);
      return Collections.emptyList();
    }
    return Arrays.stream(getFileList(FileUtils.getFile(root)))
        .filter(File::isDirectory)
        .collect(toList());
  }

  private List<File> getFiles(File reportDir) {
    return Arrays.stream(getFileList(reportDir))
        .collect(Collectors.toList());
  }
}
