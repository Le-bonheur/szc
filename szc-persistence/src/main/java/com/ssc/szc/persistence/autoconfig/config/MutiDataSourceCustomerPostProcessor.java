package com.ssc.szc.persistence.autoconfig.config;

import com.ssc.szc.persistence.autoconfig.datasource.DataSourceConsumer;
import com.ssc.szc.persistence.autoconfig.mybatis.customer.ThrowableConsumer;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ssc.szc.persistence.autoconfig.mybatis.customer.annotation.CustomerBeanBindTo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.MultiValueMap;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

import static com.ssc.szc.commons.util.ObjectUtils.isNotEmpty;

/**
 * 提供对数据源相关的bean的后期配置支持，包括mybatis的拦截器，数据源的定制等
 *
 * @author Lebonheur
 */
public class MutiDataSourceCustomerPostProcessor implements BeanPostProcessor, Ordered {

    private static final String MYBATIS_CUSTOMER_ANNOTATION = CustomerBeanBindTo.class.getName();

    private static Logger logger = LoggerFactory.getLogger(MutiDataSourceCustomerPostProcessor.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private Environment environment;

    @Autowired
    private ObjectProvider<List<DataSourceConsumer<?>>> datasourceCustomerListProvider;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        maybeCustomerDataSource(bean, beanName);
        maybeCustomerSqlSessionFactory(bean, beanName);
        return bean;
    }


    /**
     * 对数据源进行定制
     * 从spring容器中获取所有类型为DataSourceCustomer的bean，按照优先级进行排序，接着对bean进行定制
     * @param bean 定制的bean
     * @param beanName bean再Spring容器中的beanName
     */
    private void maybeCustomerDataSource(Object bean, String beanName) {
        if(bean instanceof DataSource) {
            List<DataSourceConsumer<?>> dataSourceConsumers = datasourceCustomerListProvider.getIfAvailable();
            assert dataSourceConsumers != null;

            dataSourceConsumers.sort(Comparator.comparingInt(Ordered::getOrder));

            for (DataSourceConsumer<?> dataSourceConsumer : dataSourceConsumers) {
                dataSourceConsumer.custom(environment, bean, beanName);
            }
        }
    }

    /**
     * 对SqlSessionFactory进行定制
     * 如果是SqlSessionFactory的实例，那么进行定制，在这里约定只对我们模块生成的SqlSessionFactory进行定制
     * 我们生成的SqlSessionFactory以SqlSessionFactory为前缀，获得得到数据源名称，进一步调用方法
     * customizeSqlSessionFactory进行定制
     * @param bean 可能需要定制的bean
     * @param beanName bean再spring容器中的beanName
     */
    private void maybeCustomerSqlSessionFactory(Object bean, String beanName) {
        if(bean instanceof SqlSessionFactory) {
            String datasourceName = beanName.replace("SqlSessionFactory", "");
            logger.debug("开始对SqlSessionFactory做定制");
            customizeSqlSessionFactory((SqlSessionFactory)bean, datasourceName);
        }
    }

