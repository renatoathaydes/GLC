package com.athaydes.glc.internal;

import com.athaydes.glc.api.MutableValue;
import com.athaydes.glc.api.Otherwise;
import com.athaydes.glc.api.Then;
import com.athaydes.glc.api.Value;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 *
 */
class ThenImpl<T> implements Then {

    private Runnable run;
    private final GlcImpl glc;
    private final String inputName;
    private final Class<T> type;
    private final BiFunction<Value<T>, Value<T>, Boolean> valuePredicate;
    private OtherWiseImpl otherwise;
    private T oldValue = null;

    ThenImpl( GlcImpl glc, String input, Class<T> type, Predicate<Value<T>> valuePredicate ) {
        this( glc, input, type, ( from, to ) -> valuePredicate.test( to ) );
    }

    ThenImpl( GlcImpl glc, String input, Class<T> type,
              BiFunction<Value<T>, Value<T>, Boolean> valueChangePredicate ) {
        this.glc = glc;
        this.inputName = input;
        this.type = type;
        this.valuePredicate = valueChangePredicate;
    }

    @Override
    public Otherwise then( Runnable run ) {
        this.run = run;
        otherwise = new OtherWiseImpl();
        glc.addThen( this );
        return otherwise;
    }

    protected void cycle() {
        glc.getValue( inputName, type ).ifPresent( inputValue -> {
            System.out.println("Cycling "+ inputName + " with oldValue = " + oldValue + ", new = " + inputValue );
            if ( valuePredicate.apply( new MutableValueImpl<>( type, oldValue ), inputValue ) ) {
                run.run();
            } else if ( otherwise.run != null ) {
                otherwise.run.run();
            }
            oldValue = inputValue.getValue();
        } );
    }

}
