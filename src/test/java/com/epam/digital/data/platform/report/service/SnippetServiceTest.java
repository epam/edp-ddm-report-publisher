package com.epam.digital.data.platform.report.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.SnippetClient;
import com.epam.digital.data.platform.report.model.Snippet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = {SnippetService.class})
public class SnippetServiceTest {

  @MockBean
  private SnippetClient client;
  @Autowired
  private SnippetService service;

  @Test
  void shouldPublishSnippets() {
    var snippet = mockSnippet();
    when(client.postSnippet(snippet)).thenReturn(mockVoidResponse());

    service.publish(List.of(snippet));

    verify(client).postSnippet(snippet);
  }

  private Snippet mockSnippet() {
    var snippet = new Snippet();
    snippet.setSnippet("stub");
    snippet.setDescription("stub description");
    snippet.setTrigger("stub trigger");
    return snippet;
  }

  private ResponseEntity<Void> mockVoidResponse() {
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
