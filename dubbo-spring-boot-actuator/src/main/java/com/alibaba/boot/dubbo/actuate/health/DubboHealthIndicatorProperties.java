/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.boot.dubbo.actuate.health;

import com.alibaba.dubbo.common.status.StatusChecker;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.alibaba.boot.dubbo.actuate.health.DubboHealthIndicatorProperties.PREFIX;

/**
 * Dubbo {@link HealthIndicator} Properties
 *
 *
 * @see HealthIndicator
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = PREFIX, ignoreUnknownFields = false)// "management.health.dubbo" 开头的配置
public class DubboHealthIndicatorProperties {

    /**
     * The prefix of {@link DubboHealthIndicatorProperties}
     */
    public static final String PREFIX = "management.health.dubbo";

    private Status status = new Status();

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * The nested class for {@link StatusChecker}'s names
     * <pre>
     * registry=com.alibaba.dubbo.registry.status.RegistryStatusChecker
     * spring=com.alibaba.dubbo.config.spring.status.SpringStatusChecker
     * datasource=com.alibaba.dubbo.config.spring.status.DataSourceStatusChecker
     * memory=com.alibaba.dubbo.common.status.support.MemoryStatusChecker
     * load=com.alibaba.dubbo.common.status.support.LoadStatusChecker
     * server=com.alibaba.dubbo.rpc.protocol.dubbo.status.ServerStatusChecker
     * threadpool=com.alibaba.dubbo.rpc.protocol.dubbo.status.ThreadPoolStatusChecker
     * </pre>
     *
     * @see StatusChecker
     */
    public static class Status {

        /**
         * The defaults names of {@link StatusChecker}
         * <p>
         * The defaults : "memory", "load"
         */
        private Set<String> defaults = new LinkedHashSet<>(Arrays.asList("memory", "load"));

        /**
         * The extra names of {@link StatusChecker}
         */
        private Set<String> extras = new LinkedHashSet<>();

        public Set<String> getDefaults() {
            return defaults;
        }

        public void setDefaults(Set<String> defaults) {
            this.defaults = defaults;
        }

        public Set<String> getExtras() {
            return extras;
        }

        public void setExtras(Set<String> extras) {
            this.extras = extras;
        }
    }

}
