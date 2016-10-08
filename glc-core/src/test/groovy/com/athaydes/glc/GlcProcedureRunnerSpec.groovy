package com.athaydes.glc

import spock.lang.Specification
import spock.lang.Subject

class GlcProcedureRunnerSpec extends Specification {

    @Subject
    Glc glc = new Glc()

    def "A simple GLC procedure can be run"() {
        when: 'A GLC Procedure is compiled'
        def procedures = glc.compile( '{ String s -> String t = s + "!"; return t }' ).allProcedures

        then: 'The correct number of procedures is compiled'
        procedures.size() == 1

        and: 'When we run the procedure, we get the expected result'
        procedures.first().runnable.call( 'hello' ) == 'hello!'
    }

    def "A generic GLC procedure can be run"() {
        when: 'A GLC Procedure with generic types is compiled'
        def procedures = glc.compile( '{ List<String> list, Optional<Map<Integer, Long>> opt -> ' +
                'Map<Float, Double> map = [0f:2d]; return map }' ).allProcedures

        then: 'The correct number of procedures is compiled'
        procedures.size() == 1

        and: 'When we run the procedure, we get the expected result'
        procedures.first().runnable.call( [ 'hi' ], Optional.of( [ 10, 100L ] ) ) == [ 0f: 2d ]
    }

}
