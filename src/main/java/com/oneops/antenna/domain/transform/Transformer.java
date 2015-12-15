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
package com.oneops.antenna.domain.transform;

import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;

import org.apache.log4j.Logger;

/**
 * Message transformer abstract class for OneOps notification events.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 * @version 1.0
 */
public abstract class Transformer {

    /**
     * Logger instance
     */
    private static Logger logger = Logger.getLogger(Transformer.class);

    /**
     * Transformer context
     */
    private Context ctx;

    /**
     * Apply transformation to the notification message. Specific transformation
     * behavior is determined by the transformer implementation and transformer
     * context settings. Subclasses would override this method to add specific
     * transformation logic.
     *
     * @param msg notification message to be transformed
     * @param ctx transformer context
     * @return transformed object
     */
    protected abstract NotificationMessage apply(NotificationMessage msg, Context ctx);

    /**
     * Transform the notification message.
     *
     * @param msg notification message
     * @return transformed message
     */
    public final NotificationMessage transform(NotificationMessage msg) {
        return apply(msg, this.ctx);
    }

    /**
     * Build a new transformer context
     *
     * @param sink Sink CI
     * @return transformer context
     */
    private static Context buildCtxFromCI(CmsCI sink) {
        String id = sink.getAttribute("mt_id").getDjValue();
        String klass = sink.getAttribute("mt_impl_class").getDjValue();
        return new Context()
                .id(id)
                .transformerClass(klass)
                .ciClassName(sink.getCiClassName());
    }

    /**
     * Factory method to instantiate the specific transformer implementation class.
     * ToDo - May be we can refactor it as separate factory class.
     *
     * @param sink transformer sink CI
     * @return newly built transformer. <code>null</code> if any error occurs.
     */
    private static Transformer newTransformer(CmsCI sink) {
        Context ctx = buildCtxFromCI(sink);
        try {
            Class<?> klass = Class.forName(ctx.transformerClass());
            if (!Transformer.class.isAssignableFrom(klass)) {
                logger.error("Transformer class " + ctx.transformerClass() + " is not of type " + Transformer.class.getCanonicalName());

            } else {
                Transformer t = (Transformer) klass.newInstance();
                t.ctx(ctx);
                return t;
            }
        } catch (Exception ex) {
            logger.error("Can't instantiate the transformer for " + sink.getCiClassName() + ", error: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Builds a new message transformer from the sink CI.
     *
     * @param sink {@link com.oneops.cms.cm.domain.CmsCI} sink CI
     * @return newly built {@Transformer}. <code>null</code> if the message
     *         transformation is not enabled or N/A.
     */
    public static Transformer fromSinkCI(CmsCI sink) {
        // For backward compatibility, check if the transformer is enabled.
        CmsCIAttribute attr = sink.getAttribute("mt_enabled");
        if (attr != null) {
            boolean mtEnabled = Boolean.valueOf(attr.getDjValue());
            if (mtEnabled) {
                return newTransformer(sink);
            }
        }
        return null;
    }

    /**
     * Accessor for transformer context
     *
     * @return transformer context
     */
    public Context ctx() {
        return this.ctx;
    }

    /**
     * Mutator for transformer context
     *
     * @param ctx transformer context
     * @return mutated object.
     */
    public Transformer ctx(final Context ctx) {
        this.ctx = ctx;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Transformer{");
        sb.append("ctx=").append(ctx);
        sb.append('}');
        return sb.toString();
    }

    /**
     * A notification message transformer context. This holds information like transformer unique id,
     * ci class, transformer class  etc.
     *
     * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
     * @version 1.0
     */
    public static class Context {

        /**
         * A unique id to represent the sink message transformer context.
         * Eg : hpom
         */
        private String id;

        /**
         * Message transformer implementation class
         */
        private String transformerClass;

        /**
         * Sink ci class name to which the tranformer is applied.
         */
        private String ciClassName;

        /* Fluent interfaces */

        public String id() {
            return this.id;
        }

        public String transformerClass() {
            return this.transformerClass;
        }

        public String ciClassName() {
            return this.ciClassName;
        }

        public Context id(final String id) {
            this.id = id;
            return this;
        }

        public Context transformerClass(final String transformerClass) {
            this.transformerClass = transformerClass;
            return this;
        }

        public Context ciClassName(final String ciClassName) {
            this.ciClassName = ciClassName;
            return this;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Context{");
            sb.append("id='").append(id).append('\'');
            sb.append(", transformerClass='").append(transformerClass).append('\'');
            sb.append(", ciClassName='").append(ciClassName).append('\'');
            sb.append('}');
            return sb.toString();
        }

    }
}
