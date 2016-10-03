package com.athaydes.glc

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.GenericsType

@CompileStatic
@Immutable
class GlcProcedure {
    List<GlcProcedureParameter> inputs
    GlcProcedureParameter output
    Closure runnable
}

@CompileStatic
@Immutable
class GlcProcedureParameter {
    GenericType type
    String name
}

@CompileStatic
@Immutable
class GenericType {
    static final GenericType[] EMPTY = new GenericType[0]

    Class<?> type
    GenericType[] parameters

    static GenericType create( ClassNode node ) {
        new GenericType( node.typeClass, parametersOf( node.genericsTypes ) )
    }

    static GenericType[] parametersOf( GenericsType[] genericsTypes ) {
        if ( genericsTypes == null || genericsTypes.length == 0 ) {
            return EMPTY
        } else {
            return genericsTypes.collect { GenericsType it -> create( it.type ) }
                    .toArray( new GenericType[genericsTypes.length] )
        }
    }
}
