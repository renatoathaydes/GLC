package com.athaydes.glc.io.api

import java.lang.annotation.Annotation
import java.util.function.Consumer

/**
 * A GLC system I/O.
 */
interface GlcIO {}

/**
 * A GLC Input.
 * @param <T> type of the input.
 */
interface In<T> extends GlcIO {
    Class<T> getInputType()
}

/**
 * A GLC Output.
 * @param <T> type of the output.
 */
interface Out<T> extends GlcIO {
    Class<T> getOutputType()
}

/**
 * A GLC system input of type T.
 * @param <T> type of input.
 */
interface GlcIn<T> extends In<T> {
    void provide( Consumer<T> consumer )
}

/**
 * A GLC system output of type T.
 * @param <T> type of output.
 */
interface GlcOut<T> extends Out<T> {
    void take( T instance )
}

/**
 * A GLC system input of type T.
 *
 * The configuration of the input may be provided using an annotation of type A.
 * @param <T> type of input.
 * @param <A> type of configuration annotation.
 */
interface GlcDriver<T, A extends Annotation> extends In<T> {

    Class<A> getConfig()

    void watch( A annotation, Consumer<T> onChange )
}
