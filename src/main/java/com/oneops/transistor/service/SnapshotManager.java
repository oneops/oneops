package com.oneops.transistor.service;

import com.oneops.cms.exceptions.DJException;
import com.oneops.transistor.snapshot.domain.Snapshot;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

@Transactional
public interface SnapshotManager {
    @Transactional(noRollbackFor = DJException.class)
    List<String> importSnapshotAndReplayTo(Snapshot snapshot, Long releaseId);
    List<String> importSnapshot(Snapshot snapshot);

    List<String> replay(long fromReleaseId, long toReleaseId, String nsPath);
    Snapshot exportSnapshot(String[] namespaces, String[] classNames, Boolean[] recursive);
}
