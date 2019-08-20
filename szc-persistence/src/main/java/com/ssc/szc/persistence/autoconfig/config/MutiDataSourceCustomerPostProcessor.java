package com.ssc.szc.persistence.autoconfig.config;

import com.ssc.szc.persistence.autoconfig.datasource.DataSourceConsumer;
import com.ssc.szc.persistence.autoconfig.mybatis.customer.ThrowableConsumer;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
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
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

import javax.sql.DataSource;
import java.util.*;

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
        return null;
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

    private void maybeCustomerSqlSessionFactory(Object bean, String beanName) {
        if(bean instanceof SqlSessionFactory) {
            String datasourceName = beanName.replace("SqlSessionFactory", "");
            logger.debug("开始对SqlSessionFactory做定制");

        }
    }

    private void customizeSqlSessionFactory(SqlSessionFactory sqlSessionFactory, String datasourceName) {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)this.beanFactory;
        //定制Interceptor
        ThrowableConsumer<Interceptor> addInterceptor = configuration::addInterceptor;

    }

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

                if(annotationMetadata != null) {   //TODO
                    Set<MethodMetadata> methodMetadataSet = annotationMetadata.getAnnotatedMethods(MYBATIS_CUSTOMER_ANNOTATION);
                    Optional<MethodMetadata> optionalMethodMetadata = methodMetadataSet.stream()
                            .filter(methodMetadata -> )
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
