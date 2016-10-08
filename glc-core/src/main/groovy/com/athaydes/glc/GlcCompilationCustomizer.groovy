package com.athaydes.glc

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer

/**
 *
 */
class GlcCompilationCustomizer extends CompilationCustomizer {

    GlcCompilationCustomizer() {
        super( CompilePhase.SEMANTIC_ANALYSIS )
    }

    @Override
    void call( SourceUnit source, GeneratorContext context, ClassNode classNode )
            throws CompilationFailedException {
        transformation.visit( null, source )
    }
}
