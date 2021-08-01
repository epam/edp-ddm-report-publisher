package com.epam.digital.data.platform.report.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

  private String reportsDirectoryName;
  private String excerptsDirectoryName;
  private String rolesDirectoryName;

  public String getReportsDirectoryName() {
    return reportsDirectoryName;
  }

  public void setReportsDirectoryName(String reportsDirectoryName) {
    this.reportsDirectoryName = reportsDirectoryName;
  }

  public String getExcerptsDirectoryName() {
    return excerptsDirectoryName;
  }

  public void setExcerptsDirectoryName(String excerptsDirectoryName) {
    this.excerptsDirectoryName = excerptsDirectoryName;
  }

  public String getRolesDirectoryName() {
    return rolesDirectoryName;
  }

  public void setRolesDirectoryName(String rolesDirectoryName) {
    this.rolesDirectoryName = rolesDirectoryName;
  }
}
