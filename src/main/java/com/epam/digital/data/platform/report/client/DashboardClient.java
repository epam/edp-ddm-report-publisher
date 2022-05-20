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

package com.epam.digital.data.platform.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.epam.digital.data.platform.report.config.FeignConfig;
import com.epam.digital.data.platform.report.model.Dashboard;
import com.epam.digital.data.platform.report.model.Page;

@FeignClient(name = "dashboard", url = "${redash.url}", configuration = FeignConfig.class)
public interface DashboardClient {

    @PostMapping("/api/dashboards")
    ResponseEntity<Dashboard> createDashboard(@RequestBody Dashboard dashboard);

    @PostMapping("/api/dashboards/{id}")
    ResponseEntity<Dashboard> updateDashboard(@PathVariable("id") int id,
        @RequestBody Dashboard dashboard);

    @GetMapping("/api/dashboards")
    ResponseEntity<Page<Dashboard>> findDashboardsByNameContainsIgnoringCase(@RequestParam("q") String name);

    @GetMapping("/api/dashboards/{slug}")
    ResponseEntity<Dashboard> getDashboardBySlug(@PathVariable("slug") String slug);

    @DeleteMapping("/api/dashboards/{slug}")
    ResponseEntity<Void> archiveDashboard(@PathVariable("slug") String slug);
}
