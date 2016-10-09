package com.athaydes.glc.model

import com.athaydes.glc.GlcCompilationCustomizer
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

@CompileStatic
class GlcModelInterpreter {

    private final GroovyShell shell
    private final GlcModelEntities glcModelEntities

    GlcModelInterpreter() {
        final glcASTVisitor = new GlcModelASTVisitor()
        CompilationCustomizer glcUnitASTCustomizer = new GlcCompilationCustomizer( glcASTVisitor )
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer( methodDefinitionAllowed: false )
        //glcASTCustomizer.addStatementCheckers( new ASTPrinter() )

        def compilerConfig = new CompilerConfiguration()
        compilerConfig.addCompilationCustomizers( secureASTCustomizer, glcUnitASTCustomizer )

        this.shell = new GroovyShell( compilerConfig )
        this.glcModelEntities = glcASTVisitor
    }

    GlcModelEntities compile( String script ) {
        shell.evaluate( script )
        glcModelEntities
    }

}
