package com.ssc.szc.persistence.autoconfig.mybatis.xml.parse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

/**
 * 解析mybatis的mapper xml配置文件，获取mapper对应的namespace，以便获取相应接口的解析器
 * @see MybatisXMLParserHandler
 * @author Lebonheur
 */
public class MybatisMapperXmlNamespaceParserHandler extends MybatisXMLParserHandler<String>{

    private Logger logger = LoggerFactory.getLogger(MybatisMapperXmlNamespaceParserHandler.class);

    private String namespace = null;

    public String getNamespace() {
        return namespace;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if("mapper".equalsIgnoreCase(qName)) {
            namespace = attributes.getValue("namespace");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {

    }

    @Override
    public void startDocument() {

    }

    @Override
    public void endDocument() {
        logger.debug("结束mapper xml文件解析");
    }

    @Override
    public String getResult() {
        return namespace;
    }
}
