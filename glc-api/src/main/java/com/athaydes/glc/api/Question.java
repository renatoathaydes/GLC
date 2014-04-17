package com.athaydes.glc.api;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 *
 */
public interface Question<T> {

    Then is( T value );

    Then turns( T value );

    Then turns( Callable<T> getValue );

    Then satisfies( Predicate<Value<T>> predicate );

}