    /**
     * 定制SqlSessionFactory
     * 按照如下顺序定制：Interceptor、DatabaseIdProvider, ConfigurationCustomer
     * 真正定制看doCustomer方法
     * @param sqlSessionFactory sqlSessionFactory实例
     * @param datasourceName 数据源名称
     */
    private void customizeSqlSessionFactory(SqlSessionFactory sqlSessionFactory, String datasourceName) {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)this.beanFactory;
        //定制Interceptor
        ThrowableConsumer<Interceptor> addInterceptor = configuration::addInterceptor;
        doCustomer(datasourceName, beanFactory, Interceptor.class, addInterceptor);
        //定制DatabaseIdProvider
        DataSource dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        ThrowableConsumer<DatabaseIdProvider> setDatabaseId = databaseIdProvider -> {
            try {
                String databaseId = databaseIdProvider.getDatabaseId(dataSource);
                configuration.setDatabaseId(databaseId);
            } catch (SQLException e) {
                logger.error("从给定的DatabaseIdProvider得不到database id.");
                throw new RuntimeException("从给定的DatabaseIdProvider得不到database id.", e);
            }
        };
        doCustomer(datasourceName, beanFactory, DatabaseIdProvider.class, setDatabaseId);
        ThrowableConsumer<ConfigurationCustomizer> customizerConsumer =
                configurationCustomizer -> configurationCustomizer.customize(configuration);
        doCustomer(datasourceName, beanFactory, ConfigurationCustomizer.class, customizerConsumer);
    }

    /**
     * 对给定的可定制对象进行定制
     * 根据给定的类型，从bean工厂取得所有该类型可定制对象
     * 然后获得关于该bean的BeanDefinition，这里我们需要判断是通过注解注入的BeanDefinition
     * 然后从该注解BeanDefinition得到我们设定的注解元信息，看@CustomerBeanBindTo
     * 然后找到匹配我们当前正在处理的Bean，就是说这个MethodMetadata构造出来的bean的beanName跟我们传入的beanName是一致的
     * 如果找得到，那么我们就从中获取CustomerBeanBindTo这个注解的属性，判断它value属性是否包含了传入datasourceName
     * 如果是有包含，那么表明我们需要对这个datasource以及它相关的对象进行定制，具体定制由参数customerConsumer传入
     * @param datasourceName 数据源
     * @param beanFactory bean工厂
     * @param clazz 定制对象的类
     * @param customConsumer 定制具体操作
     * @param <T> 定制类型
     */
    private <T> void doCustomer(String datasourceName,
                                DefaultListableBeanFactory beanFactory,
                                Class<T> clazz,
                                ThrowableConsumer<T> customConsumer) {
        Map<String, T> customerObjectsMap = applicationContext.getBeansOfType(clazz);
        Set<Map.Entry<String, T>> entrySet = customerObjectsMap.entrySet();
        for (Map.Entry<String, T> entry : entrySet) {
            String beanName = entry.getKey();
            T customerObject = entry.getValue();

            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            //条件：该bean是通过注解注入
            if(AnnotatedBeanDefinition.class.isAssignableFrom(beanDefinition.getClass())) {
                AnnotatedBeanDefinition definition = (AnnotatedBeanDefinition)beanDefinition;
                AnnotationMetadata annotationMetadata = definition.getMetadata();

                if(isNotEmpty(annotationMetadata)) {
                    Set<MethodMetadata> methodMetadataSet = annotationMetadata.getAnnotatedMethods(MYBATIS_CUSTOMER_ANNOTATION);
                    Optional<MethodMetadata> optionalMethodMetadata = methodMetadataSet.stream()
                            .filter(methodMetadata -> isMethodMetaDataMatchBeanName(beanName, methodMetadata))
                            .findAny();
                    if(optionalMethodMetadata.isPresent()) {
                        MethodMetadata methodMetadata = optionalMethodMetadata.get();
                        AnnotationAttributes annotationAttributes = AnnotationAttributes
                                .fromMap(methodMetadata.getAnnotationAttributes(CustomerBeanBindTo.class.getName(), false));
                        String[] dataSources = Objects.requireNonNull(annotationAttributes).getStringArray("value");
                        if(Arrays.asList(dataSources).contains(datasourceName)) {
                            customConsumer.accept(customerObject);
                        }
                    }
                }
            }
        }
    }

    /**
     * 判断MethodMetadata是否跟给定的beanName对应的bean是匹配的
     * @param beanName bean再spring中的名称
     * @param methodMetadata 构造bean时Method的元信息
     * @return 是否匹配
     */
    private boolean isMethodMetaDataMatchBeanName(String beanName, MethodMetadata methodMetadata) {
        MultiValueMap<String, Object> beanAnnotationValueMap =
                methodMetadata.getAllAnnotationAttributes(Bean.class.getName());
        if(isNotEmpty(beanAnnotationValueMap)) {
            String[] beanNames = (String[])beanAnnotationValueMap.getFirst("value");
            if(isNotEmpty(beanNames)) {
                return Arrays.asList(beanNames).contains(beanName);
            } else {
                return methodMetadata.getMethodName().equalsIgnoreCase(beanName);
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
