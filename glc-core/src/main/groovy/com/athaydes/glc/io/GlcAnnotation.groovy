package com.athaydes.glc.io

import com.athaydes.glc.io.api.GlcIO
import com.athaydes.glc.io.internal.GlcConsole

import java.util.concurrent.TimeUnit

/**
 * Annotations used to annotate GLC Procedure parameters must be themselves annotated with GlcAnnotation
 * as a means to associate the implementation class to the annotation.
 */
@interface GlcAnnotation {
    Class<GlcIO<?>> value()
}

/**
 * Annotates an input or output that should be attached to a console.
 *
 * A console is normally a command-line terminal used by a person to interact with the system.
 */
@GlcAnnotation( GlcConsole )
@interface Console {}


@GlcAnnotation( GlcConsole )
@interface Timer {
    long value()

    TimeUnit unit() default TimeUnit.MILLISECONDS
}

/**
 * The Nullable annotation may be used to annotate GLC procedure input which is not required for the procedure to run.
 *
 * Notice that this is useful only when a procedure has more than one input, as otherwise the procedure would never
 * run until the annotated input had a value.
 */
@interface Nullable {}


