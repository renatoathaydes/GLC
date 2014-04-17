package com.athaydes.glc.internal;

import com.athaydes.glc.api.HasIO;
import com.athaydes.glc.api.MutableValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 */
class IO implements HasIO {

    private final Map<Class<?>, MutableValueMap> inputs = new HashMap<>();
    private final Function<Class<?>, MutableValueMap> newMapFun = ( cls ) -> new MutableValueMap();
    private final MutableValueMap emptyMap = new MutableValueMap();

    @Override
    public <T> boolean addValue( String name, Class<T> type, T initialValue ) {
        return inputs.computeIfAbsent( type, newMapFun )
                .put( name, new MutableValueImpl<>( type, initialValue ) );
    }

    @Override
    public <T> boolean removeValue( String name, Class<T> type ) {
        return inputs.getOrDefault( type, emptyMap ).remove( name );
    }

    @Override
    public <T> Optional<MutableValue<T>> getValue( String name, Class<T> type ) {
        return inputs.getOrDefault( type, emptyMap ).get( name );
    }

}
