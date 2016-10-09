package com.athaydes.glc.driver

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.util.function.Consumer

/**
 * The Console output
 */
@Slf4j
@CompileStatic
class GlcConsole implements GlcOut<Object>, GlcIn<String> {
    String name = 'out'
    Class<Object> type = Object

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
