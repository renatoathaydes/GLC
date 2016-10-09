package com.athaydes.glc.driver

import java.lang.annotation.Annotation
import java.util.function.Consumer

/**
 * A GLC system I/O.
 */
interface GlcIO<T> {
    Class<T> getType()
}

/**
 * A GLC system output of type T.
 * @param <T> type of output.
 */
interface GlcOut<T> extends GlcIO<T> {
    String getName()

    void take( T instance )
}

/**
 * A GLC system input of type T.
 * @param <T> type of input.
 */
interface GlcIn<T> extends GlcIO<T> {
    String getName()

    void provide( Consumer<T> consumer )
}

/**
 * A GLC system input of type T.
 *
 * The configuration of the input may be provided using an annotation of type A.
 * @param <T> type of input.
 * @param <A> type of configuration annotation.
 */
interface GlcDriver<T, A extends Annotation> extends GlcIO<T> {

    Class<A> getRequiredAnnotation()

    void watch( A annotation, Consumer<T> onChange )
}
