package com.athaydes.glc.api;

/**
 *
 */
public interface Value<T> {

    T getValue();

    Class<T> getType();

}
