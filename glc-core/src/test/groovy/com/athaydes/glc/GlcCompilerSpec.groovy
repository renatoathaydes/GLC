package com.athaydes.glc

import spock.lang.Specification
import spock.lang.Subject

class GlcCompilerSpec extends Specification {

    static final GenericType STRING_TYPE = new GenericType( String, GenericType.EMPTY )
    static final GenericType INTEGER_TYPE = new GenericType( Integer, GenericType.EMPTY )
    static final GenericType FLOAT_TYPE = new GenericType( Float, GenericType.EMPTY )
    static final GenericType DOUBLE_TYPE = new GenericType( Double, GenericType.EMPTY )
    static final GenericType LONG_TYPE = new GenericType( Long, GenericType.EMPTY )

    static final GenericType LIST_OF_STRINGS_TYPE = new GenericType( List, [ STRING_TYPE ] as GenericType[] )

    static GenericType optionalOf( GenericType genericType ) {
        new GenericType( Optional, [ genericType ] as GenericType[] )
    }

    static GenericType mapOf( GenericType keyType, GenericType valueType ) {
        new GenericType( Map, [ keyType, valueType ] as GenericType[] )
    }

    @Subject
    Glc glc = new Glc()

    def "A simple GLC procedure can be compiled"() {
        when: 'A GLC Procedure is compiled'
        glc.compile( '{ String s -> String t = s + "!"; return t }' )
        CompiledGlcProcedure procedure = glc.allProcedures.last()

        then: 'The correct number of procedures is compiled'
        glc.allProcedures.size() == 1

        and: 'The GlcProcedures class provides the latest procedure as expected'
        procedure.closureName
        procedure.inputs == [ new GlcProcedureParameter( STRING_TYPE, 's' ) ]
        procedure.output == new GlcProcedureParameter( STRING_TYPE, 't' )
    }

    def "A GLC procedure using generic types can be compiled"() {
        when: 'A GLC Procedure with generic types is compiled'
        glc.compile( '{ List<String> list, Optional<Map<Integer, Long>> opt -> ' +
                'Map<Float, Double> map = [0f:2d]; return map }' )
        CompiledGlcProcedure procedure = glc.allProcedures.last()

        then: 'The correct number of procedures is compiled'
        glc.allProcedures.size() == 1

        and: 'The GlcProcedures class provides the latest procedure as expected'
        procedure.closureName
        procedure.inputs == [ new GlcProcedureParameter( LIST_OF_STRINGS_TYPE, 'list' ),
                              new GlcProcedureParameter( optionalOf( mapOf( INTEGER_TYPE, LONG_TYPE ) ), 'opt' ) ]
        procedure.output == new GlcProcedureParameter( mapOf( FLOAT_TYPE, DOUBLE_TYPE ), 'map' )
    }

    def "Many GLC procedures can be compiled"() {
        when: 'Many GLC Procedures are compiled in the same compilation unit'
        glc.compile( '{ String s -> String t = s + "!"; return t };' +
                '{ List<String> list, Optional<Map<Integer, Long>> opt -> ' +
                'Map<Float, Double> map = [0f:2d]; return map };\n\n' +
                '{ Long abc -> int integer= 0; integer };' )

        List<CompiledGlcProcedure> procedures = glc.allProcedures

        then: 'The correct number of procedures is compiled'
        procedures.size() == 3

        then: 'The first GlcProcedure is compiled as expected'
        procedures[ 0 ].closureName
        procedures[ 0 ].inputs == [ new GlcProcedureParameter( STRING_TYPE, 's' ) ]
        procedures[ 0 ].output == new GlcProcedureParameter( STRING_TYPE, 't' )

        and: 'The second GlcProcedure is compiled as expected'
        procedures[ 1 ].closureName
        procedures[ 1 ].inputs == [ new GlcProcedureParameter( LIST_OF_STRINGS_TYPE, 'list' ),
                                    new GlcProcedureParameter( optionalOf( mapOf( INTEGER_TYPE, LONG_TYPE ) ), 'opt' ) ]
        procedures[ 1 ].output == new GlcProcedureParameter( mapOf( FLOAT_TYPE, DOUBLE_TYPE ), 'map' )

        and: 'The third GlcProcedure is compiled as expected'
        procedures[ 2 ].closureName
        procedures[ 2 ].inputs == [ new GlcProcedureParameter( LONG_TYPE, 'abc' ) ]
        procedures[ 2 ].output == new GlcProcedureParameter( INTEGER_TYPE, 'integer' )

        and: 'All GlcProcedures have a different name'
        procedures.collect { it.closureName }.unique().size() == 3
    }

    def "Invalid GLC procedures cannot be compiled"() {
        when: 'We try to compile an invalid GLC procedure'
        glc.compile( invalidProcedure )

        then: 'The expected error is reported'
        def error = thrown( GlcError )
        error.message == expectedError
        error.lineNumber == expectedLineNumber

        where:
        invalidProcedure     | expectedLineNumber | expectedError
        ''                   | -1                 | 'Error at line -1: Invalid GLC procedure. Not a closure.'
        '2'                  | 1                  | 'Error at line 1: Invalid GLC procedure. Not a closure.'
        '{->}'               | 1                  | 'Error at line 1: GLC procedure is empty.'
        '{String s -> 3}'    | 1                  | 'Error at line 1: GLC Procedure does not return a named variable.'
        '{String s ->\n' +
                'String t = "0"\n' +
                'return 4\n' +
                '}'          | 3                  | 'Error at line 3: GLC Procedure does not return a named variable.'
        '{String s -> s}'    | 1                  | 'Error at line 1: GLC Procedure depends on its own output.'
        '{String s ->\n' +
                'String t = "0"\n' +
                'return s\n' +
                '}'          | 3                  | 'Error at line 3: GLC Procedure depends on its own output.'
        'int i = 0;\n' +
                '{String s ->\n' +
                'String t = "0"\n' +
                'return t\n' +
                '}'          | 1                  | 'Error at line 1: Invalid GLC procedure. Not a closure.'
        '{String s ->\n' +
                'String t = "0"\n' +
                'return t\n' +
                '}\n' +
                'int i = 0;' | 5                  | 'Error at line 5: Invalid GLC procedure. Not a closure.'
    }

}
