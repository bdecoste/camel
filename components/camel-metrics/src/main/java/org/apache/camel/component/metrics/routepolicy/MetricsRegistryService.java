/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.metrics.routepolicy;

import javax.management.MBeanServer;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.spi.ManagementAgent;
import org.apache.camel.support.ServiceSupport;

/**
 * Service holding the {@link MetricRegistry} which registers all metrics.
 */
public final class MetricsRegistryService extends ServiceSupport implements CamelContextAware {

    private CamelContext camelContext;
    private MetricRegistry registry;
    private JmxReporter reporter;
    private boolean useJmx;
    private String jmxDomain = "org.apache.camel.metrics";

    public MetricRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(MetricRegistry registry) {
        this.registry = registry;
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public boolean isUseJmx() {
        return useJmx;
    }

    public void setUseJmx(boolean useJmx) {
        this.useJmx = useJmx;
    }

    public String getJmxDomain() {
        return jmxDomain;
    }

    public void setJmxDomain(String jmxDomain) {
        this.jmxDomain = jmxDomain;
    }

    @Override
    protected void doStart() throws Exception {
        if (registry == null) {
            registry = new MetricRegistry();
        }

        if (useJmx) {
            ManagementAgent agent = getCamelContext().getManagementStrategy().getManagementAgent();
            if (agent != null) {
                MBeanServer server = agent.getMBeanServer();
                if (server != null) {
                    String domain = jmxDomain + "." + getCamelContext().getManagementName();
                    reporter = JmxReporter.forRegistry(registry).registerWith(server).inDomain(domain).build();
                    reporter.start();
                }
            } else {
                throw new IllegalStateException("CamelContext has not enabled JMX");
            }
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (reporter != null) {
            reporter.stop();
            reporter = null;
        }
    }
}