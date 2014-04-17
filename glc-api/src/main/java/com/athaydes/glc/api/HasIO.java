package com.athaydes.glc.api;

import java.util.Optional;

/**
 *
 */
public interface HasIO {

    <T> boolean addValue( String name, Class<T> type, T initialValue );

    <T> boolean removeValue( String name, Class<T> type );

    <T> Optional<MutableValue<T>> getValue( String name, Class<T> type );

}
