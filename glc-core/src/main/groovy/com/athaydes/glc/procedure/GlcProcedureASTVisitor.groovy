package com.athaydes.glc.procedure

import com.athaydes.glc.GlcError
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation

/**
 *
 */
@Slf4j
@CompileStatic
@PackageScope
class GlcProcedureASTVisitor extends CompiledGlcProcedures implements ASTTransformation {

    private final GlcProcedureCompiler glcProcedureCompiler = new GlcProcedureCompiler()

    @Override
    void visit( ASTNode[] nodes, SourceUnit source ) {
        final classNodes = source.AST.classes

        final unrecognizedClasses = classNodes.collect { ClassNode n -> n?.superClass?.name }
                .find { String name -> name != Script.name }

        if ( unrecognizedClasses ) {
            throw new GlcError( 1, 'Compilation Unit contains unrecognized classes: ' + unrecognizedClasses )
        }

        log.debug "------------------------ Visiting AST: {}", source
        log.debug "{} nodes found", classNodes.size()

        classNodes.each { node ->
            if ( node instanceof ClassNode ) {
                def runMethod = node.methods.find { it.name == 'run' && it.parameters.size() == 0 }
                if ( runMethod ) {
                    log.debug "Run method found: {}", runMethod
                    log.trace "Code {}", runMethod.code
                    add glcProcedureCompiler.compile( runMethod.code, allProcedures )
                }
            }
        }

        log.debug "------------------------ Done AST"
        log.trace "All procedures: {}", allProcedures
    }
}
