package com.athaydes.glc

import com.athaydes.glc.model.GlcModelASTVisitor
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class GlcModelSpec extends Specification {

    @Subject
    Glc glc = new Glc()

    @Unroll
    def "Simple GLC Model classes can be compiled"() {
        when: 'We compile GLC Model classes'
        def entities = glc.compileGlcEntities( script ).allEntities

        then: 'The classes compile and their names are correctly found'
        entities*.name == expectedEntities

        where:
        script                    | expectedEntities
        ''                        | [ ]
        'class A {}'              | [ 'A' ]
        'class A {}\nclass B {}'  | [ 'A', 'B' ]
        'class A { String name }' | [ 'A' ]
        '''
        class Hello {
          String person
          int age
          float percentage
          List<String> friends
        }'''           | [ 'Hello' ]
        '''
        class Name {
            String first
            String last
            Character initial
        }
        class Person {
          Name name
          int age
          float percentage
          List<Person> friends
        }'''           | [ 'Name', 'Person' ]
    }

    @Unroll
    def "Invalid GLC Model classes cannot be compiled"() {
        when: 'We try to compile invalid GLC Model classes'
        glc.compileGlcEntities( script )

        then: 'The expected error is thrown'
        def error = thrown( GlcError )
        error.message == expectedErrorMessage

        where:
        script                     | expectedErrorMessage
        'def x = 0'                | 'Error at line 1: ' + GlcModelASTVisitor.CODE_IN_SCRIPT_ERROR_MESSAGE
        'x = 0'                    | 'Error at line 1: ' + GlcModelASTVisitor.CODE_IN_SCRIPT_ERROR_MESSAGE
        'class A { void hi() {} }' | 'Error at line 1: GLC model class may not define methods.'
        '''
        class A {
          int i
          void setI(int i) {
            this.i = i
          }
        }
        '''             | 'Error at line 4: GLC model class may not define methods.'
    }

}
