package com.ssc.szc.persistence.autoconfig.config;

import com.ssc.szc.commons.util.ObjectUtils;
import com.ssc.szc.persistence.autoconfig.datasource.TomcatDataSourceCustomer;
import com.ssc.szc.persistence.autoconfig.mybatis.xml.parse.*;
import com.ssc.szc.persistence.autoconfig.property.MultiDataSourcePersistenceProperties;
import com.ssc.szc.persistence.autoconfig.property.PersistenceProperties;
import com.ssc.szc.persistence.autoconfig.util.PersistencePropertyUtil;
import org.apache.ibatis.logging.log4j.Log4jImpl;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

import static com.ssc.szc.commons.string.StringUtils.ifHasTextThen;
import static com.ssc.szc.commons.util.BeanNameGeneratorUtil.*;
import static com.ssc.szc.commons.util.ObjectUtils.*;

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
                generatePersistenceBeanDefinitionAndRegister(multiDataSourcePersistenceProperties, beanDefinitionRegistry);
            } else {
                logger.info("从配置文件解析数据源及mybatis配置你系为空。");
            }
        } else {
            logger.info("多数据源功能被关闭，如要使用，请通过配置szc.persistence.multi设置为true，或者直接去掉该配置");
        }
    }

    /**
     * 对相关bean进行注册，生成BeanDefinition注册到BeanDefinitionRegistry，并指定相互依赖
     * 这些Bean以组为单位，每一组代表mybatis操作mapper需要的所有Bean，有：
     * DataSource SqlSessionFactory SqlSessionTemplate TransactionManager以及各个mapper
     *
     * @param multiDataSourcePersistenceProperties 数据源及mybatis相关配置
     * @param registry BeanDefinitionRegistry
     */
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
            registerSqlSessionTemplate(registry, datasourceName);
            registerTransactionManager(registry, datasourceName);
            registerMappers(registry, datasourceName, mybatisProperties);
        }
        registerDatasourceCustomerBean(registry);
        //清理mapper配置文件缓存
        mapperConfigCache.remove();
    }

    private void registerDatasourceCustomerBean(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TomcatDataSourceCustomer.class);
        registry.registerBeanDefinition("szcTomcatDataSourceCustomer", beanDefinitionBuilder.getBeanDefinition());
    }

    /**
     * 注册mappers到spring
     * 如果对某一个数据源不想自动注册其关联的mybatis对应mapper.xml中对应mapper到spring容器中，那么可以使用配置项
     * `szc.persistence.datasourceName.datasource.disableRegisterMappers`设置为false
     * @see ClassPathMapperScanner
     * @param registry Bean定义注册器
     * @param datasourceName 数据源名称
     * @param mybatisProperties mybatis配置
     */
    private void registerMappers(BeanDefinitionRegistry registry,
                                 String datasourceName,
                                 MybatisProperties mybatisProperties) {
        String enableRegisterMappersKey = String.format("%s.%s.datasource.disableRegisterMappers", CONFIG_PREGIS, datasourceName);
        if(environment.getProperty(enableRegisterMappersKey, Boolean.class, Boolean.FALSE)) {
            return;
        }
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        scanner.setBeanNameGenerator(((definition, re) -> {
            String className = definition.getBeanClassName();
            String interfaceName = Objects.requireNonNull(className).replaceAll("([^.]+\\.)*", "");
            return datasourceName + interfaceName;
        }));
        scanner.setResourceLoader(resourceLoader);
        scanner.setSqlSessionTemplateBeanName(generateSqlSessionTemplateBeanName(datasourceName));
        Set<String> packagesToBeScan = new HashSet<>();
        Set<String> mapperInterfaces = extractMapperClasses(mybatisProperties, packagesToBeScan);
        try {
            doRegisterMappers(scanner, packagesToBeScan, mapperInterfaces);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Mapper对象加载失败，DatasourceName:" + datasourceName, e);
        }
    }

    private void doRegisterMappers(ClassPathMapperScanner scanner,
                                   Set<String> packagesToBeScan,
                                   Set<String> mapperInterfaces) {
        scanner.resetFilters(false);
        for (String mapperInterface : mapperInterfaces) {
            try {
                scanner.addIncludeFilter(new AssignableTypeFilter(Class.forName(mapperInterface)));
            } catch (ClassNotFoundException e) {
                logger.error("mybatis配置文件配置的mapper找不到对应的类/接口");
                throw new RuntimeException("mybatis配置文件配置的mapper找不到对应的类/接口", e);
            }
        }
        scanner.doScan(StringUtils.toStringArray(packagesToBeScan));
    }

    /**
     * 从配置文件抽取mybatis配置信息
     * 1、SpringBoot的配置文件中抽取mapper的xml配置文件路径
     * 2、从mybatis配置文件中抽取mapper节点，解析mapper节点中resource、url得到Resource并进一步解析mapper xml配置文件
     * 3、从mybatis配置文件中抽取mapper节点，解析mapper节点中package name、classes，进一步解析mapper扫描信息
     *
     * @param mybatisProperties mybatis配置属性对象
     * @param packagesToBeScan 要扫描的对象
     * @return 接口集合
     */
    private Set<String> extractMapperClasses(MybatisProperties mybatisProperties,
                                             Set<String> packagesToBeScan) {
        Set<Resource> resources = new HashSet<>();
        Set<String> mapperInterfaces = new HashSet<>();
        if(isNotEmpty(mybatisProperties.getMapperLocations())) {
            Resource[] resourceArray = resolveMapperLocations(mybatisProperties.getMapperLocations());
            ifNotEmptyThen(resourceArray, () -> resources.addAll(Arrays.asList(resourceArray)));
        }
        parseMapperXMLsFromMybatisConfigFile(mybatisProperties, mapperInterfaces, resources, packagesToBeScan);
        if(isNotEmpty(resources)) {
            Set<String> namespacesToBeScan = extractMappersFromSpringBootConfigFile(resources, mapperInterfaces);
            packagesToBeScan.addAll(namespacesToBeScan);
        }
        return mapperInterfaces;
    }

    /**
     * 从resources列表中抽取namespace和接口名
     * @param resources 资源列表
     * @param mapperInterfaces 需要扫描的接口集合
     * @return 返回namespace集合
     */
    private Set<String> extractMappersFromSpringBootConfigFile(Set<Resource> resources,
                                                               Set<String> mapperInterfaces) {
        Map<String, Pair> cache = mapperConfigCache.get();
        if(cache == null) {
            cache = new HashMap<>();
            mapperConfigCache.set(cache);
        }

        Set<String> namespacesToScan = new HashSet<>();
        Set<String> mapperInterfacesInternal = new HashSet<>();
        try {
            for (Resource resource : resources) {
                Pair pair = cache.get(resource.getURI().toString());
                if(pair == null) {
                    MybatisMapperXmlNamespaceParserHandler parserHandler = new MybatisMapperXmlNamespaceParserHandler();
                    MybatisXMLParserHandler.extractXMLResource(resource, parserHandler);
                    String interfaceName = parserHandler.getResult();
                    ifNotEmptyThen(interfaceName, () -> {
                        String packageName = interfaceName.replaceAll("(\\.[^.]+)(?!.)", "");
                        ifHasTextThen(packageName, namespacesToScan::add);
                        ifHasTextThen(interfaceName, mapperInterfacesInternal::add);
                    });
                    pair = Pair.valueOf(new HashSet<>(namespacesToScan), new HashSet<>(mapperInterfacesInternal));
                    cache.put(resource.getURI().toString(), pair);
                }
                namespacesToScan.addAll(pair.namespacesToScan);
                mapperInterfaces.addAll(pair.mapperInterfaces);
            }
        } catch (IOException e) {
            logger.error("加载、解析Mybatis Mapper路径失败.");
            throw new RuntimeException("加载、解析Mybatis Mapper路径失败.", e);
        }
        return namespacesToScan;
    }

    /**
     * 从mybatis配置文件中解析mappers这个元素
     *
     * mappers有两种元素：
     * 一种是package
     * 另一种是mapper元素，mapper有三种形式：resource、class、url
     * resource指定xml文件，class指定接口（这个时候对应的xml文件与其同名），url指定mapper文件位置
     * package则是class情况的扩展
     *
     * 扫描mybatis配置文件，将上述配置提取出来以便下一步进行mapper注册到spring时候用。
     * @param mybatisProperties mybatis配置文件
     * @param mapperInterfaces 需要扫描的接口集合
     * @param resources 需要扫描的resource资源文件集合
     * @param packagesToBeScan 需要扫描的package集合
     */
    private void parseMapperXMLsFromMybatisConfigFile(MybatisProperties mybatisProperties,
                                                      Set<String> mapperInterfaces,
                                                      Set<Resource> resources,
                                                      Set<String> packagesToBeScan) {
        if(isNotEmpty(mybatisProperties.getMapperLocations())) {
            Resource resource = this.resourceLoader.getResource(mybatisProperties.getConfigLocation());
            MyBatisConfigParserHandler myBatisConfigParserHandler = new MyBatisConfigParserHandler();
            MybatisXMLParserHandler.extractXMLResource(resource, myBatisConfigParserHandler);
            List<MybatisConfigMapperProperty> mybatisConfigMapperProperties = myBatisConfigParserHandler.getResult()
                    .getMybatisConfigMapperProperties();
            for (MybatisConfigMapperProperty mybatisConfigMapperProperty : mybatisConfigMapperProperties) {
                String interfaceName = mybatisConfigMapperProperty.getClazz();
                String resourceStr = mybatisConfigMapperProperty.getResource();
                String url = mybatisConfigMapperProperty.getUrl();
                ifHasTextThen(url, str -> resources.add(this.resourceLoader.getResource(
                        String.format("%s%s", ResourceUtils.CLASSPATH_URL_PREFIX, str)
                )));
                ifHasTextThen(resourceStr, str -> resources.add(this.resourceLoader.getResource(str)));
                ifHasTextThen(interfaceName, mapperInterfaces::add);
            }
            List<MybatisConfigMapperPackageProperty> mybatisConfigMapperPackageProperties = myBatisConfigParserHandler.getResult()
                    .getMybatisConfigMapperPackageProperties();
            for (MybatisConfigMapperPackageProperty mybatisConfigMapperPackageProperty : mybatisConfigMapperPackageProperties) {
                String packageName = mybatisConfigMapperPackageProperty.getName();
                ifHasTextThen(packageName, packagesToBeScan::add);
            }
        }
    }

    /**
     * 把String代表的路径解析成Resource对象列表
     * @param mapperLocations 路径数组
     * @return 资源对象数组
     */
    private Resource[] resolveMapperLocations(String[] mapperLocations) {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = new ArrayList<>();
        if(isNotEmpty(mapperLocations)) {
            for (String mapperLocation : mapperLocations) {
                try {
                    Resource[] mappers = resourcePatternResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                } catch (IOException e) {
                    logger.error("获取mapper文件resource资源失败.", e);
                    throw new RuntimeException("获取mapper文件resource资源失败.", e);
                }
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * 注册事务管理器
     * @param registry Bean定义注册器
     * @param datasourceName 数据源名称
     */
    private void registerTransactionManager(BeanDefinitionRegistry registry,
                                            String datasourceName) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class);
        beanDefinitionBuilder.addConstructorArgReference(datasourceName);
        beanDefinitionBuilder.addDependsOn(datasourceName);
        doRegisterBean(registry, generateTransactionManagerBeanName(datasourceName), beanDefinitionBuilder);
    }

    /**
     * 注册SqlSessionTemplate
     * @param registry Bean定义注册器
     * @param datasourceName 数据源名称
     */
    private void registerSqlSessionTemplate(BeanDefinitionRegistry registry,
                                            String datasourceName) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class);
        beanDefinitionBuilder.addConstructorArgReference(generateSqlSessionFactoryBeanName(datasourceName));
        beanDefinitionBuilder.addDependsOn(generateSqlSessionFactoryBeanName(datasourceName));
        doRegisterBean(registry, generateSqlSessionTemplateBeanName(datasourceName), beanDefinitionBuilder);
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
