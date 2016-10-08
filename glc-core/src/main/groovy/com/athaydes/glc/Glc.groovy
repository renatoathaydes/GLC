package com.athaydes.glc

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
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
        ASTTransformationCustomizer glcUnitASTCustomizer = new ASTTransformationCustomizer( glcASTVisitor )
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
            throw new AssertionError( ( Object ) ( 'Compilation Unit contains unrecognized classes: ' + unrecognizedClasses ) )
        }

        println "------------------------ Visiting AST: ${source}"
        println "${classNodes.size()} nodes found"
        classNodes.each { node ->
            if ( node instanceof ClassNode ) {
                def runMethod = node.methods.find { it.name == 'run' && it.parameters.size() == 0 }
                if ( runMethod ) {
                    println "Run method found: $runMethod"
                    println "Code ${runMethod.code}"
                    add glcProcedureCompiler.compile( runMethod.code )
                }
            }
        }
        println "------------------------ Done AST"

        println "All procedures: ${allProcedures}"
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

