package com.ssc.szc.commons.util;

/**
 * @author Lebonheur
 */
public class ObjectUtils extends org.springframework.util.ObjectUtils {

    /**
     * 对象不为空
     * @param obj 对象
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Object obj) {
        return !org.springframework.util.ObjectUtils.isEmpty(obj);
    }

    /**
     * 不定长对象是否都为空
     * @param objects 不定长对象列表
     * @return 是否全为空
     */
    public static boolean isAllEmpty(Object... objects) {
        for (Object object : objects) {
            if(!org.springframework.util.ObjectUtils.isEmpty(object)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否给定的对象列表有一个为空
     * @param objects 给定的对象列表
     * @return 是否有任意一个为空
     */
    public static boolean isAnyEmpty(Object... objects) {
        for (Object object : objects) {
            if(org.springframework.util.ObjectUtils.isEmpty(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 如果给定对象不为空，那么执行相应的操作
     * @param obj 接受判断的对象
     * @param operation 对应操作
     * @return 是否不为空
     */
    public static boolean ifNotEmptyThen(Object obj, Runnable operation) {
        boolean isNotEmpty = isNotEmpty(obj);
        if(isNotEmpty) {
            operation.run();
        }
        return isNotEmpty;
    }

}
