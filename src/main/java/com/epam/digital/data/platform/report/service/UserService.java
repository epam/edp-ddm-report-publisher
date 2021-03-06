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

import com.epam.digital.data.platform.report.exception.DatabaseUserException;
import com.epam.digital.data.platform.report.model.Group;
import com.epam.digital.data.platform.report.model.Role;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final DataSource dataSource;

  static final String CREATE_ROLE = "call p_create_analytics_user(?, ?);";
  static final String DELETE_ROLE = "call p_delete_analytics_user(?);";

  public UserService(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void createUser(Role role) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareCall(CREATE_ROLE)) {
      statement.setString(1, buildUserNameFor(role.getName()));
      statement.setString(2, role.getPassword());
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseUserException("Could not create user", e);
    }
  }

  public void deleteUser(Group group) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareCall(DELETE_ROLE)) {
      statement.setString(1, buildUserNameFor(group.getName()));
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseUserException("Could not drop user", e);
    }
  }

  private String buildUserNameFor(String name) {
    return "\"analytics_" + name + "\"";
  }
}
