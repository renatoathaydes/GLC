package com.athaydes.glc.model

import com.athaydes.glc.GlcASTVisitor
import com.athaydes.glc.GlcError
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.PackageScope
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation

@CompileStatic
@PackageScope
class ImmutableApplierASTVisitor
        implements GlcASTVisitor, ASTTransformation {

    private final AnnotationNode immutableAnnotationNode = new AnnotationNode( ClassHelper.make( Immutable ) )

    @Override
    void visit( ASTNode[] nodes, SourceUnit source ) {
        if ( !shouldVisit( source ) ) {
            return
        }

        final classNodes = source.AST.classes

        for ( ClassNode node in classNodes ) {
            if ( node.superClass?.name == Script.name ) {
                continue // skip the Script class
            }

            def immutable = node.annotations.find { AnnotationNode -> node.typeClass == Immutable }

            if ( immutable && !immutable.members.isEmpty() ) {
                throw new GlcError( immutable.lineNumber,
                        "Problem on GLC model class ${node.name}. " +
                                "Immutable annotation cannot be customized in GLC model classes." )
            } else {
                node.addAnnotation( immutableAnnotationNode )
            }
        }
    }
}
