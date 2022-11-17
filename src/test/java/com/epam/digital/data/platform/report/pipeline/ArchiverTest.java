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

import static com.epam.digital.data.platform.report.util.TestUtils.dashboard;
import static com.epam.digital.data.platform.report.util.TestUtils.mockVoidResponse;
import static java.util.List.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.report.client.DashboardClient;
import com.epam.digital.data.platform.report.model.Dashboard;
import com.epam.digital.data.platform.report.model.Page;
import com.epam.digital.data.platform.report.service.QueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class ArchiverTest {

    @Mock
    DashboardClient dashboardClient;

    @Mock
    QueryService queryService;

    Archiver instance;

    @BeforeEach
    void init() {
        instance = new Archiver(dashboardClient, queryService);
    }

    @Test
    void shouldSuccessfullyArchiveDashboardAndQueries() {
        ResponseEntity<Page<Dashboard>> dashboardResponse = mockDashboardResponse();

        when(dashboardClient.findDashboardsByNameContainsIgnoringCase(any())).thenReturn(dashboardResponse);
        when(dashboardClient.archiveDashboard(anyInt())).thenReturn(mockVoidResponse());

        instance.archive(dashboard("stub"));

        verify(dashboardClient).findDashboardsByNameContainsIgnoringCase("stub");
        verify(dashboardClient).archiveDashboard(1);
        verify(queryService).archive(any());
    }

    private ResponseEntity<Page<Dashboard>> mockDashboardResponse() {
        Page<Dashboard> page = new Page<>();
        page.setResults(of(dashboard("stub")));

        return ResponseEntity.status(200)
            .body(page);
    }
}
