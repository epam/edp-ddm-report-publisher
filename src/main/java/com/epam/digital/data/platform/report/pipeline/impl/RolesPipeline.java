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

package com.epam.digital.data.platform.report.pipeline.impl;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.isExtension;

import com.epam.digital.data.platform.report.model.Context;
import com.epam.digital.data.platform.report.model.Group;
import com.epam.digital.data.platform.report.model.Role;
import com.epam.digital.data.platform.report.model.RolesList;
import com.epam.digital.data.platform.report.pipeline.AbstractPipeline;
import com.epam.digital.data.platform.report.pipeline.PipelineOrder;
import com.epam.digital.data.platform.report.service.GroupService;
import com.epam.digital.data.platform.report.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(PipelineOrder.ROLES_PIPELINE)
@Component
public class RolesPipeline extends AbstractPipeline {

  private final Predicate<? super File> OFFICER_ROLE_FILTER =
      file -> !file.isDirectory() && file.getName().contains("officer")
          && isExtension(file.getName(), "yaml", "yml");

  private final List<String> defaultGroups = List.of("admin", "default", "auditor");

  private final GroupService groupService;
  private final RoleService roleService;

  public RolesPipeline(@Qualifier("yamlMapper") ObjectMapper objectMapper,
      GroupService groupService,
      RoleService roleService) {
    super(objectMapper);
    this.groupService = groupService;
    this.roleService = roleService;
  }

  @Override
  public void process(List<File> files, Context context) {
    var roles = getRoles(files);

    var existingGroups = groupService.getGroups();

    deleteRemovedGroups(existingGroups, roles);
    createAddedGroups(existingGroups, roles);
  }

  private void createAddedGroups(List<Group> existingGroups, List<Role> roles) {
    var existingNames = existingGroups.stream()
        .map(Group::getName)
        .collect(toList());

    roles.stream()
        .filter(role -> !existingNames.contains(role.getName()))
        .forEach(roleService::create);
  }

  private void deleteRemovedGroups(List<Group> existingGroups, List<Role> roles) {
    var roleNames = roles.stream()
        .map(Role::getName)
        .collect(toList());

    existingGroups.stream()
        .filter(group -> !defaultGroups.contains(group.getName()))
        .filter(group -> !roleNames.contains(group.getName()))
        .forEach(roleService::delete);
  }

  @Override
  public boolean isApplicable(List<File> files) {
    return files.stream().anyMatch(OFFICER_ROLE_FILTER);
  }

  private List<Role> getRoles(List<File> files) {
    return files.stream()
        .filter(OFFICER_ROLE_FILTER)
        .map(file -> readValue(file, RolesList.class))
        .flatMap(rolesList -> rolesList.getRoles().stream())
        .collect(toList());
  }
}
