package com.epam.digital.data.platform.report.repository;

import com.epam.digital.data.platform.report.model.ExcerptTemplate;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface ExcerptTemplateRepository extends CrudRepository<ExcerptTemplate, UUID> {

  ExcerptTemplate findFirstByTemplateName(String templateName);
}
