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
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor;
import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import com.alibaba.dubbo.config.spring.context.annotation.DubboConfigConfiguration;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubboConfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import java.util.Set;

import static com.alibaba.boot.dubbo.util.DubboUtils.BASE_PACKAGES_PROPERTY_NAME;
import static com.alibaba.boot.dubbo.util.DubboUtils.DUBBO_PREFIX;
import static com.alibaba.boot.dubbo.util.DubboUtils.MULTIPLE_CONFIG_PROPERTY_NAME;
import static java.util.Collections.emptySet;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Dubbo Auto {@link Configuration}
 *
 * @see ApplicationConfig
 * @see Service
 * @see Reference
 * @see DubboComponentScan
 * @see EnableDubboConfig
 * @see EnableDubbo
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = DUBBO_PREFIX, name = "enabled", matchIfMissing = true, havingValue = "true")// 要求配置了 "dubbo.enabled=true" 或者，"dubbo.enabled" 未配置
@ConditionalOnClass(AbstractConfig.class)// AbstractConfig 类存在的时候，即用于判断有 Dubbo 库
public class DubboAutoConfiguration {

    /**
     * Creates {@link ServiceAnnotationBeanPostProcessor} Bean
     *
     * @param environment {@link Environment} Bean
     * @return {@link ServiceAnnotationBeanPostProcessor}
     */
    @ConditionalOnProperty(name = BASE_PACKAGES_PROPERTY_NAME)// 配置了 "dubbo.scan.base-package" 属性，即要扫描 Dubbo 注解的包
    @ConditionalOnClass(ConfigurationPropertySources.class) // 有 Spring Boot 配置加载的功能
    @Bean
    public ServiceAnnotationBeanPostProcessor serviceAnnotationBeanPostProcessor(Environment environment) {
        // <1> 获得 "dubbo.scan.base-package" 属性
        Set<String> packagesToScan = environment.getProperty(BASE_PACKAGES_PROPERTY_NAME, Set.class, emptySet());
        // <2> 创建 ServiceAnnotationBeanPostProcessor 对象
        return new ServiceAnnotationBeanPostProcessor(packagesToScan);
    }

    @ConditionalOnClass(Binder.class)// 存在 Binder 类的时候
    @Bean
    @Scope(scopeName = SCOPE_PROTOTYPE)// 多例,因为有多个 AbstractConfig 对象
    public RelaxedDubboConfigBinder relaxedDubboConfigBinder() {
        return new RelaxedDubboConfigBinder();
    }

    /**
     * Creates {@link ReferenceAnnotationBeanPostProcessor} Bean if Absent
     *
     * @return {@link ReferenceAnnotationBeanPostProcessor}
     */
    @ConditionalOnMissingBean// 不存在 ReferenceAnnotationBeanPostProcessor Bean 的时候
    @Bean(name = ReferenceAnnotationBeanPostProcessor.BEAN_NAME)// Bean 的名字是 referenceAnnotationBeanPostProcessor
    public ReferenceAnnotationBeanPostProcessor referenceAnnotationBeanPostProcessor() {
        return new ReferenceAnnotationBeanPostProcessor();
    }

    /**
     * Single Dubbo Config Configuration
     *
     * @see EnableDubboConfig
     * @see DubboConfigConfiguration.Single
     */
    @EnableDubboConfig
    protected static class SingleDubboConfigConfiguration {
    }

    /**
     * Multiple Dubbo Config Configuration , equals @EnableDubboConfig.multiple() == <code>true</code>
     *
     * @see EnableDubboConfig
     * @see DubboConfigConfiguration.Multiple
     */
    @ConditionalOnProperty(name = MULTIPLE_CONFIG_PROPERTY_NAME, havingValue = "true")// 要求配置 "dubbo.config.multiple=true" 。默认情况下，Dubbo 自带 "dubbo.config.multiple=true"
    @EnableDubboConfig(multiple = true)
    protected static class MultipleDubboConfigConfiguration {
    }

}
