package com.athaydes.glc

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

/**
 *
 */
@CompileStatic
class Glc {

    private final GlcStatementChecker checker
    private final GroovyShell shell

    Glc() {
        this.checker = new GlcStatementChecker()

        SecureASTCustomizer glcASTCustomizer = new SecureASTCustomizer( methodDefinitionAllowed: false )
        glcASTCustomizer.addStatementCheckers( checker )

        def compilerConfig = new CompilerConfiguration()

        compilerConfig.addCompilationCustomizers( glcASTCustomizer )

        this.shell = new GroovyShell( compilerConfig )
    }

    GlcProcedure compile( String script ) {
        try {
            def runnable = shell.evaluate( script )
            return checker.getGlcProcedureFor( runnable as Closure )
        } finally {
            checker.reset()
        }
    }

}

@CompileStatic
@PackageScope
class GlcStatementChecker implements SecureASTCustomizer.StatementChecker {

    @SuppressWarnings( "GrFinalVariableAccess" )
    final class State {
        final boolean parsingTopLevelBlock
        final boolean parsingTopLevelClosure
        final boolean returnStatementFound

        // implementation required to work with @CompileStatic
        State( Map<String, Boolean> args ) {
            assert args.size() <= 1
            this.parsingTopLevelBlock = args.getOrDefault( 'parsingTopLevelBlock', false )
            this.parsingTopLevelClosure = args.getOrDefault( 'parsingTopLevelClosure', false )
            this.returnStatementFound = args.getOrDefault( 'returnStatementFound', false )
        }

    }

    private State state = new State( parsingTopLevelBlock: true )
    private Statement currentStatement
    private List<GlcProcedureParameter> parameters

    void reset() {
        state = new State( parsingTopLevelBlock: true )
        parameters = null
    }

    GlcProcedure getGlcProcedureFor( Closure procedure ) {
        if ( parameters == null ) {
            throw new IllegalStateException( "Must compile procedure before trying to read it" )
        }

        println "The last statement was $currentStatement"

        if ( currentStatement instanceof BlockStatement ) {
            final block = currentStatement as BlockStatement
            if ( !block.statements.isEmpty() ) {
                currentStatement = block.statements.last()
            } else {
                throw new AssertionError( error( currentStatement, "Procedure is empty" ) )
            }
        }

        if ( !( currentStatement instanceof ReturnStatement ) ) {
            throw new AssertionError( error( currentStatement, "No return statement" ) )
        }

        Expression expression = ( currentStatement as ReturnStatement ).expression

        if ( expression instanceof DeclarationExpression ) {
            expression = ( expression as DeclarationExpression ).variableExpression
        }

        if ( expression instanceof VariableExpression ) {
            final varExp = expression as VariableExpression
            final output = new GlcProcedureParameter( GenericType.create( varExp.type ), varExp.name )
            return new GlcProcedure( parameters, output, procedure )
        } else {
            throw new AssertionError( error( currentStatement, "Procedure does not return a named variable." ) )
        }
    }

    @Override
    boolean isAuthorized( Statement statement ) {
        final generalError = 'Invalid GLC procedure. Not a single closure'
        if ( state.parsingTopLevelBlock ) {
            assert statement instanceof BlockStatement, error( statement, generalError )
            assert statement.statements.size() == 1, error( statement, generalError )
            state = new State( parsingTopLevelClosure: true )
        } else if ( state.parsingTopLevelClosure ) {
            assert statement instanceof ExpressionStatement, error( statement, generalError )
            assert statement.expression instanceof ClosureExpression, error( statement, generalError )
            def closure = statement.expression as ClosureExpression

            List<GlcProcedureParameter> parameters = closure.parameters.collect { Parameter parameter ->
                new GlcProcedureParameter( GenericType.create( parameter.type ), parameter.name )
            }

            this.parameters = parameters

            println "Closure parameters: $parameters"
            println "Closure code: ${closure.code}"

            state = new State( [ : ] )
        } else if ( state.returnStatementFound ) {
            throw new AssertionError( error( statement, "Return statement must be the last statement in a procedure." ) )
        } else {
            if ( statement instanceof ReturnStatement ) {
                state = new State( returnStatementFound: true )
                println "RETURN STATEMENT: $statement"
            } else {
                println "VALID STATEMENT: $statement"
            }

            currentStatement = statement
        }

        return true
    }

    static def error( Statement statement, String message ) {
        "Error at line ${statement?.lineNumber ?: 1}: $message"
    }

}
