package com.athaydes.glc

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.transform.ASTTransformation

@Slf4j
@CompileStatic
class GlcCompilationCustomizer extends CompilationCustomizer {

    private final ASTTransformation visitor

    GlcCompilationCustomizer( CompilePhase compilePhase, ASTTransformation visitor ) {
        super( compilePhase )
        this.visitor = visitor
    }

    GlcCompilationCustomizer( ASTTransformation visitor ) {
        this( CompilePhase.SEMANTIC_ANALYSIS, visitor )
    }

    @Override
    void call( SourceUnit source, GeneratorContext context, ClassNode classNode )
            throws CompilationFailedException {
        log.debug( "Compiling source unit: {}", source.name )
        visitor.visit( null, source )
    }
}