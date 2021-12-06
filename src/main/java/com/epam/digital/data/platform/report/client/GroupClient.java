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

import com.epam.digital.data.platform.report.config.FeignConfig;
import com.epam.digital.data.platform.report.model.DataSourceAssociation;
import com.epam.digital.data.platform.report.model.Group;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "group", url = "${redash.url}", configuration = FeignConfig.class)
public interface GroupClient {

  @GetMapping("/api/groups")
  ResponseEntity<List<Group>> getGroups();

  @PostMapping("/api/groups")
  ResponseEntity<Group> createGroup(@RequestBody Group group);

  @DeleteMapping("/api/groups/{id}")
  ResponseEntity<Void> deleteGroup(@PathVariable("id") int id);

  @PostMapping("/api/groups/{id}/data_sources")
  ResponseEntity<Void> associateGroupWithDataSource(@PathVariable("id") int id,
      @RequestBody DataSourceAssociation dataSourceAssociation);

  @DeleteMapping("/api/groups/{groupId}/data_sources/{dataSourceId}")
  ResponseEntity<Void> deleteAssociationGroupWithDataSource(@PathVariable("groupId") int groupId,
      @PathVariable("dataSourceId") int dataSourceId);
}
