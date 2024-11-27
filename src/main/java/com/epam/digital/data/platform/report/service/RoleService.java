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

import com.epam.digital.data.platform.report.exception.NoGroupFoundException;
import com.epam.digital.data.platform.report.model.DataSource;
import com.epam.digital.data.platform.report.model.Group;
import com.epam.digital.data.platform.report.model.Role;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RoleService {

  public static final String AUDITOR_ROLE_NAME = "auditor";
  public static final String ADMIN_ROLE_NAME = "admin";
  public static final String ADMIN_REGISTRY_NAME = "registry";
  public static final String REDASH_ADMIN_ROLE_NAME = "redash-admin";

  private final Logger log = LoggerFactory.getLogger(RoleService.class);

  private final GroupService groupService;
  private final UserService userService;
  private final DataSourceService dataSourceService;

  private final Map<String, String> rolePasswordMap = new HashMap<>();

  public RoleService(
      @Value("${PWD_ADMIN}") String adminPassword,
      @Value("${PWD_AUDITOR}") String auditorPassword,
      GroupService groupService,
      UserService userService,
      DataSourceService dataSourceService) {
    this.groupService = groupService;
    this.userService = userService;
    this.dataSourceService = dataSourceService;

    this.rolePasswordMap.put(ADMIN_ROLE_NAME, adminPassword);
    this.rolePasswordMap.put(AUDITOR_ROLE_NAME, auditorPassword);
  }

  public void createAuditorGroup() {
    if (!isGroupCreated(AUDITOR_ROLE_NAME)) {
      log.info("Creating auditor group and datasource");

      var group = new Group(AUDITOR_ROLE_NAME);
      var role = new Role(AUDITOR_ROLE_NAME);

      role.setPassword(rolePasswordMap.get(AUDITOR_ROLE_NAME));
      var dataSource = dataSourceService.buildDataSource(role);

      var createdGroup = groupService.createGroup(group);
      var createdDataSource = dataSourceService.createDataSource(dataSource);
      groupService.associate(createdDataSource, createdGroup);
    }
  }

  public void createAdminRegistry() {
    var redashAdminGroup = groupService.getGroup(new Role(REDASH_ADMIN_ROLE_NAME))
            .orElseThrow(() -> new NoGroupFoundException(REDASH_ADMIN_ROLE_NAME));
    DataSource adminDataSource;
    var existingAdminDataSourceOpt = getAdminDataSource();
    if (existingAdminDataSourceOpt.isEmpty()) {
      log.info("Creating admin datasource");

      var role = new Role(ADMIN_ROLE_NAME);

      role.setPassword(rolePasswordMap.get(ADMIN_ROLE_NAME));
      var dataSource = dataSourceService.buildDataSource(role);
      dataSource.setName(ADMIN_REGISTRY_NAME);

      adminDataSource = dataSourceService.createDataSource(dataSource);
    } else {
      adminDataSource = existingAdminDataSourceOpt.get();
    }
    groupService.associate(adminDataSource, redashAdminGroup);
  }

  public void create(Role role) {
    log.info("Creating role: {}", role.getName());

    var group = new Group(role.getName());

    role.setPassword(RandomStringUtils.randomAlphanumeric(16));
    var dataSource = dataSourceService.buildDataSource(role);

    createAndAssociate(group, role, dataSource);
  }

  public void delete(Group group) {
    log.info("Deleting group: {}", group.getName());

    var dataSource = dataSourceService.getDataSource(group);
    dataSourceService.deleteDataSource(dataSource);
    groupService.deleteGroup(group);
    userService.deleteUser(group);
  }

  private void createAndAssociate(Group group, Role role, DataSource dataSource) {
    var createdGroup = groupService.createGroup(group);
    userService.createUser(role);
    var createdDataSource = dataSourceService.createDataSource(dataSource);
    groupService.associate(createdDataSource, createdGroup);
  }

  private Optional<DataSource> getAdminDataSource() {
    return dataSourceService.getDataSources().stream()
        .filter(dataSource -> dataSource.getName().equals(ADMIN_REGISTRY_NAME))
        .findFirst();
  }

  private boolean isGroupCreated(String role) {
    return groupService.getGroup(new Role(role)).isPresent();
  }
}
