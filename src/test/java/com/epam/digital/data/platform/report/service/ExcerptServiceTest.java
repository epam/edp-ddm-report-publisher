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

  private static final String expectedChecksum = "dccebf6047bf21908fd7ae0fb1c64715f7b8cb3a9f0e51f4df3dec9800b596b8";

  private static String expectedResult;
  private static File correctReportFile;
  private static File missingPictureFile;
  private static File missingStyleCssFile;
  private static File correctResultFile;
  private static File redundantStylesFile;

  private ExcerptService service;

  @BeforeAll
  static void setup() throws IOException, URISyntaxException {
    correctReportFile = getFile("/excerpts/CorrectExcerpt");
    missingPictureFile = getFile("/excerpts/MissingPictureExcerpt");
    missingStyleCssFile = getFile("/excerpts/MissingStyleCssExcerpt");
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
    assertThat(resultDocument.head().select("style")).hasToString("<style>\n"
        + "* { font-family: DejaVu Sans; }\n"
        + "</style>");
  }

  @Test
  void shouldThrowExceptionWhenMissingPicture() {
    Assertions.assertThrows(ExcerptBuildingException.class,
        () -> service.loadDir(missingPictureFile));
  }

  @Test
  void shouldThrowExceptionWhenMissingStyleCss() {
    Assertions.assertThrows(ExcerptBuildingException.class,
        () -> service.loadDir(missingStyleCssFile));
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