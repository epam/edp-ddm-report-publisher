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

import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.report.exception.ExcerptBuildingException;
import com.epam.digital.data.platform.report.model.ExcerptTemplate;
import com.epam.digital.data.platform.report.repository.ExcerptTemplateRepository;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import static com.epam.digital.data.platform.report.util.DBUtils.saveToDataBase;
import static com.epam.digital.data.platform.report.util.IOUtils.getFileChecksum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy
public class ExcerptCsvService {

  public static final String EXCERPT_CONTENT_TYPE = "application/octet-stream";
  public static final String CSV = "csv";
  public static final String SLASH_DELIMITER = "/";

  private final Logger log = LoggerFactory.getLogger(ExcerptCsvService.class);

  private final ExcerptTemplateRepository repository;
  private final CephService datafactoryCephService;
  private final String bucket;

  public ExcerptCsvService(ExcerptTemplateRepository repository,
      CephService datafactoryCephService,
      @Value("${datafactory-excerpt-ceph.bucket}") String bucket) {
    this.repository = repository;
    this.datafactoryCephService = datafactoryCephService;
    this.bucket = bucket;
  }

  public void processFile(File file) {
    try {
      saveFileToCeph(file);
      saveToDataBase(repository, createTemplate(file));
    } catch (Exception e) {
      throw new ExcerptBuildingException("Failed to process csv file", e);
    }
  }

  private void saveFileToCeph(File file) {
    try {
      var cephKey = CSV + SLASH_DELIMITER + FilenameUtils.getBaseName(file.getName());
      byte[] bytes = FileUtils.readFileToByteArray(file);
      datafactoryCephService.put(
          bucket, cephKey, EXCERPT_CONTENT_TYPE, Collections.emptyMap(),
          new ByteArrayInputStream(bytes));
    } catch (IOException e) {
      log.error("Error when extract content from {} file", file.getName());
      throw new ExcerptBuildingException("Failed to extract content", e);
    }

  }

  private ExcerptTemplate createTemplate(File file) {
    var template = new ExcerptTemplate();

    String checkSum = getFileChecksum(file);
    String baseName = FilenameUtils.getBaseName(file.getName());

    template.setTemplate(CSV + SLASH_DELIMITER + baseName);
    template.setTemplateName(baseName);
    template.setChecksum(checkSum);
    template.setTemplateType(CSV);
    return template;
  }
}
