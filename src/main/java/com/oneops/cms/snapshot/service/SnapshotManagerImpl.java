package com.oneops.cms.snapshot.service;

import com.oneops.cms.snapshot.domain.Snapshot;

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
public class SnapshotManagerImpl implements SnapshotManager {
    private SnapshotExportProcessor exportProcessor;
    private SnapshotImportProcessor importProcessor;

    public void setImportProcessor(SnapshotImportProcessor importProcessor) {
        this.importProcessor = importProcessor;
    }

    public void setExportProcessor(SnapshotExportProcessor exportProcessor) {
        this.exportProcessor = exportProcessor;
    }

    @Override
    public void importSnapshot(Snapshot snapshot) {
        
    }

    @Override
    public Snapshot exportSnapshot(String ns) {
        return exportProcessor.export(ns);
    }
}
