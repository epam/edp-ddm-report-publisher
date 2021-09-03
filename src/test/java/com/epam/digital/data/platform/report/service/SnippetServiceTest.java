package com.epam.digital.data.platform.report.service;

import static com.epam.digital.data.platform.report.util.TestUtils.snippet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.SnippetClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class SnippetServiceTest {

  @Mock
  SnippetClient client;

  SnippetService instance;

  @BeforeEach
  void init() {
    instance = new SnippetService(client);
  }

  @Test
  void shouldPublishSnippets() {
    var snippet = snippet("stub");
    when(client.postSnippet(snippet)).thenReturn(mockVoidResponse());

    instance.publish(List.of(snippet));

    verify(client).postSnippet(snippet);
  }

  private ResponseEntity<Void> mockVoidResponse() {
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
