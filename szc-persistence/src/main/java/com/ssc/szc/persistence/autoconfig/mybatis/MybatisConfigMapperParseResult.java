package com.ssc.szc.persistence.autoconfig.mybatis;

import com.ssc.szc.persistence.autoconfig.mybatis.xml.parse.MybatisConfigMapperPackageProperty;
import com.ssc.szc.persistence.autoconfig.mybatis.xml.parse.MybatisConfigMapperProperty;

import java.util.List;

/**
 * @author Lebonheur
 */
public class MybatisConfigMapperParseResult {

    private List<MybatisConfigMapperPackageProperty> mybatisConfigMapperPackageProperties;

    private List<MybatisConfigMapperProperty> mybatisConfigMapperProperties;

    public List<MybatisConfigMapperPackageProperty> getMybatisConfigMapperPackageProperties() {
        return mybatisConfigMapperPackageProperties;
    }

    public void setMybatisConfigMapperPackageProperties(List<MybatisConfigMapperPackageProperty> mybatisConfigMapperPackageProperties) {
        this.mybatisConfigMapperPackageProperties = mybatisConfigMapperPackageProperties;
    }

    public List<MybatisConfigMapperProperty> getMybatisConfigMapperProperties() {
        return mybatisConfigMapperProperties;
    }

    public void setMybatisConfigMapperProperties(List<MybatisConfigMapperProperty> mybatisConfigMapperProperties) {
        this.mybatisConfigMapperProperties = mybatisConfigMapperProperties;
    }
}
