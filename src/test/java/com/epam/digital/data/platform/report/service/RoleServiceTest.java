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

import static com.epam.digital.data.platform.report.util.TestUtils.dataSource;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.model.DataSource;
import com.epam.digital.data.platform.report.model.Group;
import com.epam.digital.data.platform.report.model.Role;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

  @Mock
  GroupService groupService;

  @Mock
  UserService userService;

  @Mock
  DataSourceService dataSourceService;

  private final String ADMIN_PASS = "admPass";
  private final String AUDITOR_PASS = "audPass";

  RoleService instance;

  @BeforeEach
  void init() {
    instance = new RoleService(ADMIN_PASS, AUDITOR_PASS, groupService, userService, dataSourceService);
  }

  @Test
  void shouldSkipAuditorGroupCreationIfAuditorAlreadyExists() {
    var group = new Group();
    group.setName("auditor");

    when(groupService.getGroup(any())).thenReturn(Optional.of(group));

    instance.createAuditorGroup();

    verify(dataSourceService, never()).buildDataSource(any());
    verify(dataSourceService, never()).createDataSource(any());
    verify(groupService, never()).createGroup(any());
    verify(groupService, never()).associate(any(), any());
  }

  @Test
  void shouldCreateAuditorGroup() {
    when(groupService.getGroup(any())).thenReturn(Optional.empty());

    instance.createAuditorGroup();

    verify(dataSourceService).buildDataSource(any());
    verify(dataSourceService).createDataSource(any());
    verify(groupService).createGroup(any());
    verify(groupService).associate(any(), any());
  }

  @Test
  void shouldSkipAdminDataSourceCreationIfAdminAlreadyExists() {
    var redashAdminGroup = new Group("redash-admin");
    when(groupService.getGroup(any())).thenReturn(Optional.of(redashAdminGroup));
    var registryDs = dataSource("registry", 1);
    var officerRegistryDs = dataSource("officer registry", 2);
    when(dataSourceService.getDataSources()).thenReturn(List.of(registryDs, officerRegistryDs));

    instance.createAdminRegistry();

    verify(dataSourceService, never()).buildDataSource(any());
    verify(dataSourceService, never()).createDataSource(any());
    verify(groupService).associate(registryDs, redashAdminGroup);
  }

  @Test
  void shouldCreateAdminDataSource() {
    var redashAdminGroup = new Group("redash-admin");
    when(groupService.getGroup(any())).thenReturn(Optional.of(redashAdminGroup));
    when(dataSourceService.getDataSources()).thenReturn(emptyList());
    when(dataSourceService.buildDataSource(any())).thenReturn(new DataSource());
    var createdDs = new DataSource();
    when(dataSourceService.createDataSource(any())).thenReturn(createdDs);

    instance.createAdminRegistry();

    verify(dataSourceService).buildDataSource(any());
    verify(dataSourceService).createDataSource(any());
    verify(groupService).associate(createdDs, redashAdminGroup);
  }

  @Test
  void shouldCreateRole() {
    when(dataSourceService.buildDataSource(any())).thenReturn(new DataSource());
    var createdGroup = new Group("officer");
    when(groupService.createGroup(any())).thenReturn(createdGroup);

    var role = new Role();
    role.setName("officer");

    instance.create(role);

    verify(dataSourceService).buildDataSource(role);
    verify(dataSourceService).createDataSource(any());
    verify(groupService).createGroup(any());
    verify(groupService).associate(any(), any());
  }

  @Test
  void shouldDeleteGroup() {
    var group = new Group();
    group.setName("officer");

    var dataSource = new DataSource();
    dataSource.setName("officer registry");

    when(dataSourceService.getDataSource(group)).thenReturn(dataSource);

    instance.delete(group);

    verify(dataSourceService).getDataSource(group);
    verify(dataSourceService).deleteDataSource(dataSource);
    verify(groupService).deleteGroup(group);
    verify(userService).deleteUser(group);
  }
}
