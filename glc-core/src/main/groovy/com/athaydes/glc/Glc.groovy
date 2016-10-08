package com.athaydes.glc

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * Groovy Logic Controller.
 */
@CompileStatic
class Glc {

    private final GroovyShell shell
    private final GlcProcedures glcProcedures

    Glc() {
        final glcASTVisitor = new GlcASTVisitor()
        CompilationCustomizer glcUnitASTCustomizer = new GlcCompilationCustomizer( glcASTVisitor )
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer( methodDefinitionAllowed: false )
        //glcASTCustomizer.addStatementCheckers( new ASTPrinter() )

        def compilerConfig = new CompilerConfiguration()
        compilerConfig.addCompilationCustomizers( secureASTCustomizer, glcUnitASTCustomizer )

        this.shell = new GroovyShell( compilerConfig )
        this.glcProcedures = glcASTVisitor
    }

    List<GlcProcedure> compile( String script ) {
        shell.evaluate( script )
        allProcedures.collect { CompiledGlcProcedure procedure ->
            final runnable = shell.getVariable( procedure.closureName ) as Closure
            new GlcProcedure( procedure, runnable )
        } as List<GlcProcedure>
    }

    @PackageScope
    List<CompiledGlcProcedure> getAllProcedures() {
        glcProcedures.allProcedures
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

@CompileStatic
@PackageScope
class GlcProcedures {
    private final List<CompiledGlcProcedure> compiledGlcProcedures = [ ]

    void add( List<CompiledGlcProcedure> procedures ) {
        compiledGlcProcedures.addAll( procedures )
    }

    List<CompiledGlcProcedure> getAllProcedures() {
        compiledGlcProcedures.asImmutable()
    }
}

@GroovyASTTransformation( phase = CompilePhase.SEMANTIC_ANALYSIS )
@Slf4j
@CompileStatic
@PackageScope
class GlcASTVisitor extends GlcProcedures implements ASTTransformation {

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
                    add glcProcedureCompiler.compile( runMethod.code )
                }
            }
        }

        log.debug "------------------------ Done AST"
        log.trace "All procedures: {}", allProcedures
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

