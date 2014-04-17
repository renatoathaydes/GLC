package com.athaydes.glc.internal;

import com.athaydes.glc.api.Question;
import com.athaydes.glc.api.Then;
import com.athaydes.glc.api.Value;

import java.util.Objects;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 *
 */
class QuestionImpl<T> implements Question<T> {

    private final GlcImpl glc;
    private final String value;
    private final Class<T> type;

    QuestionImpl( GlcImpl glc, String value, Class<T> type ) {
        this.glc = glc;
        this.value = value;
        this.type = type;
    }

    @Override
    public Then is( T expected ) {
        return new ThenImpl<>( glc, value, type,
                elValue -> Objects.equals( elValue.getValue(), expected ) );
    }

    @Override
    public Then turns( T expectedValue ) {
        Observable o = new Observable();
        return new ThenImpl<>( glc, value, type,
                (oldValue, newValue) ->
                        !Objects.equals( oldValue.getValue(), expectedValue ) &&
                        Objects.equals( newValue.getValue(), expectedValue ));
    }

    @Override
    public Then turns( Callable<T> getValue ) {
        //TODO
        return null;
    }

    @Override
    public Then satisfies( Predicate<Value<T>> predicate ) {
        return new ThenImpl<>( glc, value, type, predicate );
    }

}