/*
 * Copyright 2022 EPAM Systems.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.exception.ExcerptBuildingException;
import com.epam.digital.data.platform.report.model.ExcerptTemplate;
import com.epam.digital.data.platform.report.repository.ExcerptTemplateRepository;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExcerptServiceTest {

  @Mock
  private ExcerptTemplateRepository repository;
  @Mock
  private ExcerptTemplate mockTemplate;
  @Captor
  private ArgumentCaptor<ExcerptTemplate> templateCaptor;

  private static final String expectedChecksum = "009bed673a8a30cd6d1db46067201b146e8a4a7666a66d8456bb5fbb69965590";

  private static String expectedResult;
  private static File correctReportFile;
  private static File missingPictureFile;
  private static File correctResultFile;
  private static File redundantStylesFile;

  private ExcerptService service;

  @BeforeAll
  static void setup() throws IOException, URISyntaxException {
    correctReportFile = getFile("/excerpts/CorrectExcerpt");
    missingPictureFile = getFile("/excerpts/MissingPictureExcerpt");
    correctResultFile = getFile("/excerpts/correctResult");
    redundantStylesFile = getFile("/excerpts/RedundantStylesExcerpt");

    expectedResult = FileUtils.readFileToString(correctResultFile, StandardCharsets.UTF_8);
  }

  @BeforeEach
  void init() {
    service = new ExcerptService(repository);
  }

  @Test
  void happyPath() {
    service.loadDir(correctReportFile);

    verify(repository).save(templateCaptor.capture());
    var result = templateCaptor.getValue().getTemplate();
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void shouldDeleteLinkAndStyleFromHeadAndAddFont() {
    service.loadDir(redundantStylesFile);

    verify(repository).save(templateCaptor.capture());
    var result = templateCaptor.getValue().getTemplate();

    var resultDocument = Jsoup.parse(result);
    assertThat(resultDocument.head().select("link")).isEmpty();
    assertThat(resultDocument.head().select("style")).hasToString("<style>\n\n"
        + "* { font-family: Roboto; }\n"
        + "</style>");
  }

  @Test
  void shouldThrowExceptionWhenMissingPicture() {
    Assertions.assertThrows(ExcerptBuildingException.class,
        () -> service.loadDir(missingPictureFile));
  }

  @Test
  void shouldThrowExceptionWhenArchiveIsCorrupted() {
    Assertions.assertThrows(ExcerptBuildingException.class,
        () -> service.loadDir(correctResultFile));
  }

  @Test
  void shouldNotSaveNewTemplateIfAlreadyExistTemplateWithTheSameNameAndChecksum() {
    when(repository.findFirstByTemplateName("CorrectExcerpt")).thenReturn(mockTemplate);
    when(mockTemplate.getChecksum()).thenReturn(expectedChecksum);

    service.loadDir(correctReportFile);

    verify(repository, never()).save(any());
  }

  @Test
  void shouldUpdateSavedTemplateIfOldChecksumNotEqualToNewChecksum() {
    when(repository.findFirstByTemplateName("CorrectExcerpt")).thenReturn(mockTemplate);
    when(mockTemplate.getChecksum()).thenReturn("0");

    service.loadDir(correctReportFile);

    verify(mockTemplate).update(templateCaptor.capture());
    var checksum = templateCaptor.getValue().getChecksum();
    var template = templateCaptor.getValue().getTemplate();

    assertThat(checksum).isEqualTo(expectedChecksum);
    assertThat(template).isEqualTo(expectedResult);

    verify(repository).save(mockTemplate);
  }

  private static File getFile(String path) throws URISyntaxException {
    return new File(Objects.requireNonNull(ExcerptServiceTest.class.getResource(path)).toURI());
  }
}