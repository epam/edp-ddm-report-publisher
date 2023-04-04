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

package com.epam.digital.data.platform.report.pipeline;

import static java.util.stream.Collectors.toList;
import static com.epam.digital.data.platform.report.util.ResponseHandler.handleResponse;

import com.epam.digital.data.platform.report.model.Page;
import com.epam.digital.data.platform.report.service.QueryService;

import java.util.Collection;
import java.util.List;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.epam.digital.data.platform.report.client.DashboardClient;
import com.epam.digital.data.platform.report.model.Dashboard;
import com.epam.digital.data.platform.report.model.Query;
import com.epam.digital.data.platform.report.model.Visualization;
import com.epam.digital.data.platform.report.model.Widget;

@Component
public class Archiver {

    private final DashboardClient dashboardClient;
    private final QueryService queryService;

    private final Logger log = LoggerFactory.getLogger(Archiver.class);

    public Archiver(DashboardClient dashboardClient, QueryService queryService) {
        this.dashboardClient = dashboardClient;
        this.queryService = queryService;
    }

    public void archiveAll() {
        int page = 0;
        Page<Dashboard> dashboardsPageResponse;
        do {
            page++;
            dashboardsPageResponse = handleResponse(dashboardClient.findAllDashboards(page));
            dashboardsPageResponse.getResults()
                    .forEach(dashboard -> {
                        log.info("Archiving dashboard: id = {}, name = '{}'", dashboard.getId(), dashboard.getName());
                        handleResponse(dashboardClient.archiveDashboard(dashboard.getId()));
                        queryService.archive(getQueries(dashboard));
                    });
        } while (page * dashboardsPageResponse.getPageSize() < dashboardsPageResponse.getCount());
    }

    private List<Query> getQueries(Dashboard dashboard) {
        return Optional.ofNullable(dashboard.getWidgets()).stream()
            .flatMap(Collection::stream)
            .map(Widget::getVisualization)
            .filter(Objects::nonNull)
            .map(Visualization::getQuery)
            .collect(toList());
    }
}
