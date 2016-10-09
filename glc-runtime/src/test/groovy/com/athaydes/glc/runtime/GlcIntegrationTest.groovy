package com.athaydes.glc.runtime

import com.athaydes.glc.Glc
import spock.lang.Specification

class GlcIntegrationTest extends Specification implements GlcTest {

    final GlcRunner glcRunner = new GlcRunner()
    final Glc GLC = new Glc()

    def "Can execute GLC application using model entities"() {
        given: 'GLC Entities'
        GLC.compileGlcEntities( 'class Person { String name; int age }' )
        GLC.compileGlcEntities( 'class Work { String name; String description }\n' +
                'class WorkingPerson { Person person; Work profession }' )

        and: 'GLC Procedures using the GLC entities'
        def procedures = '''
        { Person joe ->
            WorkingPerson worker = null
            if (joe.age > 18) {
                def architect = new Work('architect', 'designs houses and other buildings')
                worker = new WorkingPerson(joe, architect)
            }
            return worker
        }
        ;
        { WorkingPerson worker ->
            String message = "${worker.person.name} has become an ${worker.profession.name}"
            return message
        }
        ;
        // get the procedures started
        { -> Person joe = new Person('Joe', 19); joe }
        '''

        def glcProcedures = GLC.compileGlcProcedures( procedures )

        when: 'The GLC Procedures are run'
        glcRunner.run( glcProcedures )

        then: 'The GLC Runner contains the expected entities and values'
        def joe = glcRunner.valueOf( GlcParameter( 'Person', 'joe' ) )
        joe.name == 'Joe'
        joe.age == 19

        def worker = glcRunner.valueOf( GlcParameter( 'WorkingPerson', 'worker' ) )
        worker.person == joe
        worker.profession.name == 'architect'
        worker.profession.description == 'designs houses and other buildings'

        def message = glcRunner.valueOf( GlcParameter( String, 'message' ) )
        message == 'Joe has become an architect'
    }

}
