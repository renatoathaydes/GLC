package com.athaydes.glc.io.internal

import com.athaydes.glc.io.api.GlcIn
import com.athaydes.glc.io.api.GlcOut
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.util.logging.Slf4j

import java.util.function.Consumer

/**
 * The Console output
 */
@Slf4j
@CompileStatic
@Immutable
class GlcConsole implements GlcIn<String>, GlcOut<Object> {
    Class<String> inputType = String
    Class<Object> outputType = Object

    @Override
    void take( Object instance ) {
        println instance
    }

    @Override
    void provide( Consumer<String> consumer ) {
        log.debug( "Starting Thread to wait for user input" )

        Thread.startDaemon( 'glc-console-in' ) {
            Scanner scanner = new Scanner( System.in )
            Closure loop = null;
            loop = {
                log.debug( "Starting console input loop" )
                try {
                    //noinspection GroovyInfiniteLoopStatement
                    while ( true ) {
                        consumer.accept( scanner.nextLine() )
                    }
                } catch ( e ) {
                    log.warn( "Error while reading console input", e )
                    loop()
                }
            }
        }
    }
}