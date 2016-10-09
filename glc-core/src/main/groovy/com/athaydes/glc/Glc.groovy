package com.athaydes.glc

import com.athaydes.glc.model.GlcModelEntities
import com.athaydes.glc.model.GlcModelInterpreter
import com.athaydes.glc.procedure.CompiledGlcProcedure
import com.athaydes.glc.procedure.GlcProcedureInterpreter
import com.athaydes.glc.procedure.GlcProcedures
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

/**
 * Groovy Logic Controller.
 */
@CompileStatic
class Glc {

    private final GlcProcedureInterpreter glcProcedureInterpreter
    private final GlcModelInterpreter glcModelInterpreter
    private final GroovyShell shell

    Glc() {
        final compilerConfig = new CompilerConfiguration()
        glcProcedureInterpreter = new GlcProcedureInterpreter( compilerConfig )
        glcModelInterpreter = new GlcModelInterpreter( compilerConfig )

        this.shell = new GroovyShell( compilerConfig )
    }

    GlcProcedures compileGlcProcedures( String glcProceduresScript ) {
        glcModelInterpreter.enabled = false
        glcProcedureInterpreter.enabled = true
        glcProcedureInterpreter.compile( shell, glcProceduresScript )
    }

    GlcModelEntities compileGlcEntities( String glcModelEntitiesScript ) {
        glcModelInterpreter.enabled = true
        glcProcedureInterpreter.enabled = false
        glcModelInterpreter.compile( shell, glcModelEntitiesScript )
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

