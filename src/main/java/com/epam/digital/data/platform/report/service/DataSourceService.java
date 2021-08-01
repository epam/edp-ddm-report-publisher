package com.epam.digital.data.platform.report.service;

import static com.epam.digital.data.platform.report.util.ResponseHandler.handleResponse;

import com.epam.digital.data.platform.report.client.DataSourceClient;
import com.epam.digital.data.platform.report.exception.NoDataSourceFoundException;
import com.epam.digital.data.platform.report.model.DataSource;
import com.epam.digital.data.platform.report.model.Group;
import com.epam.digital.data.platform.report.model.Role;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DataSourceService {

  private final DataSourceClient client;

  private final String dbHost;
  private final String dbName;

  public DataSourceService(
      DataSourceClient client,
      @Value("${DB_URL}") String dbHost,
      @Value("${DB_NAME}") String dbName) {
    this.client = client;
    this.dbHost = dbHost;
    this.dbName = dbName;
  }

  public DataSource getDataSource(Group group) {
    return handleResponse(client.getDataSources()).stream()
        .filter(dataSource -> dataSource.getName().toLowerCase().contains(group.getName()))
        .findFirst()
        .orElseThrow(() -> new NoDataSourceFoundException(
            String.format("No datasource found for role: %s", group.getName())));
  }

  public List<DataSource> getDataSources() {
    return handleResponse(client.getDataSources());
  }

  public DataSource createDataSource(DataSource dataSource) {
    return handleResponse(client.createDataSource(dataSource));
  }

  public void deleteDataSource(DataSource dataSource) {
    client.deleteDataSource(dataSource.getId());
  }

  public DataSource buildDataSource(Role role) {
    var map = new HashMap<String, String>();
    map.put("host", dbHost);
    map.put("dbname", dbName);
    map.put("user", "analytics_" + role.getName());
    map.put("password", role.getPassword());

    var dataSource = new DataSource();
    dataSource.setName(dbName + "_" + role.getName());
    dataSource.setQueueName("queries");
    dataSource.setType("pg");
    dataSource.setSyntax("sql");
    dataSource.setOptions(map);

    return dataSource;
  }
}
