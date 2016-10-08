package com.athaydes.glc

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

import java.util.concurrent.atomic.AtomicInteger

/**
 *
 */
@CompileStatic
@PackageScope
class GlcProcedureCompiler {

    static final String generalError = 'Invalid GLC procedure. Not a single closure'
    static final String GLC_CLOSURE_NAME_PREFIX = '___glc_procedure___'
    static AtomicInteger closureCounter = new AtomicInteger( 0 )

    List<CompiledGlcProcedure> compile( Statement statement ) {
        final List<CompiledGlcProcedure> result = [ ]

        assert statement instanceof BlockStatement, error( statement, generalError )
        final List<Statement> topLevelStatements = statement.statements

        topLevelStatements.eachWithIndex { Statement topLevelStatement, int index ->
            assert topLevelStatement instanceof ExpressionStatement, error( topLevelStatement, generalError )
            def topLevelExpression = ( ExpressionStatement ) topLevelStatement
            assert topLevelExpression.expression instanceof ClosureExpression, error( topLevelStatement, generalError )
            def closureExpression = ( ClosureExpression ) topLevelExpression.expression
            def closureName = GLC_CLOSURE_NAME_PREFIX + closureCounter.getAndIncrement()
            def closureStatement = namedClosure( closureExpression, closureName )
            topLevelStatements.set( index, closureStatement )

            result << createCompiledGlcProcedure( closureExpression, closureName )
        }

        return result
    }

    private static Statement namedClosure( ClosureExpression closureExpression, String name ) {
        new ExpressionStatement( new BinaryExpression(
                new VariableExpression( name ),
                new Token( Types.ASSIGN, '=', closureExpression.lineNumber, 1 ),
                closureExpression ) )
    }

    private static CompiledGlcProcedure createCompiledGlcProcedure( ClosureExpression closureExpression,
                                                                    String closureName ) {
        def closureStatements = ( closureExpression.code as BlockStatement ).statements

        def lastStatement = closureStatements.last()

        Expression expression

        if ( lastStatement instanceof ReturnStatement ) {
            expression = ( lastStatement as ReturnStatement ).expression
        } else if ( lastStatement instanceof ExpressionStatement ) {
            expression = ( lastStatement as ExpressionStatement ).expression
        } else {
            expression = null
        }

        if ( expression instanceof VariableExpression ) {
            List<GlcProcedureParameter> parameters = closureExpression.parameters.collect { Parameter parameter ->
                new GlcProcedureParameter( GenericType.create( parameter.type ), parameter.name )
            }

            final varExp = expression as VariableExpression
            final output = new GlcProcedureParameter( GenericType.create( varExp.type ), varExp.name )
            if ( output in parameters ) {
                throw new AssertionError( "Procedure depends on its own output" as Object )
            }
            return new CompiledGlcProcedure( closureName, parameters, output )
        } else {
            throw new AssertionError( error( lastStatement, "Procedure does not return a named variable." ) )
        }
    }

    private static def error( Statement statement, String message ) {
        "Error at line ${statement?.lineNumber ?: 1}: $message"
    }

}
