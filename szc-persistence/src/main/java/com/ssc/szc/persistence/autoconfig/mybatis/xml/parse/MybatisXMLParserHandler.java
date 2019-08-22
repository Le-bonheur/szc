package com.ssc.szc.persistence.autoconfig.mybatis.xml.parse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * 用于获取mybatis配置文件的解析器
 * 主要有解析mybatis配置的mapper节点相关信息、mapper配置文件的namespace信息
 * @see MyBatisConfigParserHandler
 * @see MybatisMapperXmlNamespaceParserHandler
 * @author Lebonheur
 */
public abstract class MybatisXMLParserHandler<T> extends DefaultHandler {

    private static Logger logger = LoggerFactory.getLogger(MybatisXMLParserHandler.class);

    /**
     * 从Resource对象中获得xml文件并解析xml文件并处理
     * 从Resource对象获取文本内容，去掉xml相关scheme信息，因为默认SAXParser处理的时候需要下载相关文件规范进行校验，在内网环境往往失败，
     * 因此这里需要去掉。格式校验的mybatis会去做，这里由于我们的解析是独立于mybatis之外，并且再之前，因此不必担心格式方面的问题。
     * xml解析使用的是SAXParser进行解析，根据传入的MybatisXMLParserHandler进行解析，解析的结果由泛型T指定。
     * @see MyBatisConfigParserHandler
     * @see MybatisMapperXmlNamespaceParserHandler
     * @param resource 资源对象
     * @param handler 处理器
     * @param <T> 解析将得到的结果类型
     */
    public static <T> void extractXMLResource(Resource resource, MybatisXMLParserHandler<T> handler) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try (InputStream inputStream = resource.getInputStream()) {
            String content = new String(FileCopyUtils.copyToByteArray(inputStream));
            content = content.replaceAll("<!DOCTYPE[^>]*>", "")
                    .replaceAll("\r\n|\n", "")
                    .replaceAll("\\s+", " ")
                    .replaceAll("<!--((?!-->).)*-->", "");
            SAXParser parser = factory.newSAXParser();
            InputSource inputSource = new InputSource(new StringReader(content));
            parser.parse(inputSource, handler);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.error("解析xml配置文件提取信息时出错.", e);
            throw new RuntimeException("解析xml配置文件提取信息时出错.", e);
        }
    }

    public abstract T getResult();

}
