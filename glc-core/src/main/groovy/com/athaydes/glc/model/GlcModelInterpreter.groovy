package com.athaydes.glc.model

import com.athaydes.glc.GlcCompilationCustomizer
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.CompilationCustomizer

@CompileStatic
class GlcModelInterpreter {

    private final GlcModelASTVisitor glcModelASTVisitor
    private final ImmutableApplierASTVisitor immutableApplierASTVisitor

    GlcModelInterpreter( CompilerConfiguration compilerConfiguration ) {
        this.immutableApplierASTVisitor = new ImmutableApplierASTVisitor()
        this.glcModelASTVisitor = new GlcModelASTVisitor()

        // Run in the CONVERSION Phase to allow us to add the @Immutable annotation to the model classes
        CompilationCustomizer immutableASTCustomizer = new GlcCompilationCustomizer(
                CompilePhase.CONVERSION, immutableApplierASTVisitor )

        CompilationCustomizer glcUnitASTCustomizer = new GlcCompilationCustomizer( glcModelASTVisitor )

        compilerConfiguration.addCompilationCustomizers(
                immutableASTCustomizer, glcUnitASTCustomizer )
    }

    void setEnabled( boolean enabled ) {
        immutableApplierASTVisitor.enabled = enabled
        glcModelASTVisitor.enabled = enabled
    }

    GlcModelEntities compile( GroovyShell shell, String script ) {
        shell.evaluate( script )
        glcModelASTVisitor
    }

}
