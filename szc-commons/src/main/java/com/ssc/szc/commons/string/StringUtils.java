package com.ssc.szc.commons.string;

import java.util.function.Consumer;

/**
 * @author Lebonheur
 */
public class StringUtils extends org.springframework.util.StringUtils {

    /**
     * 判断字符串是否有内容，有则进行相关操作
     * @param str 字符串
     * @param consumer 函数
     * @return 是否有内容
     */
    public static boolean ifHasTextThen(String str, Consumer<String> consumer) {
        boolean result = org.springframework.util.StringUtils.hasText(str);
        if(result) {
            consumer.accept(str);
        }
        return result;
    }

}
