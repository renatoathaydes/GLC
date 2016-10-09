package com.athaydes.glc.model

import com.athaydes.glc.GlcError
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.util.logging.Slf4j
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation

/**
 *
 */
@Slf4j
@CompileStatic
class GlcModelASTVisitor extends GlcModelEntities implements ASTTransformation {

    static final String CODE_IN_SCRIPT_ERROR_MESSAGE =
            'Unexpected script code in GLC model classes compilation unit. ' +
                    'Only model class definitions expected.'
    private final Set<String> visitedSourceUnits = [ ]

    @Override
    void visit( ASTNode[] nodes, SourceUnit source ) {
        def alreadyVisited = !visitedSourceUnits.add( source.name )

        if ( alreadyVisited ) {
            log.debug( "Skipping check on compilation unit as it was already visited: {}", source.name )
            return
        }

        final classNodes = source.AST.classes

        final scriptClasses = classNodes.findAll { ClassNode n -> n?.superClass?.name == Script.name }

        verifyScriptClasses( scriptClasses )

        log.debug "------------------------ Visiting AST: {}", source.name
        log.debug "{} nodes found", classNodes.size()

        for ( ClassNode node in classNodes ) {
            if ( node in scriptClasses ) {
                return
            }

            log.debug( "Checking class {}", node )

            def immutable = node.annotations.find { AnnotationNode -> node.typeClass == Immutable }

            if ( immutable && !immutable.members.isEmpty() ) {
                throw new GlcError( immutable.lineNumber,
                        "Problem on GLC model class ${node.name}. " +
                                "Immutable annotation cannot be customized in GLC model classes." )
            } else {
                node.addAnnotation( new AnnotationNode( new ClassNode( Immutable ) ) )
            }

            if ( !node.methods.empty && node.methods*.name != [ '<clinit>' ] ) {
                def lineNumber = node.methods.first().lineNumber
                throw new GlcError( lineNumber, 'GLC model class may not define methods' )
            }

            add new GlcModelEntity( node.name )
        }

        source.AST.statementBlock.addStatement( new ReturnStatement( new ConstantExpression( null, true ) ) )

        log.debug "------------------------ Done AST"
        log.trace "All GLC Entities: {}", allEntities
    }

    private static void verifyScriptClasses( List<ClassNode> scriptClasses ) {
        if ( scriptClasses.size() > 1 ) {
            throw new GlcError( 1, 'Compilation Unit contains Script classes: ' + scriptClasses )
        } else if ( scriptClasses.size() == 1 ) {
            def runMethodStatements = ( (
                    scriptClasses.first().methods.find { it.name == 'run' }?.code as BlockStatement
            )?.statements ?: [ ] ) as List<Statement>

            if ( runMethodStatements ) {
                boolean invalidRunMethod = false

                if ( runMethodStatements.size() > 1 ) {
                    log.warn( "Script.run() method contains several statements." )
                    invalidRunMethod = true
                } else {
                    final lastStatement = runMethodStatements.first()
                    if ( lastStatement instanceof ReturnStatement ) {
                        final returnStatement = ( ReturnStatement ) lastStatement
                        boolean nullStatement = returnStatement.expression instanceof ConstantExpression &&
                                ( returnStatement.expression as ConstantExpression ).value == null
                        if ( !nullStatement ) {
                            log.warn( "Script.run() method contains an invalid return statement." )
                            invalidRunMethod = true
                        }
                    } else {
                        log.warn( "Script.run() method contains unexpected statement of type {}.", lastStatement?.class )
                        invalidRunMethod = true
                    }
                }

                if ( invalidRunMethod ) {
                    throw new GlcError( runMethodStatements.first().lineNumber, CODE_IN_SCRIPT_ERROR_MESSAGE )
                }
            }
        }
    }
}
