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

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.status.StatusChecker;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.alibaba.boot.dubbo.actuate.health.DubboHealthIndicatorProperties.PREFIX;
import static com.alibaba.dubbo.common.extension.ExtensionLoader.getExtensionLoader;

/**
 * Dubbo {@link HealthIndicator}
 *
 *
 * @see HealthIndicator
 * @since 1.0.0
 */
public class DubboHealthIndicator extends AbstractHealthIndicator {

    @Autowired
    private DubboHealthIndicatorProperties dubboHealthIndicatorProperties;

    @Autowired(required = false)
    private Map<String, ProtocolConfig> protocolConfigs = Collections.emptyMap();

    @Autowired(required = false)
    private Map<String, ProviderConfig> providerConfigs = Collections.emptyMap();

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        // <1> 获得 StatusChecker 对应的 Dubbo ExtensionLoader 对象
        ExtensionLoader<StatusChecker> extensionLoader = getExtensionLoader(StatusChecker.class);

        // <2> 解析 StatusChecker 的名字的 Map
        Map<String, String> statusCheckerNamesMap = resolveStatusCheckerNamesMap();

        // <3> 声明 hasError、hasUnknown 变量
        boolean hasError = false;// 是否有错误的返回

        boolean hasUnknown = false;// 是否有未知的返回

        // Up first
        // <4> 先 builder 标记状态是 UP
        builder.up();

        // <5> 遍历 statusCheckerNamesMap 元素
        for (Map.Entry<String, String> entry : statusCheckerNamesMap.entrySet()) {

            // <6.1> 获得 StatusChecker 的名字
            String statusCheckerName = entry.getKey();

            // <6.2> 获得 source
            String source = entry.getValue();

            // <6.3> 获得 StatusChecker 对象
            StatusChecker checker = extensionLoader.getExtension(statusCheckerName);

            // <6.4> 执行校验
            com.alibaba.dubbo.common.status.Status status = checker.check();

            // <7.1> 获得校验结果
            com.alibaba.dubbo.common.status.Status.Level level = status.getLevel();

            // <7.2> 如果是 ERROR 检验结果，则标记 hasError 为 true ，并标记 builder 状态为 down
            if (!hasError && level.equals(com.alibaba.dubbo.common.status.Status.Level.ERROR)) {
                hasError = true;
                builder.down();
            }

            // <7.3> 如果是 UNKNOWN 检验结果，则标记 hasUnknown 为 true ，并标记 builder 状态为 unknown
            if (!hasError && !hasUnknown && level.equals(com.alibaba.dubbo.common.status.Status.Level.UNKNOWN)) {
                hasUnknown = true;
                builder.unknown();
            }

            // <8.1> 创建 detail Map
            Map<String, Object> detail = new LinkedHashMap<>();

            // <8.2> 设置 detail 属性值
            detail.put("source", source);
            detail.put("status", status);// 校验结果

            // <8.3> 添加到 builder 中
            builder.withDetail(statusCheckerName, detail);

        }


    }

    /**
     * Resolves the map of {@link StatusChecker}'s name and its' source.
     *
     * @return non-null {@link Map}
     */
    protected Map<String, String> resolveStatusCheckerNamesMap() {

        Map<String, String> statusCheckerNamesMap = new LinkedHashMap<>();

        statusCheckerNamesMap.putAll(resolveStatusCheckerNamesMapFromDubboHealthIndicatorProperties());

        statusCheckerNamesMap.putAll(resolveStatusCheckerNamesMapFromProtocolConfigs());

        statusCheckerNamesMap.putAll(resolveStatusCheckerNamesMapFromProviderConfig());

        return statusCheckerNamesMap;

    }

    private Map<String, String> resolveStatusCheckerNamesMapFromDubboHealthIndicatorProperties() {

        DubboHealthIndicatorProperties.Status status =
                dubboHealthIndicatorProperties.getStatus();

        Map<String, String> statusCheckerNamesMap = new LinkedHashMap<>();

        for (String statusName : status.getDefaults()) {

            statusCheckerNamesMap.put(statusName, PREFIX + ".status.defaults");

        }

        for (String statusName : status.getExtras()) {

            statusCheckerNamesMap.put(statusName, PREFIX + ".status.extras");

        }

        return statusCheckerNamesMap;

    }


    private Map<String, String> resolveStatusCheckerNamesMapFromProtocolConfigs() {

        Map<String, String> statusCheckerNamesMap = new LinkedHashMap<>();

        for (Map.Entry<String, ProtocolConfig> entry : protocolConfigs.entrySet()) {

            String beanName = entry.getKey();

            ProtocolConfig protocolConfig = entry.getValue();

            Set<String> statusCheckerNames = getStatusCheckerNames(protocolConfig);

            for (String statusCheckerName : statusCheckerNames) {

                String source = buildSource(beanName, protocolConfig);

                statusCheckerNamesMap.put(statusCheckerName, source);

            }

        }

        return statusCheckerNamesMap;

    }

    private Map<String, String> resolveStatusCheckerNamesMapFromProviderConfig() {

        Map<String, String> statusCheckerNamesMap = new LinkedHashMap<>();

        for (Map.Entry<String, ProviderConfig> entry : providerConfigs.entrySet()) {

            String beanName = entry.getKey();

            ProviderConfig providerConfig = entry.getValue();

            Set<String> statusCheckerNames = getStatusCheckerNames(providerConfig);

            for (String statusCheckerName : statusCheckerNames) {

                String source = buildSource(beanName, providerConfig);

                statusCheckerNamesMap.put(statusCheckerName, source);

            }

        }

        return statusCheckerNamesMap;

    }

    private Set<String> getStatusCheckerNames(ProtocolConfig protocolConfig) {
        String status = protocolConfig.getStatus();
        return StringUtils.commaDelimitedListToSet(status);
    }

    private Set<String> getStatusCheckerNames(ProviderConfig providerConfig) {
        String status = providerConfig.getStatus();
        return StringUtils.commaDelimitedListToSet(status);
    }

    private String buildSource(String beanName, Object bean) {
        return beanName + "@" + bean.getClass().getSimpleName() + ".getStatus()";
    }

}
