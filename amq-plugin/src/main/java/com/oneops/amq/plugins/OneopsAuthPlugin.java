/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
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
package com.oneops.amq.plugins;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;

/**
 * The Class OneopsAuthPlugin.
 */
public class OneopsAuthPlugin implements BrokerPlugin {

    private CMSClient cms;

    /* (non-Javadoc)
     * @see org.apache.activemq.broker.BrokerPlugin#installPlugin(org.apache.activemq.broker.Broker)
     */
    @Override
    public Broker installPlugin(Broker broker) throws Exception {
        return new OneopsAuthBroker(broker, cms);
    }

    /**
     * Sets the cms.
     *
     * @param cms the new cms
     */
    public void setCms(CMSClient cms) {
        this.cms = cms;
    }

}
