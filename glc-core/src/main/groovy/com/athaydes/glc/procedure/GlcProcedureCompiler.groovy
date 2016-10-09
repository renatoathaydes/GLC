package com.athaydes.glc.procedure

import com.athaydes.glc.GlcError
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

import static com.athaydes.glc.GlcError.preCondition

@CompileStatic
@PackageScope
class GlcProcedureCompiler {

    static final String GLC_CLOSURE_NAME_PREFIX = '___glc_procedure___'
    static AtomicInteger closureCounter = new AtomicInteger( 0 )

    List<CompiledGlcProcedure> compile( Statement statement,
                                        List<CompiledGlcProcedure> existingProcedures ) {
        final List<CompiledGlcProcedure> result = [ ]

        final Set<GlcProcedureParameter> inputs = new HashSet<>(
                existingProcedures*.inputs.flatten() as List<GlcProcedureParameter> )
        final Set<GlcProcedureParameter> outputs = new HashSet<>(
                existingProcedures*.output as List<GlcProcedureParameter> )

        preCondition( statement instanceof BlockStatement, statement.lineNumber )
        final List<Statement> topLevelStatements = ( ( BlockStatement ) statement ).statements

        topLevelStatements.eachWithIndex { Statement topLevelStatement, int index ->
            preCondition( topLevelStatement instanceof ExpressionStatement, topLevelStatement.lineNumber )
            def topLevelExpression = ( ExpressionStatement ) topLevelStatement

            preCondition( topLevelExpression.expression instanceof ClosureExpression, topLevelStatement.lineNumber )
            def closureExpression = ( ClosureExpression ) topLevelExpression.expression
            def closureName = GLC_CLOSURE_NAME_PREFIX + closureCounter.getAndIncrement()
            def closureStatement = namedClosure( closureExpression, closureName )
            topLevelStatements.set( index, closureStatement )

            final glcProcedure = createCompiledGlcProcedure( closureExpression, closureName )
            verifyParameters( glcProcedure, closureExpression, inputs, outputs )
            result << glcProcedure
        }

        return result
    }

    private void verifyParameters( CompiledGlcProcedure glcProcedure,
                                   ClosureExpression closureExpression,
                                   Set<GlcProcedureParameter> inputs,
                                   Set<GlcProcedureParameter> outputs ) {

        final Set<String> duplicatedInputs = [ ]
        String duplicatedOutput = ''

        glcProcedure.inputs.each { GlcProcedureParameter input ->
            def added = inputs.add( input )
            if ( !added ) {
                duplicatedInputs << input.name
            }
        }

        def addedOutput = outputs.add( glcProcedure.output )
        if ( !addedOutput ) {
            duplicatedOutput = glcProcedure.output.name
        }

        if ( duplicatedInputs || duplicatedOutput ) {
            int lineNumber = duplicatedInputs.empty ?
                    ( ( BlockStatement ) closureExpression.code ).statements.last().lineNumber :
                    closureExpression.lineNumber

            throw new GlcError( lineNumber, "Detected duplicated GLC Procedure parameters." +
                    ( duplicatedInputs ? "\nDuplicated inputs: $duplicatedInputs" : '' ) +
                    ( duplicatedOutput ? "\nDuplicated output: $duplicatedOutput" : '' ) )
        }
    }

    private static Statement namedClosure( ClosureExpression closureExpression, String name ) {
        new ExpressionStatement( new BinaryExpression(
                new VariableExpression( name ),
                new Token( Types.ASSIGN, '=', closureExpression.lineNumber, 1 ),
                closureExpression ) )
    }

    private static CompiledGlcProcedure createCompiledGlcProcedure( ClosureExpression closureExpression,
                                                                    String closureName ) {
        def closureStatements = ( ( BlockStatement ) closureExpression.code ).statements

        preCondition( !closureStatements.isEmpty(), closureExpression.lineNumber,
                'GLC procedure is empty.' )

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

            final varExp = ( VariableExpression ) expression
            final output = new GlcProcedureParameter( GenericType.create( varExp.type ), varExp.name )
            if ( output in parameters ) {
                throw new GlcError( expression.lineNumber, "GLC Procedure depends on its own output." )
            }
            return new CompiledGlcProcedure( closureName, parameters, output )
        } else {
            throw new GlcError( lastStatement.lineNumber, "GLC Procedure does not return a named variable." )
        }
    }

}
