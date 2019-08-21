package com.ssc.szc.persistence.autoconfig.property;

import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.util.Map;

/**
 * @author Lebonheur
 */
public class PersistenceProperties {

    private Map<String, Object> datasourcePropsMap;

    private Map<String, Object> mybatisPropsMap;

    private DataSourceProperties dataSource;

    private MybatisProperties mybatis;

    public Map<String, Object> getDatasourcePropsMap() {
        return datasourcePropsMap;
    }

    public void setDatasourcePropsMap(Map<String, Object> datasourcePropsMap) {
        this.datasourcePropsMap = datasourcePropsMap;
    }

    public Map<String, Object> getMybatisPropsMap() {
        return mybatisPropsMap;
    }

    public void setMybatisPropsMap(Map<String, Object> mybatisPropsMap) {
        this.mybatisPropsMap = mybatisPropsMap;
    }

    public DataSourceProperties getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSourceProperties dataSource) {
        this.dataSource = dataSource;
    }

    public MybatisProperties getMybatis() {
        return mybatis;
    }

    public void setMybatis(MybatisProperties mybatis) {
        this.mybatis = mybatis;
    }
}
