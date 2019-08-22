package com.ssc.szc.persistence.autoconfig.mybatis.xml.parse;

import com.ssc.szc.persistence.autoconfig.mybatis.MybatisConfigMapperParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用于mybatis配置文件解析的处理器，用于获取mybatis xml配置文件中mappers元素下mapper及package元素的解析
 * @see MybatisXMLParserHandler
 * @see org.xml.sax.helpers.DefaultHandler
 * @author Lebonheur
 */
public class MyBatisConfigParserHandler extends MybatisXMLParserHandler<MybatisConfigMapperParseResult>{

    private Logger logger = LoggerFactory.getLogger(MyBatisConfigParserHandler.class);

    private MybatisConfigMapperProperty mybatisConfigMapperPropertyTemp;

    private List<MybatisConfigMapperProperty> mybatisConfigMapperProperties = new ArrayList<>();

    private MybatisConfigMapperPackageProperty mybatisConfigMapperPackagePropertyTemp;

    private List<MybatisConfigMapperPackageProperty> mybatisConfigMapperPackageProperties = new ArrayList<>();

    public List<MybatisConfigMapperProperty> getMybatisConfigMapperProperties() {
        return mybatisConfigMapperProperties;
    }

    public List<MybatisConfigMapperPackageProperty> getMybatisConfigMapperPackageProperties() {
        return mybatisConfigMapperPackageProperties;
    }

    private boolean inMappersNode = false;

    @Override
    public void endDocument() {
        mybatisConfigMapperPackageProperties = mybatisConfigMapperPackageProperties.stream()
                .filter(mybatisConfigMapperPackageProperty ->
                        StringUtils.hasText(mybatisConfigMapperPackageProperty.getName()))
                .collect(Collectors.toList());
        mybatisConfigMapperProperties = mybatisConfigMapperProperties.stream()
                .filter(mybatisConfigMapperProperty -> StringUtils.hasText(mybatisConfigMapperProperty.getClazz())
                        || StringUtils.hasText(mybatisConfigMapperProperty.getResource())
                        || StringUtils.hasText(mybatisConfigMapperProperty.getUrl()))
                .collect(Collectors.toList());
        logger.debug("解析mybatis config文件结束");
    }

    @Override
    public void startDocument() {
        logger.debug("解析mybatis config文件开始");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if("mappers".equalsIgnoreCase(qName)) {
            inMappersNode = true;
        }
        if("mapper".equalsIgnoreCase(qName) && inMappersNode) {
            mybatisConfigMapperPropertyTemp = new MybatisConfigMapperProperty();
            mybatisConfigMapperPropertyTemp.setClazz(attributes.getValue("class"));
            mybatisConfigMapperPropertyTemp.setResource(attributes.getValue("resource"));
            mybatisConfigMapperPropertyTemp.setUrl(attributes.getValue("url"));
            mybatisConfigMapperProperties.add(mybatisConfigMapperPropertyTemp);
            mybatisConfigMapperPropertyTemp = null;
        } else if("package".equalsIgnoreCase(qName) && inMappersNode) {
            mybatisConfigMapperPackagePropertyTemp = new MybatisConfigMapperPackageProperty();
            mybatisConfigMapperPackagePropertyTemp.setName(attributes.getValue("name"));
            mybatisConfigMapperPackageProperties.add(mybatisConfigMapperPackagePropertyTemp);
            mybatisConfigMapperPackagePropertyTemp = null;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if("mappers".equalsIgnoreCase(qName)) {
            inMappersNode = false;
        }
    }

    @Override
    public MybatisConfigMapperParseResult getResult() {
        MybatisConfigMapperParseResult mybatisConfigMapperParseResult = new MybatisConfigMapperParseResult();
        mybatisConfigMapperParseResult.setMybatisConfigMapperPackageProperties(mybatisConfigMapperPackageProperties);
        mybatisConfigMapperParseResult.setMybatisConfigMapperProperties(mybatisConfigMapperProperties);
        return mybatisConfigMapperParseResult;
    }
}
