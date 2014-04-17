package com.athaydes.glc.internal;

import com.athaydes.glc.api.MutableValue;

/**
 *
 */
public class MutableValueImpl<T> implements MutableValue<T> {

    private T value;
    private final Class<T> type;

    public MutableValueImpl( Class<T> type, T initialValue ) {
        this.type = type;
        this.value = initialValue;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue( T value ) {
        this.value = value;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        MutableValueImpl that = ( MutableValueImpl ) o;

        if ( !type.equals( that.type ) ) return false;
        if ( !value.equals( that.value ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MutableValueImpl{" +
                "value=" + value +
                ", type=" + type +
                '}';
    }
}
