package com.epam.digital.data.platform.report.client;

import com.epam.digital.data.platform.report.config.FeignConfig;
import com.epam.digital.data.platform.report.model.Snippet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "snippet", url = "${redash.url}", configuration = FeignConfig.class)
public interface SnippetClient {

  @PostMapping("/api/query_snippets")
  ResponseEntity<Void> postSnippet(@RequestBody Snippet snippet);
}
