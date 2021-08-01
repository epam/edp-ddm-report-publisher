package com.epam.digital.data.platform.report.pipeline.impl;

import static java.util.stream.Collectors.toList;

import com.epam.digital.data.platform.report.pipeline.AbstractPipeline;
import com.epam.digital.data.platform.report.pipeline.Archiver;
import com.epam.digital.data.platform.report.pipeline.PipelineOrder;
import com.epam.digital.data.platform.report.pipeline.Publisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.epam.digital.data.platform.report.model.Context;
import com.epam.digital.data.platform.report.model.Dashboard;

@Order(PipelineOrder.DASHBOARD_PIPELINE)
@Component
public class DashboardPipeline extends AbstractPipeline {

    private final Archiver archiver;
    private final Publisher publisher;

    private final Predicate<? super File> DASHBOARD_FILTER =
        file -> !file.isDirectory() && file.getName().endsWith(".json");

    public DashboardPipeline(ObjectMapper objectMapper, Archiver archiver, Publisher publisher) {
        super(objectMapper);
        this.archiver = archiver;
        this.publisher = publisher;
    }

    public void process(List<File> files, Context context) {
        for (Dashboard dashboard : getDashboards(files)) {
            archiver.archive(dashboard);
            publisher.publish(dashboard, context);
        }
    }

    public boolean isApplicable(List<File> files) {
        return files.stream().anyMatch(DASHBOARD_FILTER);
    }

    private List<Dashboard> getDashboards(List<File> files) {
        return files.stream()
            .filter(DASHBOARD_FILTER)
            .map(file -> readValue(file, Dashboard.class))
            .collect(toList());
    }
}
