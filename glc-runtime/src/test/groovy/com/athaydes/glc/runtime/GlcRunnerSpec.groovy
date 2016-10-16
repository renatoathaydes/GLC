package com.athaydes.glc.runtime

import com.athaydes.glc.io.Console
import com.athaydes.glc.io.api.GlcIn
import com.athaydes.glc.procedure.AnnotationInfo
import com.athaydes.glc.procedure.GenericType
import com.athaydes.glc.procedure.GlcProcedures
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Consumer

class GlcRunnerSpec extends Specification implements GlcTest {

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

    def "All GLC procedures whose input has a GLC annotation should be run immediately"() {
        given: 'A GLC procedure that takes a GLC-annotated input'
        def lines = [ ]
        def runnable1 = { String line -> lines << line; line + '!!' }

        def consoleIn = GlcParameter( String, 'line',
                [ new AnnotationInfo(
                        type: new GenericType( Console.name, [ ] as GenericType[], [ ] ),
                        members: [ : ],
                        driverType: MockConsole ) ] )

        def out = GlcParameter( String, 'out' )

        def procedures = [ GlcProcedure( 'console', [ consoleIn ], out, runnable1 ) ]

        when: 'The GLC Runner runs the procedure'
        glcRunner.run( new GlcProcedures( procedures ) )

        then: 'The procedure runnable did run'
        lines == [ 'Hello test' ]

        and: 'The value of the output is correctly assigned'
        glcRunner.valueOf( out ) == 'Hello test!!'
    }

}

class MockConsole implements GlcIn<String> {
    @Override
    Class<String> getInputType() { String }

    @Override
    void provide( Consumer<String> consumer ) {
        consumer.accept( 'Hello test' )
    }

}
