package com.athaydes.glc.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
public class MutableValueMap<T> {

    private final Map<String, MutableValueImpl<T>> valueByName = new HashMap<>();

    public Optional<MutableValueImpl<T>> get( String name ) {
        return Optional.ofNullable( valueByName.get( name ) );
    }

    public boolean put( String name, MutableValueImpl<T> value ) {
        if ( name == null || value == null ) throw new NullPointerException();
        if ( valueByName.containsKey( name ) ) {
            return false;
        }
        valueByName.put( name, value );
        return true;
    }

    public boolean remove( String name ) {
        return valueByName.remove( name ) != null;
    }

}
