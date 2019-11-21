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
package com.alibaba.boot.dubbo.autoconfigure;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.spring.context.properties.AbstractDubboConfigBinder;
import com.alibaba.dubbo.config.spring.context.properties.DubboConfigBinder;

import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.UnboundElementsSourceFilter;
import org.springframework.core.env.PropertySource;

import static org.springframework.boot.context.properties.source.ConfigurationPropertySources.from;

/**
 *
 * 负责将 Spring Boot 的配置属性，注入到 Dubbo AbstractConfig 配置对象中
 *
 * Spring Boot Relaxed {@link DubboConfigBinder} implementation
 * see org.springframework.boot.context.properties.ConfigurationPropertiesBinder
 *
 * @since 0.1.1
 */
public class RelaxedDubboConfigBinder extends AbstractDubboConfigBinder {

    @Override
    public <C extends AbstractConfig> void bind(String prefix, C dubboConfig) {
        // <1.1> 获得 PropertySource 数组
        Iterable<PropertySource<?>> propertySources = getPropertySources();

        // Converts ConfigurationPropertySources
        // <1.2> 转换成 ConfigurationPropertySource 数组
        Iterable<ConfigurationPropertySource> configurationPropertySources = from(propertySources);

        // Wrap Bindable from DubboConfig instance
        // <2> 将 dubboConfig 包装成 Bindable 对象
        Bindable<C> bindable = Bindable.ofInstance(dubboConfig);
        // <3.1> 创建 Binder 对象
        Binder binder = new Binder(configurationPropertySources, new PropertySourcesPlaceholdersResolver(propertySources));

        // Get BindHandler
        // <3.2> 获得 BindHandler 对象
        BindHandler bindHandler = getBindHandler();

        // Bind
        // <3.3> 执行绑定，会将 propertySources 属性，注入到 dubboConfig 对象中
        binder.bind(prefix, bindable, bindHandler);

    }

    private BindHandler getBindHandler() {
        // 获得默认的 BindHandler 处理器
        BindHandler handler = BindHandler.DEFAULT;
        // 进一步包装成 IgnoreErrorsBindHandler 对象
        if (isIgnoreInvalidFields()) {
            handler = new IgnoreErrorsBindHandler(handler);
        }
        // 进一步包装成 NoUnboundElementsBindHandler 对象
        if (!isIgnoreUnknownFields()) {
            UnboundElementsSourceFilter filter = new UnboundElementsSourceFilter();
            handler = new NoUnboundElementsBindHandler(handler, filter);
        }
        return handler;
    }
}
