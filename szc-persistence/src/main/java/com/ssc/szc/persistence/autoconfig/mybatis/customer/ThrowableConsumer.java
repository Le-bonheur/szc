package com.ssc.szc.persistence.autoconfig.mybatis.customer;

import java.util.Objects;

/**
 * @author Lebonheur
 *
 * 提供可以抛出异常的Consumer
 */
@FunctionalInterface
public interface ThrowableConsumer<T> {

    void accept(T t) throws RuntimeException;

    default ThrowableConsumer<T> andThen(ThrowableConsumer<? super T> after) throws RuntimeException {
        Objects.requireNonNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }

}
