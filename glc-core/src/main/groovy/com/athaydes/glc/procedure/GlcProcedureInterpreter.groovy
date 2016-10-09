package com.athaydes.glc.procedure

import com.athaydes.glc.GlcCompilationCustomizer
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.CompilationCustomizer

/**
 *
 */
@CompileStatic
class GlcProcedureInterpreter {

    private final GlcProcedureASTVisitor glcProceduresASTVisitor

    GlcProcedureInterpreter( CompilerConfiguration compilerConfiguration ) {
        this.glcProceduresASTVisitor = new GlcProcedureASTVisitor()
        CompilationCustomizer glcUnitASTCustomizer = new GlcCompilationCustomizer( glcProceduresASTVisitor )
        compilerConfiguration.addCompilationCustomizers( glcUnitASTCustomizer )
    }

    GlcProcedures compile( GroovyShell shell, String script ) {
        shell.evaluate( script )
        new GlcProcedures( allProcedures.collect { CompiledGlcProcedure procedure ->
            final runnable = shell.getVariable( procedure.closureName ) as Closure
            new GlcProcedure( procedure, runnable )
        } as List<GlcProcedure> )
    }

    List<CompiledGlcProcedure> getAllProcedures() {
        glcProceduresASTVisitor.allProcedures
    }

    void setEnabled( boolean enabled ) {
        glcProceduresASTVisitor.enabled = enabled
    }

}
