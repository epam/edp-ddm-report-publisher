package com.epam.digital.data.platform.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.epam.digital.data.platform.report.config.FeignConfig;
import com.epam.digital.data.platform.report.model.Visualization;

@FeignClient(name = "visualization", url = "${redash.url}", configuration = FeignConfig.class)
public interface VisualizationClient {

    @PostMapping("/api/visualizations")
    ResponseEntity<Visualization> createVisualization(@RequestBody Visualization visualization);

    @PostMapping("/api/visualizations/{id}")
    ResponseEntity<Visualization> updateVisualization(@PathVariable("id") int id,
        @RequestBody Visualization visualization);

    @DeleteMapping("/api/visualizations/id")
    ResponseEntity<Void> archiveVisualization(@PathVariable("id") int id);
}
