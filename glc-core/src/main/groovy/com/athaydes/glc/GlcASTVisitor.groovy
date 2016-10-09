package com.athaydes.glc

import org.codehaus.groovy.control.SourceUnit

/**
 * A simple trait that helps implementing AST visitors.
 */
trait GlcASTVisitor {

    boolean enabled = true
    private final Set<String> visitedSourceUnits = [ ]

    boolean shouldVisit( SourceUnit source ) {
        enabled && visitedSourceUnits.add( source.name )
    }

}
