package com.athaydes.glc.runtime

import com.athaydes.glc.procedure.AnnotationInfo
import com.athaydes.glc.procedure.CompiledGlcProcedure
import com.athaydes.glc.procedure.GenericType
import com.athaydes.glc.procedure.GlcProcedure
import com.athaydes.glc.procedure.GlcProcedureParameter

/**
 * Test trait with helpful methods for testing GLC.
 */
trait GlcTest {

    GlcProcedure GlcProcedure( String name,
                               List<GlcProcedureParameter> inputs,
                               GlcProcedureParameter output,
                               Closure runnable ) {
        new GlcProcedure( new CompiledGlcProcedure( name, inputs, output ), runnable )
    }

    GlcProcedureParameter GlcParameter( Class type, String name ) {
        GlcParameter( type, name, [ ] )
    }

    GlcProcedureParameter GlcParameter( Class type, String name, List<AnnotationInfo> annotations ) {
        new GlcProcedureParameter( new GenericType( type.name, GenericType.EMPTY, annotations ), name )
    }

    GlcProcedureParameter GlcParameter( String type, String name ) {
        GlcParameter( type, name, [ ] )
    }

    GlcProcedureParameter GlcParameter( String type, String name, List<AnnotationInfo> annotations ) {
        new GlcProcedureParameter( new GenericType( type, GenericType.EMPTY, annotations ), name )
    }

}