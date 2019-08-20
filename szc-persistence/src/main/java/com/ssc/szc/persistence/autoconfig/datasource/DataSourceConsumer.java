package com.ssc.szc.persistence.autoconfig.datasource;

import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * 支持第三方数据源定制，根据特定的数据源实现进行定制
 *
 * @author Lebonheur
 */
public interface DataSourceConsumer<T extends DataSource> extends Ordered {

    Class<T> getDataSourceClass();

    void customDataSource(Environment environment, Object bean, String beanName);

    default void custom(Environment environment, Object bean, String beanName) {
        Class<?> clazz = bean.getClass();
        if(getDataSourceClass().isAssignableFrom(clazz)) {
            T dataSource = getDataSourceClass().cast(bean);
            customDataSource(environment, dataSource, beanName);
        }
    }


}
