package com.ssc.szc.persistence.autoconfig.config;

import com.ssc.szc.commons.util.ObjectUtils;
import com.ssc.szc.persistence.autoconfig.property.MultiDataSourcePersistenceProperties;
import com.ssc.szc.persistence.autoconfig.property.PersistenceProperties;
import com.ssc.szc.persistence.autoconfig.util.PersistencePropertyUtil;
import org.apache.ibatis.logging.log4j.Log4jImpl;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

import static com.ssc.szc.commons.util.BeanNameGeneratorUtil.generateSqlSessionFactoryBeanName;
import static com.ssc.szc.commons.util.ObjectUtils.isAnyEmpty;
import static com.ssc.szc.commons.util.ObjectUtils.isNotEmpty;

/**
 * 多数据源集成自动配置
 * 提供通用配置，对于个性化需求，提供可扩展方式给用户定制
 * 从Environment获取前缀bee.persistence开头的配置，以数据源名称做进一步的配置
 * @author Lebonheur
 */
public class MultiDataSourceModePropertiesConfiguration implements ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {

    public static String CONFIG_PREGIS = "bee.persistence";

    private Environment environment;

    private ResourceLoader resourceLoader;

    private static Logger logger = LoggerFactory.getLogger(MultiDataSourceModePropertiesConfiguration.class);

    private String primaryDataSourceName;

