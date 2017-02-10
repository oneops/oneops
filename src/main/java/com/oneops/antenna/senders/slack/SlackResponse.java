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
package com.oneops.antenna.senders.slack;

/**
 * Slack web api response.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 * @see <a href="https://api.slack.com/web">Slack Web Api response</a>
 */
public class SlackResponse {

    private boolean ok;
    private String stuff;
    private String error;
    private String warning;

    public boolean isOk() {
        return ok;
    }

    public String getStuff() {
        return stuff;
    }

    public String getWarning() {
        return warning;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "SlackResponse{" +
                "ok=" + ok +
                ", stuff='" + stuff + '\'' +
                ", warning='" + warning + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
