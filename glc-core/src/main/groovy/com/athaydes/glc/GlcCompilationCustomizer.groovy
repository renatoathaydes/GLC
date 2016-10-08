package com.athaydes.glc

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer

class GlcCompilationCustomizer extends CompilationCustomizer {

    private final GlcASTVisitor visitor

    GlcCompilationCustomizer( GlcASTVisitor visitor ) {
        super( CompilePhase.SEMANTIC_ANALYSIS )
        this.visitor = visitor
    }

    @Override
    void call( SourceUnit source, GeneratorContext context, ClassNode classNode )
            throws CompilationFailedException {
        visitor.visit( null, source )
    }
}