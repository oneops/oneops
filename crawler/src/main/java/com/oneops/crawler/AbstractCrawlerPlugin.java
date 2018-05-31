/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
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
package com.oneops.crawler;

import com.oneops.Deployment;
import com.oneops.Environment;
import com.oneops.Organization;

import java.util.List;
import java.util.Map;

public abstract class AbstractCrawlerPlugin {
    public void processEnvironment(Environment env, List<Deployment> deployments, Map<String, Organization> organizations) {
        //default empty impl
    }
    public void processEnvironment(Environment env, Map<String, Organization> organizations) {
        //default empty impl
    }
    public void init() {
        //default empty impl
    }
    public void cleanup() {
        //default empty impl
    }
}


