package com.ssc.szc.persistence.autoconfig;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Collections;
import java.util.Map;

/**
 * @author Lebonheur
 */
public class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        String propertySourceName = "szcFrameworkAutoconfigExcludes";
        String toBeExcludes = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration";
        if(!environment.getProperty("szc.enablePersistence", Boolean.class, Boolean.TRUE)) {
            toBeExcludes = "ssc.szc.persistence.autoconfig.PersistenceAutoConfiguration";
        }

        Map<String, Object> properties = Collections.singletonMap("spring.autoconfigure.exclude", toBeExcludes);
        MapPropertySource mapPropertySource = new MapPropertySource(propertySourceName, properties);

        mutablePropertySources.addLast(mapPropertySource);

    }
}
