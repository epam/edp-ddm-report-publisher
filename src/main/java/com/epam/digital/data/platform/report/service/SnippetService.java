/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.report.service;

import com.epam.digital.data.platform.report.client.SnippetClient;
import com.epam.digital.data.platform.report.model.Snippet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SnippetService {

  private final SnippetClient client;

  private final Logger log = LoggerFactory.getLogger(SnippetService.class);

  public SnippetService(SnippetClient client) {
    this.client = client;
  }

  public void publish(List<Snippet> snippets) {
    log.info("Publishing snippets");
    snippets.forEach(client::postSnippet);
  }
}
