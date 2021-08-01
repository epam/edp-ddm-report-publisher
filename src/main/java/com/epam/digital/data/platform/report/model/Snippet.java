package com.epam.digital.data.platform.report.model;

import java.util.Objects;

public class Snippet {
  private String trigger;
  private String description;
  private String snippet;

  public String getTrigger() {
    return trigger;
  }

  public void setTrigger(String trigger) {
    this.trigger = trigger;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSnippet() {
    return snippet;
  }

  public void setSnippet(String snippet) {
    this.snippet = snippet;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Snippet snippet1 = (Snippet) o;
    return Objects.equals(trigger, snippet1.trigger) && Objects
        .equals(description, snippet1.description) && Objects
        .equals(snippet, snippet1.snippet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trigger, description, snippet);
  }
}
