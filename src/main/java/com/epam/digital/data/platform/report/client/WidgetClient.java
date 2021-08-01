package com.epam.digital.data.platform.report.client;

import com.epam.digital.data.platform.report.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.epam.digital.data.platform.report.model.Widget;

@FeignClient(name = "widget", url = "${redash.url}", configuration = FeignConfig.class)
public interface WidgetClient {

    @PostMapping("/api/widgets")
    ResponseEntity<Widget> createWidget(@RequestBody Widget widget);

    @PostMapping("/api/widgets/{id}")
    ResponseEntity<Widget> updateWidget(@PathVariable("id") int id, @RequestBody Widget widget);

    @DeleteMapping("/api/widgets/id")
    ResponseEntity<Void> archiveWidget(@PathVariable("id") int id);
}
