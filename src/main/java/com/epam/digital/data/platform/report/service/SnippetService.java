package com.epam.digital.data.platform.report.service;

import com.epam.digital.data.platform.report.client.SnippetClient;
import com.epam.digital.data.platform.report.model.Snippet;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SnippetService {

  private final SnippetClient client;

  public SnippetService(SnippetClient client) {
    this.client = client;
  }

  public void publish(List<Snippet> snippets) {
    snippets.forEach(client::postSnippet);
  }
}
