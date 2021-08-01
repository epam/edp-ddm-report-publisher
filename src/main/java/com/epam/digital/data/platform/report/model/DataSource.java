package com.epam.digital.data.platform.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.util.Map;
import java.util.Objects;

public class DataSource {

  @JsonProperty(access = Access.WRITE_ONLY)
  private int id;
  private String name;
  private String queueName;
  private String syntax;
  private String type;
  private Map<String, String> options;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getQueueName() {
    return queueName;
  }

  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }

  public String getSyntax() {
    return syntax;
  }

  public void setSyntax(String syntax) {
    this.syntax = syntax;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DataSource that = (DataSource) o;
    return id == that.id && Objects.equals(name, that.name) && Objects
        .equals(queueName, that.queueName) && Objects.equals(syntax, that.syntax)
        && Objects.equals(type, that.type) && Objects
        .equals(options, that.options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, queueName, syntax, type, options);
  }
}
