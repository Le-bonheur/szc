package com.ssc.szc.persistence.autoconfig.mybatis.xml.parse;

/**
 * mybatis配置文件中mapper元素下配置的属性
 * 在mybatis的配置文件中，mapper节点可以有三个属性：resource、url、class，三者只能选一
 * @author Lebonheur
 */
public class MybatisConfigMapperProperty {

    private String resource;

    private String url;

    private String clazz;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
}
