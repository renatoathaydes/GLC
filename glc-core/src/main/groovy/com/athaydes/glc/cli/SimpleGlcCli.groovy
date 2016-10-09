package com.athaydes.glc.cli

import com.athaydes.glc.Glc
import com.athaydes.glc.procedure.GlcProcedure

/**
 *
 */
class SimpleGlcCli {

    static main( args ) {
        def glc = new Glc()

        String input
        def nextLine = {
            print "glc > "
            System.in.newReader().readLine()
        }

        List<GlcProcedure> procedures = [ ]

        while ( ( input = nextLine() ) != 'exit' ) {
            try {
                if ( input.startsWith( 'run' ) ) {
//                    def procedureInput = Eval.me( input.substring( 3 ) )
//                    if ( procedures ) {
//                        def result = procedures.last().runnable( *procedureInput )
//                        println "Procedure returned: $result"
//                    } else {
//                        println "Cannot run, procedure not found."
//                    }
                    glc.eval( input.substring( 3 ) )
                } else {
                    glc.compileGlcProcedures( input )
                    println "GLC expression compiled ok!"
                }
            } catch ( Throwable t ) {
                println "ERROR: $t"
                t.printStackTrace()
            }
        }

        println "Bye!"

    }
}
