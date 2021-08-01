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

  public void associate(Group group, DataSource dataSource) {
    var association = new DataSourceAssociation();
    association.setDataSourceId(dataSource.getId());
    client.associateGroupWithDataSource(group.getId(), association);
  }

  public void deleteAssociation(Group group, DataSource dataSource) {
    client.deleteAssociationGroupWithDataSource(group.getId(), dataSource.getId());
  }
}
