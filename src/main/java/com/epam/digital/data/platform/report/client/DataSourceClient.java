package com.epam.digital.data.platform.report.client;

import com.epam.digital.data.platform.report.config.FeignConfig;
import com.epam.digital.data.platform.report.model.DataSource;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "datasource", url = "${redash.url}", configuration = FeignConfig.class)
public interface DataSourceClient {

  @GetMapping("/api/data_sources")
  ResponseEntity<List<DataSource>> getDataSources();

  @PostMapping("/api/data_sources")
  ResponseEntity<DataSource> createDataSource(@RequestBody DataSource dataSource);

  @DeleteMapping("/api/data_sources/{id}")
  ResponseEntity<Void> deleteDataSource(@PathVariable("id") int id);
}
