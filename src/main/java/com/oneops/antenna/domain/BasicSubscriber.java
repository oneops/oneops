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
package com.oneops.antenna.domain;

import com.oneops.antenna.domain.filter.MessageFilter;
import com.oneops.antenna.domain.transform.Transformer;
import com.oneops.antenna.domain.transform.impl.HPOMTransformer;
import com.oneops.antenna.service.Dispatcher;

/**
 * Sink Subscriber base class.
 */
public class BasicSubscriber {

    /**
     * Sink subscriber name
     */
    private String name;

    /**
     * Message filter for sink
     */
    private MessageFilter filter;

    /**
     * Message transformer
     */
    private Transformer transformer;

    /**
     * Event dispatch method
     */
    private Dispatcher.Method dispatchMethod;

    /**
     * Accessor for subscriber name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Mutator for subscriber name
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Accessor for message notification filter
     *
     * @return {@link MessageFilter}
     */
    public MessageFilter getFilter() {
        return filter;
    }

    /**
     * Mutator for message notification filter
     *
     * @param {@link NotificationFilter}
     */
    public void setFilter(MessageFilter filter) {
        this.filter = filter;
    }

    /**
     * Accessor for message transformer
     *
     * @return {@Transformer}
     */
    public Transformer getTransformer() {
        return transformer;
    }

    /**
     * Mutator for message transformer
     *
     * @param transformer {@Transformer}
     */
    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Accessor for dispatch method
     *
     * @return {@DispatchMethod}
     */
    public Dispatcher.Method getDispatchMethod() {
        return dispatchMethod;
    }

    /**
     * Mutator for dispatch method
     *
     * @param dispatchMethod
     */
    public void setDispatchMethod(Dispatcher.Method dispatchMethod) {
        this.dispatchMethod = dispatchMethod;
    }

    /**
     * Checks whether any message filter has configured for this subscriber
     *
     * @return {@code true} if the notification filter is set.
     */
    public boolean hasFilter() {
        return this.filter != null;
    }

    /**
     * Checks whether any message transformer has configured for this subscriber
     *
     * @return{@code true} if the notification message transformer is set.
     */
    public boolean hasTransformer() {
        return this.transformer != null;
    }

    /**
     * Checks if the subscriber has HPOM transformer
     *
     * @return {@code true} if the subscriber has a transformer and of type {@code HPOMTransformer}
     */
    public boolean hasHpomXfmr() {
        return HPOMTransformer.class.isInstance(transformer);
    }

    /**
     * Checks whether the message dispatching is asynchronous for this subscriber
     *
     * @return{@code true} if the dispatch method is async.
     */
    public boolean isAsync() {
        return Dispatcher.Method.ASYNC == this.dispatchMethod;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MsgSubscriber{");
        sb.append("name='").append(name).append('\'');
        sb.append(", filter=").append(hasFilter());
        sb.append(", transformer=").append(hasTransformer());
        sb.append(", async=").append(isAsync());
        sb.append('}');
        return sb.toString();
    }
}
