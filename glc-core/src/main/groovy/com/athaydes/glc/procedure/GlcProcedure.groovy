package com.athaydes.glc.procedure

import com.athaydes.glc.GlcError
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.PackageScope
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression

@CompileStatic
@Immutable
@ToString( includePackage = false )
class GlcProcedure {
    @Delegate
    CompiledGlcProcedure procedure
    Closure runnable
}

@CompileStatic
@Immutable
@ToString( includePackage = false )
class CompiledGlcProcedure {
    String closureName
    List<GlcProcedureParameter> inputs
    GlcProcedureParameter output
}

@CompileStatic
@Immutable
@ToString( includePackage = false )
class GlcProcedureParameter {
    GenericType type
    String name
}

@CompileStatic
@Immutable
@ToString( includePackage = false )
class AnnotationInfo {
    GenericType type
    Map<String, Object> members
}

@Slf4j
@CompileStatic
@Immutable
@ToString( includePackage = false )
class GenericType {
    static final GenericType[] EMPTY = new GenericType[0]

    String type
    GenericType[] parameters
    List<AnnotationInfo> annotations

    static GenericType create( ClassNode node, List<AnnotationNode> annotationNodes = [ ] ) {
        final annotations = annotationNodes.collect { AnnotationNode a ->
            def members = a.members.collectEntries { String k, Expression v ->
                if ( v instanceof ConstantExpression ) {
                    [ k, ( ( ConstantExpression ) v ).value ]
                } else {
                    log.warn( "Annotation {} on {} has non-constant expression: {}", a.classNode, node, v )
                    throw new GlcError( v.lineNumber, "Annotation members must contain constants only" )
                }
            } as Map<String, Object>

            new AnnotationInfo( create( a.classNode ), members )
        } as List<AnnotationInfo>

        new GenericType( node.typeClass.name, parametersOf( node.genericsTypes ), annotations )
    }

    static GenericType[] parametersOf( GenericsType[] genericsTypes ) {
        if ( genericsTypes == null || genericsTypes.length == 0 ) {
            return EMPTY
        } else {
            return genericsTypes.collect { GenericsType it -> create( it.type ) }
                    .toArray( new GenericType[genericsTypes.length] )
        }
    }
}

@CompileStatic
@ToString( includePackage = false )
class GlcProcedures {

    private final Map<GlcProcedureParameter, GlcProcedure> inputMap
    private final Map<GlcProcedureParameter, GlcProcedure> outputMap

    final List<GlcProcedure> emptyInputProcedures
    final List<GlcProcedure> allProcedures

    GlcProcedures( List<GlcProcedure> glcProcedures ) {
        Map<GlcProcedureParameter, GlcProcedure> inputMap = [ : ]
        Map<GlcProcedureParameter, GlcProcedure> outputMap = [ : ]

        for ( GlcProcedure procedure in glcProcedures ) {
            for ( input in procedure.inputs ) {
                inputMap[ ( GlcProcedureParameter ) input ] = procedure
            }
            outputMap[ procedure.output ] = procedure
        }

        this.allProcedures = glcProcedures
        this.emptyInputProcedures = glcProcedures.findAll { GlcProcedure procedure ->
            procedure.inputs.empty
        }.asImmutable()

        this.inputMap = inputMap.asImmutable()
        this.outputMap = outputMap.asImmutable()
    }

    Optional<GlcProcedure> getProcedureReading( GlcProcedureParameter parameter ) {
        Optional.ofNullable( inputMap[ parameter ] )
    }

    Optional<GlcProcedure> getProducerOf( GlcProcedureParameter parameter ) {
        Optional.ofNullable( outputMap[ parameter ] )
    }

}

@CompileStatic
@ToString( includePackage = false )
class CompiledGlcProcedures {
    private final List<CompiledGlcProcedure> compiledGlcProcedures = [ ]

    @PackageScope
    void add( List<CompiledGlcProcedure> procedures ) {
        compiledGlcProcedures.addAll( procedures )
    }

    List<CompiledGlcProcedure> getAllProcedures() {
        compiledGlcProcedures.asImmutable()
    }

}