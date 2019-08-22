package com.ssc.szc.persistence.autoconfig.datasource;

import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * @author Lebonheur
 */
public class TomcatDataSourceCustomer implements DataSourceConsumer<DataSource> {
    @Override
    public Class<DataSource> getDataSourceClass() {
        return null;
    }

    @Override
    public void customDataSource(Environment environment, Object bean, String beanName) {

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
