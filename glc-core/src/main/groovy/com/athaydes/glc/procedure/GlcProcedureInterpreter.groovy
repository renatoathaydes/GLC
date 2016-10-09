package com.athaydes.glc.procedure

import com.athaydes.glc.GlcCompilationCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

/**
 *
 */
class GlcProcedureInterpreter {

    private final GroovyShell shell
    private final CompiledGlcProcedures glcProcedures

    GlcProcedureInterpreter() {
        final glcASTVisitor = new GlcProcedureASTVisitor()
        CompilationCustomizer glcUnitASTCustomizer = new GlcCompilationCustomizer( glcASTVisitor )
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer( methodDefinitionAllowed: false )
        //glcASTCustomizer.addStatementCheckers( new ASTPrinter() )

        def compilerConfig = new CompilerConfiguration()
        compilerConfig.addCompilationCustomizers( secureASTCustomizer, glcUnitASTCustomizer )

        this.shell = new GroovyShell( compilerConfig )
        this.glcProcedures = glcASTVisitor
    }

    GlcProcedures compile( String script ) {
        shell.evaluate( script )
        new GlcProcedures( allProcedures.collect { CompiledGlcProcedure procedure ->
            final runnable = shell.getVariable( procedure.closureName ) as Closure
            new GlcProcedure( procedure, runnable )
        } as List<GlcProcedure> )
    }

    List<CompiledGlcProcedure> getAllProcedures() {
        glcProcedures.allProcedures
    }

}
