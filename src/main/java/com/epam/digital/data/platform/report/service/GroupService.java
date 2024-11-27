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

import static com.epam.digital.data.platform.report.util.ResponseHandler.handleResponse;

import com.epam.digital.data.platform.report.client.GroupClient;
import com.epam.digital.data.platform.report.model.DataSource;
import com.epam.digital.data.platform.report.model.DataSourceAssociation;
import com.epam.digital.data.platform.report.model.Group;
import com.epam.digital.data.platform.report.model.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GroupService {

  private final GroupClient client;

  public GroupService(GroupClient client) {
    this.client = client;
  }

  public Optional<Group> getGroup(Role role) {
    return handleResponse(client.getGroups()).stream()
        .filter(group -> group.getName().equals(role.getName()))
        .findFirst();
  }

  public List<Group> getGroups() {
    return handleResponse(client.getGroups());
  }

  public Group createGroup(Group group) {
    return handleResponse(client.createGroup(group));
  }

  public void deleteGroup(Group group) {
    client.deleteGroup(group.getId());
  }

  public void associate(DataSource dataSource, Group... groups) {
    for (var group : groups) {
      var association = new DataSourceAssociation();
      association.setDataSourceId(dataSource.getId());
      client.associateGroupWithDataSource(group.getId(), association);
    }
  }

}
