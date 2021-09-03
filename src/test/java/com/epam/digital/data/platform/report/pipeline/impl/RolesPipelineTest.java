package com.epam.digital.data.platform.report.pipeline.impl;

import static com.epam.digital.data.platform.report.util.TestUtils.context;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.model.Group;
import com.epam.digital.data.platform.report.service.GroupService;
import com.epam.digital.data.platform.report.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
public class RolesPipelineTest {

  @Mock
  GroupService groupService;

  @Mock
  RoleService roleService;

  ObjectMapper mapper;

  RolesPipeline instance;

  private static List<File> rolesFile;

  @BeforeAll
  static void setup() throws FileNotFoundException {
    var file = ResourceUtils.getFile("classpath:roles/officer.yml");
    rolesFile = of(file);
  }

  @BeforeEach
  void init() {
    mapper = new ObjectMapper(new YAMLFactory());
    instance = new RolesPipeline(mapper, groupService, roleService);
  }

  @Test
  void shouldSuccessfullyCreateRole() {
    when(groupService.getGroups()).thenReturn(emptyList());

    instance.process(rolesFile, context());

    verify(groupService).getGroups();
    verify(roleService).create(any());
    verify(roleService, never()).delete(any());
  }

  @Test
  void shouldSuccessfullyDeleteRole() {
    when(groupService.getGroups()).thenReturn(groups("citizen", "officer"));

    instance.process(rolesFile, context());

    verify(groupService).getGroups();
    verify(roleService, never()).create(any());
    verify(roleService).delete(any());
  }

  @Test
  void shouldSuccessfullyIgnoreExistingRole() {
    when(groupService.getGroups()).thenReturn(groups("officer"));

    instance.process(rolesFile, context());

    verify(groupService).getGroups();
    verify(roleService, never()).create(any());
    verify(roleService, never()).delete(any());
  }

  @Test
  void shouldNotBeApplicableToNotYamlFile() throws FileNotFoundException {
    var file = ResourceUtils.getFile("classpath:roles/officer.json");
    var files = of(file);

    var result = instance.isApplicable(files);

    assertThat(result).isFalse();
  }

  @Test
  void shouldNotBeApplicableToNotOfficerYaml() throws FileNotFoundException {
    var file = ResourceUtils.getFile("classpath:roles/citizen.yaml");
    var files = of(file);

    var result = instance.isApplicable(files);

    assertThat(result).isFalse();
  }

  private List<Group> groups(String... names) {
    var groups = new ArrayList<Group>();

    for (var name : names) {
      var group = new Group();
      group.setName(name);
      groups.add(group);
    }

    return groups;
  }
}
