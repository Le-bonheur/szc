package com.ssc.szc.commons.util;

/**
 * @author Lebonheur
 */
public class BeanNameGeneratorUtil {

    public static String generateSqlSessionFactoryBeanName(String dataSourceName) {
        return dataSourceName + "SqlSessionFactory";
    }

    public static String generateSqlSessionTemplateBeanName(String dataSourceName) {
        return dataSourceName + "SqlSessionTemplate";
    }

    public static String generateTransactionManagerBeanName(String dataSourceName) {
        return dataSourceName + "TransactionManager";
    }

    public static String generateMapperFactoryBeanBeanName(String dataSourceName) {
        return dataSourceName + "MapperFactoryBean";
    }

}
