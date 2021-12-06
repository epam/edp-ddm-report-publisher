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

import com.epam.digital.data.platform.report.exception.ExcerptBuildingException;
import com.epam.digital.data.platform.report.model.ExcerptTemplate;
import com.epam.digital.data.platform.report.repository.ExcerptTemplateRepository;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class ExcerptService {

  private final ExcerptTemplateRepository repository;

  public ExcerptService(ExcerptTemplateRepository repository) {
    this.repository = repository;
  }

  public void loadDir(File dir) {
    var indexFile = Path.of(dir.getPath(), "index.html.ftl").toFile();
    try {
      var htmlString = FileUtils.readFileToString(indexFile, StandardCharsets.UTF_8);
      var document = Jsoup.parse(htmlString);
      document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

      embedImagesToHtml(document, dir);
      embedStyleToHtml(document, dir);

      var template = new ExcerptTemplate();
      template.setTemplate(document.toString());
      template.setTemplateName(dir.getName());
      template.setChecksum(DigestUtils.sha256Hex(document.toString()));

      save(template);
    } catch (Exception e) {
      throw new ExcerptBuildingException("Failed to build template", e);
    }
  }

  private void embedImagesToHtml(Document htmlDocument, File dir) {
    for (Element image : htmlDocument.select("img")) {
      var imageFile = Path.of(dir.getPath(), image.attr("src")).toFile();
      try {
        var bytes = FileUtils.readFileToByteArray(imageFile);
        var base64encodedImage = Base64.getEncoder().encodeToString(bytes);
        image.attr("src", "data:image/jpeg;base64," + base64encodedImage);
      } catch (Exception e) {
        throw new ExcerptBuildingException(
            String.format("Failed to embed picture \"%s\" into template",
                image.attr("src")), e);
      }
    }
  }

  private void embedStyleToHtml(Document document, File dir) {
    var styleFile = Path.of(dir.getPath(), "css", "style.css").toFile();
    try {
      document.head().select("link").remove();
      document.head().select("style").remove();
      
      var styleString = FileUtils.readFileToString(styleFile, StandardCharsets.UTF_8);
      styleString += "\n* { font-family: Roboto; }\n";
      document.head().append("<style>" + styleString + "</style>");
    } catch (Exception e) {
      throw new ExcerptBuildingException("Failed to embed styles into template", e);
    }
  }

  private void save(ExcerptTemplate newTemplate) {
    var saved = repository.findFirstByTemplateName(newTemplate.getTemplateName());
    if (saved == null) {
      repository.save(newTemplate);
      return;
    }

    if (saved.getChecksum().equals(newTemplate.getChecksum())) {
      return;
    }

    saved.update(newTemplate);
    repository.save(saved);
  }
}
