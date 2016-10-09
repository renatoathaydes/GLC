package com.athaydes.glc

import com.athaydes.glc.procedure.CompiledGlcProcedure
import com.athaydes.glc.procedure.GenericType
import com.athaydes.glc.procedure.GlcProcedureParameter
import spock.lang.Specification
import spock.lang.Subject

class GlcCompilerSpec extends Specification {

    static final GenericType STRING_TYPE = new GenericType( String.name, GenericType.EMPTY )
    static final GenericType INTEGER_TYPE = new GenericType( Integer.name, GenericType.EMPTY )
    static final GenericType FLOAT_TYPE = new GenericType( Float.name, GenericType.EMPTY )
    static final GenericType DOUBLE_TYPE = new GenericType( Double.name, GenericType.EMPTY )
    static final GenericType LONG_TYPE = new GenericType( Long.name, GenericType.EMPTY )

    static final GenericType LIST_OF_STRINGS_TYPE = new GenericType( List.name, [ STRING_TYPE ] as GenericType[] )
    static final GenericType LIST_OF_INTS_TYPE = new GenericType( List.name, [ INTEGER_TYPE ] as GenericType[] )
    static final GenericType LIST_OF_FLOATS_TYPE = new GenericType( List.name, [ FLOAT_TYPE ] as GenericType[] )

    static GenericType optionalOf( GenericType genericType ) {
        new GenericType( Optional.name, [ genericType ] as GenericType[] )
    }

    static GenericType mapOf( GenericType keyType, GenericType valueType ) {
        new GenericType( Map.name, [ keyType, valueType ] as GenericType[] )
    }

    @Subject
    Glc glc = new Glc()

    def "A simple GLC procedure can be compiled"() {
        when: 'A GLC Procedure is compiled'
        glc.compileGlcProcedures( '{ String s -> String t = s + "!"; return t }' )
        CompiledGlcProcedure procedure = glc.allProcedures.last()

        then: 'The correct number of procedures is compiled'
        glc.allProcedures.size() == 1

        and: 'The GlcProcedures class provides the latest procedure as expected'
        procedure.closureName
        procedure.inputs == [ new GlcProcedureParameter( STRING_TYPE, 's' ) ]
        procedure.output == new GlcProcedureParameter( STRING_TYPE, 't' )
    }

    def "A GLC procedure without input can be compiled"() {
        when: 'A GLC Procedure is compiled'
        glc.compileGlcProcedures( '{ -> String t = "!"; t }' )
        CompiledGlcProcedure procedure = glc.allProcedures.last()

        then: 'The correct number of procedures is compiled'
        glc.allProcedures.size() == 1

        and: 'The GlcProcedures class provides the latest procedure as expected'
        procedure.closureName
        procedure.inputs == [ ]
        procedure.output == new GlcProcedureParameter( STRING_TYPE, 't' )
    }

    def "A GLC procedure using generic types can be compiled"() {
        when: 'A GLC Procedure with generic types is compiled'
        glc.compileGlcProcedures( '{ List<String> list, Optional<Map<Integer, Long>> opt -> ' +
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
        glc.compileGlcProcedures( '{ String s -> String t = s + "!"; return t };' +
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

    def "Different GLC procedures using inputs with generic types can be compiled"() {
        when:
        'GLC Procedures whose inputs are different only by the generic type parameter ' +
                'are compiled in the same compilation unit'
        glc.compileGlcProcedures( '{ List<String> list -> String t = s + "!"; return t };' +
                '{ List<Integer> list, Optional<Map<Integer, Long>> opt -> ' +
                'Map<Float, Double> map = [0f:2d]; return map };\n\n' +
                '{ List<Float> list -> int integer= 0; integer };' )

        List<CompiledGlcProcedure> procedures = glc.allProcedures

        then: 'The correct number of procedures is compiled'
        procedures.size() == 3

        then: 'The first GlcProcedure is compiled as expected'
        procedures[ 0 ].closureName
        procedures[ 0 ].inputs == [ new GlcProcedureParameter( LIST_OF_STRINGS_TYPE, 'list' ) ]
        procedures[ 0 ].output == new GlcProcedureParameter( STRING_TYPE, 't' )

        and: 'The second GlcProcedure is compiled as expected'
        procedures[ 1 ].closureName
        procedures[ 1 ].inputs == [ new GlcProcedureParameter( LIST_OF_INTS_TYPE, 'list' ),
                                    new GlcProcedureParameter( optionalOf( mapOf( INTEGER_TYPE, LONG_TYPE ) ), 'opt' ) ]
        procedures[ 1 ].output == new GlcProcedureParameter( mapOf( FLOAT_TYPE, DOUBLE_TYPE ), 'map' )

        and: 'The third GlcProcedure is compiled as expected'
        procedures[ 2 ].closureName
        procedures[ 2 ].inputs == [ new GlcProcedureParameter( LIST_OF_FLOATS_TYPE, 'list' ) ]
        procedures[ 2 ].output == new GlcProcedureParameter( INTEGER_TYPE, 'integer' )

        and: 'All GlcProcedures have a different name'
        procedures.collect { it.closureName }.unique().size() == 3
    }

    def "Invalid GLC procedures cannot be compiled"() {
        when: 'We try to compile an invalid GLC procedure'
        glc.compileGlcProcedures( invalidProcedure )

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
        '{String s ->\n' +
                'String t = "0"\n' +
                'return t\n' +
                '};\n' +
                '{String s ->\n' +
                'String v = "0"\n' +
                'return v\n' +
                '}'          | 5                  | 'Error at line 5: Detected duplicated GLC Procedure parameters.\nDuplicated inputs: [s]'
        '{String s ->\n' +
                'String t = "0"\n' +
                'return t\n' +
                '};\n' +
                '{String m ->\n' +
                'String t = "0"\n' +
                'return t\n' +
                '}'          | 7                  | 'Error at line 7: Detected duplicated GLC Procedure parameters.\nDuplicated output: t'
        '''{ String s, int b ->
               String t = "0"
               return t
           };
           { String s, int b ->
               String t = "0"
               return t
           };
           { String a ->
               String v = "0"
               return v
           };
           { String b ->
               String v = "0"
               return v
           }'''   | 5                  | 'Error at line 5: Detected duplicated GLC Procedure parameters.\n' +
                'Duplicated inputs: [s, b]\n' +
                'Duplicated output: t'
    }

}
