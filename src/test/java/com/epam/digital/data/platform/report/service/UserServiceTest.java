package com.epam.digital.data.platform.report.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.exception.DatabaseUserException;
import com.epam.digital.data.platform.report.model.Group;
import com.epam.digital.data.platform.report.model.Role;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  DataSource dataSource;

  @Mock
  Connection connection;

  @Mock
  CallableStatement statement;

  UserService instance;

  @BeforeEach
  void init() throws SQLException {
    instance = new UserService(dataSource);
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareCall(any())).thenReturn(statement);
  }

  @Test
  void shouldCreateUser() throws SQLException {
    var role = new Role();
    role.setName("officer");
    role.setPassword("password");

    instance.createUser(role);

    verify(statement).setString(1, "analytics_officer");
    verify(statement).setString(2, "password");
    verify(statement).execute();
  }

  @Test
  void shouldThrowDatabaseUserExceptionWhenSQLExceptionDuringCreate() throws SQLException {
    when(statement.execute()).thenThrow(new SQLException());

    var role = new Role();
    role.setName("officer");
    role.setPassword("password");

    assertThatThrownBy(() -> instance.createUser(role))
        .isInstanceOf(DatabaseUserException.class)
        .hasMessageContaining("Could not create user");
  }

  @Test
  void shouldDropUser() throws SQLException {
    var group = new Group();
    group.setName("officer");

    instance.deleteUser(group);

    verify(statement).execute();
  }

  @Test
  void shouldThrowDatabaseUserExceptionWhenSQLExceptionDuringDrop() throws SQLException {
    when(statement.execute()).thenThrow(new SQLException());

    var group = new Group();
    group.setName("officer");

    assertThatThrownBy(() -> instance.deleteUser(group))
        .isInstanceOf(DatabaseUserException.class)
        .hasMessageContaining("Could not drop user");
  }
}