    private ThreadLocal<Map<String, Pair>> mapperConfigCache = new ThreadLocal<>();

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 该方法继承自ImportBeanDefinitionRegistrar，主要作用就是解析配置，并且注册相关持久化相关的bean
     * @param annotationMetadata 被注释到的bean
     * @param beanDefinitionRegistry spring容器相关BeanDefinitionRegistry注册器，用于注册Bean
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
                                        BeanDefinitionRegistry beanDefinitionRegistry) {
        if(isEnabled()) {
            MultiDataSourcePersistenceProperties multiDataSourcePersistenceProperties = parsePersistenceConfig();
            if(multiDataSourcePersistenceProperties != null) {

            }
        }
    }

    private void generatePersistenceBeanDefinitionAndRegister(MultiDataSourcePersistenceProperties multiDataSourcePersistenceProperties,
                                                              BeanDefinitionRegistry registry) {
        Map<String, PersistenceProperties> datasourcePropertiesMap = multiDataSourcePersistenceProperties.getPropertiesMap();
        Set<String> datasourceNames = datasourcePropertiesMap.keySet();
        for(String datasourceName : datasourceNames) {
            PersistenceProperties persistenceProperties = datasourcePropertiesMap.get(datasourceName);
            DataSourceProperties dataSourceProperties = persistenceProperties.getDataSource();
            MybatisProperties mybatisProperties = persistenceProperties.getMybatis();
            if(isAnyEmpty(dataSourceProperties, mybatisProperties)) {
                break;
            }
            registerDatasource(registry, dataSourceProperties, datasourceName);
            registerSqlSessionFactory(registry, datasourceName, mybatisProperties);
        }
    }

    /**
     * 注册SqlSessionFactory
     * @param registry Bean定义注册器
     * @param datasourceName 数据源名称
     * @param mybatisProperties mybatis配置
     */
    private void registerSqlSessionFactory(BeanDefinitionRegistry registry,
                                           String datasourceName,
                                           MybatisProperties mybatisProperties) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactoryBean.class);
        beanDefinitionBuilder.addPropertyReference("dataSource", datasourceName);
        beanDefinitionBuilder.addPropertyValue("vfs", SpringBootVFS.class);
        if(StringUtils.hasText(mybatisProperties.getConfigLocation())) {
            beanDefinitionBuilder.addPropertyValue("configLocation",
                    this.resourceLoader.getResource(mybatisProperties.getConfigLocation()));
        }
        Configuration configuration = mybatisProperties.getConfiguration();
        if(ObjectUtils.isEmpty(configuration) && !StringUtils.hasText(mybatisProperties.getConfigLocation())) {
            configuration = new Configuration();
        }
        if(isNotEmpty(configuration) && ObjectUtils.isEmpty(configuration.getLogImpl())) {
            configuration.setLogImpl(Log4jImpl.class);
        }
        beanDefinitionBuilder.addPropertyValue("configuration", configuration);
        if(isNotEmpty(mybatisProperties.getConfigurationProperties())) {
            beanDefinitionBuilder.addPropertyValue("configurationProperties", mybatisProperties.getConfigurationProperties());
        }
        if(StringUtils.hasLength(mybatisProperties.getTypeAliasesPackage())) {
            beanDefinitionBuilder.addPropertyValue("typeAliasesPackage", mybatisProperties.getTypeAliasesPackage());
        }
        if(StringUtils.hasLength(mybatisProperties.getTypeHandlersPackage())) {
            beanDefinitionBuilder.addPropertyValue("typeHandlersPackage", mybatisProperties.getTypeHandlersPackage());
        }
        if(isNotEmpty(mybatisProperties.resolveMapperLocations())) {
            beanDefinitionBuilder.addPropertyValue("mapperLocations", mybatisProperties.resolveMapperLocations());
        }
        beanDefinitionBuilder.addDependsOn(datasourceName);
        doRegisterBean(registry, generateSqlSessionFactoryBeanName(datasourceName), beanDefinitionBuilder);
    }

    /**
     * 注册数据源
     * @param registry BeanDefinitionRegistry Bean注册器
     * @param dataSourceProperties 数据源配置
     * @param datasourceName 数据源名称
     */
    private void registerDatasource(BeanDefinitionRegistry registry,
                                    DataSourceProperties dataSourceProperties,
                                    String datasourceName) {
        Class<? extends DataSource> datasourceClass = DataSourceBuilder.findType(dataSourceProperties.getClassLoader());
        if(isNotEmpty(dataSourceProperties.getType())) {
            datasourceClass = dataSourceProperties.getType();
        }
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(datasourceClass);
        beanDefinitionBuilder.addPropertyValue("url", dataSourceProperties.getUrl());
        beanDefinitionBuilder.addPropertyValue("username", dataSourceProperties.getUsername());
        beanDefinitionBuilder.addPropertyValue("password", dataSourceProperties.getPassword());
        beanDefinitionBuilder.addPropertyValue("driverClassName", dataSourceProperties.getDriverClassName());
        doRegisterBean(registry, datasourceName, beanDefinitionBuilder);
    }

    private void doRegisterBean(BeanDefinitionRegistry registry,
                                String datasourceName,
                                BeanDefinitionBuilder beanDefinitionBuilder) {
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        if(datasourceName.equals(primaryDataSourceName)) {
            beanDefinition.setPrimary(true);
        }
        registry.registerBeanDefinition(datasourceName, beanDefinition);
    }

    /**
     * 从spring boot指定的application.yml以及application.properties解析多数据源及mybatis相关配置
     * 从CONFIG_PREGIS代表的前缀开始后接数据源名称，接若以datasource开始的数据源的配置，以及mybatis开始的
     * 对mybatis的配置
     * @return MultiDataSourcePersistenceProperties
     */
    @SuppressWarnings("unchecked")
    private MultiDataSourcePersistenceProperties parsePersistenceConfig() {

        Map<String, Map<String,Object>> properties = PersistencePropertyUtil.get(environment);
        if(properties == null || properties.isEmpty()) {
            logger.info("{}为前缀的配置解析得不到相关配置", CONFIG_PREGIS);
            return null;
        }
        if(properties.size() == 1) {
            primaryDataSourceName = properties.keySet().toArray(new String[]{})[0];
        }
        MultiDataSourcePersistenceProperties multiDataSourcePersistenceProperties = new MultiDataSourcePersistenceProperties();
        try {
            for(Map.Entry<String, Map<String, Object>> entry : properties.entrySet()) {
                String datasourceName = entry.getKey();
                maybeSetPrimaryDataSourceName(datasourceName);

                DataSourceProperties dsp = new DataSourceProperties();
                Map<String, Object> dataSourceProperties = (Map<String, Object>)entry.getValue().get("datasource");
                //TODO
                if(dataSourceProperties != null && !dataSourceProperties.isEmpty()) {
                    ConfigurationPropertySource configurationPropertySource = new MapConfigurationPropertySource(dataSourceProperties);
                    Binder binder = new Binder(configurationPropertySource);
                    binder.bind("", Bindable.ofInstance(dsp));
                }
                //TODO
                MybatisProperties mp = new MybatisProperties();
                Map<String, Object> mybatisProperties = (Map<String, Object>)entry.getValue().get("mybatis");
                if(mybatisProperties != null && !mybatisProperties.isEmpty()) {
                    ConfigurationPropertySource configurationPropertySource = new MapConfigurationPropertySource(mybatisProperties);
                    Binder binder = new Binder(configurationPropertySource);
                    binder.bind("", Bindable.ofInstance(mp));
                }

                PersistenceProperties pp = new PersistenceProperties();
                pp.setDataSource(dsp);
                pp.setMybatis(mp);
                multiDataSourcePersistenceProperties.getPropertiesMap().put(datasourceName, pp);
            }
        } catch (IllegalStateException e) {
            logger.error("从配置文件解析数据源及mybatis相关配置到bean时失败", e);
            throw e;
        }
        return multiDataSourcePersistenceProperties;
    }

    private void maybeSetPrimaryDataSourceName(String datasourceName) {
        if(StringUtils.hasText(primaryDataSourceName)) {
            return;
        }
        String datasourcePrefix = String.format("%s.%s.datasource.", CONFIG_PREGIS, datasourceName);
        boolean isPrimary = environment.getProperty(datasourcePrefix + "isPrimary", Boolean.class, Boolean.FALSE);
        if(isNotEmpty(isPrimary) && isPrimary) {
            primaryDataSourceName = datasourceName;
        }
    }

    /**
     * 是否启动本模块功能
     * 如果`szc.persistence.multi`配置为false，则不启用本模块
     * 如果`szc.persistence.multi`配置为true，或者默认，那么启动本模块
     * @return 是否启动本模块功能
     */
    private boolean isEnabled() {
        return environment.getProperty("szc.persistence.multi", Boolean.class, Boolean.TRUE);
    }

    static class Pair {
        Set<String> namespacesToScan;
        Set<String> mapperInterfaces;
        static Pair valueOf(Set<String> namespacesToScan, Set<String> mapperInterfaces) {
            Pair pair = new Pair();
            pair.namespacesToScan = namespacesToScan;
            pair.mapperInterfaces = mapperInterfaces;
            return pair;
        }
    }

}
