package com.epam.digital.data.platform.report.model;

import java.util.Objects;

public class DataSourceAssociation {
  private Integer dataSourceId;

  public Integer getDataSourceId() {
    return dataSourceId;
  }

  public void setDataSourceId(Integer dataSourceId) {
    this.dataSourceId = dataSourceId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DataSourceAssociation that = (DataSourceAssociation) o;
    return Objects.equals(dataSourceId, that.dataSourceId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataSourceId);
  }
}
