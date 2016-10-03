package com.athaydes.glc

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

        GlcProcedure procedure = null

        while ( ( input = nextLine() ) != 'exit' ) {
            try {
                if ( input.startsWith( 'run' ) ) {
                    def procedureInput = Eval.me( input.substring( 3 ) )
                    if ( procedure != null ) {
                        def result = procedure.runnable( *procedureInput )
                        println "Procedure returned: $result"
                    } else {
                        println "Cannot run, procedure not found."
                    }
                } else {
                    procedure = glc.compile( input )
                    println "GLC expression compiled ok!"
                    println "Args: ${procedure.inputs}"
                    println "Out:  ${procedure.output}"
                }
            } catch ( Throwable t ) {
                println "ERROR: $t"
                t.printStackTrace()
            }
        }

        println "Bye!"

    }
}
