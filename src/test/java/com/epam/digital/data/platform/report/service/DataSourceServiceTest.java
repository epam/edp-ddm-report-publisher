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

package com.epam.digital.data.platform.report.service;

import static com.epam.digital.data.platform.report.util.TestUtils.dataSources;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.DataSourceClient;
import com.epam.digital.data.platform.report.exception.NoDataSourceFoundException;
import com.epam.digital.data.platform.report.model.DataSource;
import com.epam.digital.data.platform.report.model.Group;
import com.epam.digital.data.platform.report.model.Role;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class DataSourceServiceTest {

  @Mock
  private DataSourceClient client;

  private final String HOST = "HOST";
  private final String NAME = "NAME";

  private final String REGISTRY = "officer registry";

  private DataSourceService instance;

  @BeforeEach
  void init() {
    instance = new DataSourceService(client, HOST, NAME);
  }

  @Test
  void getDataSourceHappyPath() {
    var group = new Group();
    group.setName("officer");

    when(client.getDataSources()).thenReturn(listResponse());

    var result = instance.getDataSource(group);

    assertThat(result.getName()).isEqualTo(REGISTRY);
  }

  @Test
  void getDataSourceThrowExceptionWhenNoDataSourceFound() {
    var group = new Group();
    group.setName("citizen");

    when(client.getDataSources()).thenReturn(listResponse());

    assertThatThrownBy(() -> instance.getDataSource(group))
        .isInstanceOf(NoDataSourceFoundException.class)
        .hasMessage(String.format("No datasource found for role: %s", group.getName()));
  }

  @Test
  void shouldDeleteDataSource() {
    var dataSource = new DataSource();
    dataSource.setId(1);

    instance.deleteDataSource(dataSource);

    verify(client).deleteDataSource(1);
  }

  @Test
  void shouldCreateDataSource() {
    var dataSource = new DataSource();
    dataSource.setId(1);

    when(client.createDataSource(any())).thenReturn(entityResponse());

    instance.createDataSource(dataSource);

    verify(client).createDataSource(dataSource);
  }

  @Test
  void getDataSources() {
    when(client.getDataSources()).thenReturn(listResponse());

    instance.getDataSources();

    verify(client).getDataSources();
  }

  @Test
  void buildDataSource() {
    var role = new Role();
    role.setName("officer");
    role.setPassword("password");

    var result = instance.buildDataSource(role);
    var options = result.getOptions();

    assertThat(options.get("host")).isEqualTo(HOST);
    assertThat(options.get("dbname")).isEqualTo(NAME);
    assertThat(options.get("user")).isEqualTo("analytics_officer");
    assertThat(options.get("password")).isEqualTo("password");
    assertThat(result.getName()).isEqualTo(NAME + "_" + role.getName());
  }

  private ResponseEntity<List<DataSource>> listResponse() {
    return new ResponseEntity<>(dataSources(REGISTRY), HttpStatus.OK);
  }

  private ResponseEntity<DataSource> entityResponse() {
    var dataSource = new DataSource();
    dataSource.setName(REGISTRY);
    dataSource.setId(1);

    return new ResponseEntity<>(dataSource, HttpStatus.OK);
  }
}
