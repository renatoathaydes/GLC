package com.athaydes.glc

import com.athaydes.glc.procedure.CompiledGlcProcedure
import com.athaydes.glc.procedure.GlcProcedureInterpreter
import com.athaydes.glc.procedure.GlcProcedures
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

/**
 * Groovy Logic Controller.
 */
@CompileStatic
class Glc {

    private final GlcProcedureInterpreter glcProcedureInterpreter

    Glc() {
        glcProcedureInterpreter = new GlcProcedureInterpreter()
    }

    GlcProcedures compileGlcProcedures( String script ) {
        glcProcedureInterpreter.compile( script )
    }

    @PackageScope
    List<CompiledGlcProcedure> getAllProcedures() {
        glcProcedureInterpreter.allProcedures
    }

}

@CompileStatic
@Canonical
class GlcError extends Error {

    static final String generalError = 'Invalid GLC procedure. Not a closure.'

    final int lineNumber

    static String errorMessage( int lineNumber, String message ) {
        "Error at line $lineNumber: $message"
    }

    static void preCondition( condition, int lineNumber, String error = generalError ) {
        if ( !condition ) {
            throw new GlcError( lineNumber, error )
        }
    }

    GlcError( int lineNumber, String message ) {
        super( errorMessage( lineNumber, message ) )
        this.lineNumber = lineNumber
    }
}


@PackageScope
class ASTPrinter implements SecureASTCustomizer.StatementChecker {
    @Override
    boolean isAuthorized( Statement expression ) {
        println "-- $expression"
        true
    }
}

