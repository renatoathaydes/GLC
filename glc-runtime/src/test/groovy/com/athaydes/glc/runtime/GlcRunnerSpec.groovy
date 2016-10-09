package com.athaydes.glc.runtime

import com.athaydes.glc.procedure.CompiledGlcProcedure
import com.athaydes.glc.procedure.GenericType
import com.athaydes.glc.procedure.GlcProcedure
import com.athaydes.glc.procedure.GlcProcedureParameter
import com.athaydes.glc.procedure.GlcProcedures
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

        def runnable1 = { counter1++; 'AA' }
        def runnable2 = { counter2++; 2 }
        def runnable3 = { counter3++; 3 }

        def parameterA = GlcParameter( String, 'a' )
        def parameterI = GlcParameter( Integer, 'i' )
        def parameterJ = GlcParameter( Integer, 'j' )

        def procedures = [
                GlcProcedure( 'procedure1', [ ], parameterA, runnable1 ),
                GlcProcedure( 'procedure2', [ ], parameterI, runnable2 ),
                GlcProcedure( 'procedure3', [ ], parameterJ, runnable3 )
        ]

        when: 'The GLC Runner runs those procedures'
        glcRunner.run( new GlcProcedures( procedures ) )

        then: 'They are run immediately'
        counter1 == 1
        counter2 == 1
        counter3 == 1

        and: 'The value of each parameter is correctly assigned'
        glcRunner.valueOf( parameterA ) == 'AA'
        glcRunner.valueOf( parameterI ) == 2
        glcRunner.valueOf( parameterJ ) == 3
    }

    def "GLC should run procedures until no input changes are possible"() {
        given: 'A few GLC procedures that cause a few cycles to run'
        def runnable1 = { 123 }
        def runnable2 = { Integer i -> i + 321 }
        def runnable3 = { Integer j -> j.toString() }

        def parameterI = GlcParameter( Integer, 'i' )
        def parameterJ = GlcParameter( Integer, 'j' )
        def parameterS = GlcParameter( String, 's' )

        def procedures = [
                GlcProcedure( 'procedure1', [ ], parameterI, runnable1 ),
                GlcProcedure( 'procedure2', [ parameterI ], parameterJ, runnable2 ),
                GlcProcedure( 'procedure3', [ parameterJ ], parameterS, runnable3 )
        ]

        when: 'The GLC Runner runs those procedures'
        glcRunner.run( new GlcProcedures( procedures ) )

        then: 'The value of each parameter is correctly assigned'
        glcRunner.valueOf( parameterI ) == 123
        glcRunner.valueOf( parameterJ ) == 444
        glcRunner.valueOf( parameterS ) == '444'
    }

    GlcProcedure GlcProcedure( String name,
                               List<GlcProcedureParameter> inputs,
                               GlcProcedureParameter output,
                               Closure runnable ) {
        new GlcProcedure( new CompiledGlcProcedure( name, inputs, output ), runnable )
    }

    GlcProcedureParameter GlcParameter( Class type, String name ) {
        new GlcProcedureParameter( new GenericType( type, GenericType.EMPTY ), name )
    }

}
