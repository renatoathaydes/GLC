package com.athaydes.glc.runtime

import com.athaydes.glc.CompiledGlcProcedure
import com.athaydes.glc.GenericType
import com.athaydes.glc.GlcProcedure
import com.athaydes.glc.GlcProcedureParameter
import com.athaydes.glc.GlcProcedures
import spock.lang.Specification
import spock.lang.Subject

class GlcRunnerSpec extends Specification {

    @Subject
    final GlcRunner glcRunner = new GlcRunner()

    def "All GLC procedures without input should be run immediately"() {
        given: 'A few GLC procedures that do not take any input'
        def counter1 = 0
        def counter2 = 0
        def counter3 = 0

        def runnable1 = { counter1++ }
        def runnable2 = { counter2++ }
        def runnable3 = { counter3++ }

        def procedures = [
                createGlcProcedure( 'procedure1', [ ], null, runnable1 ),
                createGlcProcedure( 'procedure2', [ ], null, runnable2 ),
                createGlcProcedure( 'procedure3', [ ], null, runnable3 )
        ]

        when: 'The GLC Runner runs those procedures'
        glcRunner.run( new GlcProcedures( procedures ) )

        then: 'They are run immediately'
        counter1 == 1
        counter2 == 1
        counter3 == 1
    }

    GlcProcedure createGlcProcedure( String name,
                                     List<GlcProcedureParameter> inputs = [ ],
                                     GlcProcedureParameter output = null,
                                     Closure runnable = { -> } ) {
        if ( output == null ) {
            output = new GlcProcedureParameter( new GenericType( String, GenericType.EMPTY ), 'out' )
        }

        return new GlcProcedure( new CompiledGlcProcedure( name, inputs, output ), runnable )
    }

}
