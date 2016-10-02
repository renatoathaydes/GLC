package com.athaydes.glc

import groovy.transform.CompileStatic
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

    private final GlcStatementChecker glcStatementChecker = new GlcStatementChecker()

    void run() {
        def glcASTCustomizer = new SecureASTCustomizer(
                methodDefinitionAllowed: false
        )

        glcASTCustomizer.addStatementCheckers( glcStatementChecker )

        def compilerConfig = new CompilerConfiguration()
        compilerConfig.addCompilationCustomizers( glcASTCustomizer )
        def shell = new GroovyShell( compilerConfig )

        String input
        def nextLine = {
            print "glc > "
            System.in.newReader().readLine()
        }

        while ( ( input = nextLine() ) != 'exit' ) {
            try {
                def result = shell.evaluate( input )
                assert result instanceof Closure
                println "GLC expression compiled ok!"
                println "Closure returns ${glcStatementChecker.outputNode}"
            } catch ( Throwable t ) {
                println "ERROR: $t"
            } finally {
                glcStatementChecker.reset()
            }
        }

        println "Bye!"
    }

    static void main( String[] args ) {
        new Glc().run()
    }
}

class GlcStatementChecker implements SecureASTCustomizer.StatementChecker {

    class State {
        boolean parsingTopLevelBlock
        boolean parsingTopLevelClosure
        boolean returnStatementFound
    }

    private State state = new State( parsingTopLevelBlock: true )
    private Statement currentStatement

    void reset() {
        state = new State( parsingTopLevelBlock: true )
    }

    Map getOutputNode() {
        if ( currentStatement instanceof BlockStatement ) {
            currentStatement = currentStatement.statements.last()
        }

        Expression expression =
                currentStatement instanceof ReturnStatement ?
                        currentStatement.expression :
                        currentStatement instanceof ExpressionStatement ?
                                currentStatement.expression :
                                null

        println "LAST Expression is $expression"

        if ( expression instanceof DeclarationExpression ) {
            expression = expression.variableExpression
        }

        if ( expression instanceof VariableExpression ) {
            [ type    : expression.type.typeClass,
              generics: expression.type.genericsTypes,
              name    : expression.name ]
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

            List<Map> parameters = [ ]
            for ( parameter in closure.parameters ) {
                parameters << [
                        type    : parameter.type.typeClass,
                        generics: parameter.type.genericsTypes,
                        name    : parameter.name
                ]
            }

            println "Closure parameters: $parameters"
            println "Closure code: ${closure.code}"

            state = new State()
        } else if ( state.returnStatementFound ) {
            throw new AssertionError( error( statement, "Return statement must be the last statement in a procedure." ) )
        } else if ( statement instanceof ReturnStatement ) {
            state = new State( returnStatementFound: true )
        } else {
            println "VALID STATEMENT: $statement"
            currentStatement = statement
        }

        return true
    }

    static String error( Statement statement, String message ) {
        "Error at line ${statement?.lineNumber ?: 1}: $message"
    }

}
