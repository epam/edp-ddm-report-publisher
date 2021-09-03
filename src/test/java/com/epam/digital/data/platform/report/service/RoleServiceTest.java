package com.epam.digital.data.platform.report.service;

import static com.epam.digital.data.platform.report.util.TestUtils.dataSources;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.model.DataSource;
import com.epam.digital.data.platform.report.model.Group;
import com.epam.digital.data.platform.report.model.Role;
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
    verify(groupService, never()).deleteAssociation(any(), any());
    verify(groupService, never()).associate(any(), any());
  }

  @Test
  void shouldCreateAuditorGroup() {
    when(groupService.getGroup(any())).thenReturn(Optional.empty());

    instance.createAuditorGroup();

    verify(dataSourceService).buildDataSource(any());
    verify(dataSourceService).createDataSource(any());
    verify(groupService).createGroup(any());
    verify(groupService).deleteAssociation(any(), any());
    verify(groupService).associate(any(), any());
  }

  @Test
  void shouldSkipAdminDataSourceCreationIfAdminAlreadyExists() {
    when(dataSourceService.getDataSources()).thenReturn(dataSources("registry", "officer registry"));

    instance.createAdminRegistry();

    verify(dataSourceService, never()).buildDataSource(any());
    verify(dataSourceService, never()).createDataSource(any());
  }

  @Test
  void shouldCreateAdminDataSource() {
    when(dataSourceService.getDataSources()).thenReturn(emptyList());
    when(dataSourceService.buildDataSource(any())).thenReturn(new DataSource());

    instance.createAdminRegistry();

    verify(dataSourceService).buildDataSource(any());
    verify(dataSourceService).createDataSource(any());
  }

  @Test
  void shouldCreateRole() {
    var role = new Role();
    role.setName("officer");

    instance.create(role);

    verify(dataSourceService).buildDataSource(role);
    verify(dataSourceService).createDataSource(any());
    verify(groupService).createGroup(any());
    verify(groupService).deleteAssociation(any(), any());
    verify(groupService).associate(any(), any());
    verify(userService).createUser(role);
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
