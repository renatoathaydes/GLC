package com.athaydes.glc.model

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.PackageScope
import groovy.transform.ToString

/**
 *
 */
@Immutable
@CompileStatic
@ToString( includePackage = false )
class GlcModelEntity {
    String name
}

@CompileStatic
@ToString( includePackage = false )
class GlcModelEntities {
    private final Set<GlcModelEntity> entities = [ ]
    private final Set<String> classNames = [ ]

    @PackageScope
    void add( GlcModelEntity entity ) {
        entities << entity
        classNames << entity.name
    }

    Set<GlcModelEntity> getAllEntities() {
        entities.asImmutable()
    }

    boolean contains( String className ) {
        className in classNames
    }

}
