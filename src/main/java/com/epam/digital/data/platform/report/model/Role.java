package com.epam.digital.data.platform.report.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;

public class Role {

  private String name;
  private String description;

  @JsonIgnore
  private String password;

  public Role() {
  }

  public Role(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Role role = (Role) o;
    return Objects.equals(name, role.name) && Objects
        .equals(description, role.description) && Objects.equals(password, role.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, password);
  }
}
