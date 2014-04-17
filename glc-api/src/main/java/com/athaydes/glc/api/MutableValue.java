package com.athaydes.glc.api;

/**
 *
 */
public interface MutableValue<T> extends Value<T> {

    void setValue( T value );

}
