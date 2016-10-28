package com.oneops.cms.ws.rest;

import com.oneops.cms.snapshot.domain.Snapshot;
import com.oneops.cms.snapshot.service.SnapshotManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
@Controller
public class SnapshotRestController extends AbstractRestController {
    private SnapshotManager snapshotManager;


    public void setSnapshotManager(SnapshotManager snapshotManager) {
        this.snapshotManager = snapshotManager;
    }

    @RequestMapping(value = "/snapshot/export", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Snapshot exportSnapshot(@RequestParam(value = "ns") String namespace,
                                   @RequestHeader(value = "X-Cms-Scope", required = false) String scope) {

        return snapshotManager.exportSnapshot(namespace);
    }


    @RequestMapping(value = "/snapshot/import", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> importSnapshot(
            @RequestBody Snapshot snapshot,
            @RequestHeader(value = "X-Cms-User", required = false) String userId,
            @RequestHeader(value = "X-Cms-Scope", required = false) String scope) {

        if (userId == null) userId = "oneops-system";
        snapshotManager.importSnapshot(snapshot);
        Map<String, String> result = new HashMap<>(1);
        result.put("result", "success");
        return result;
    }
}
