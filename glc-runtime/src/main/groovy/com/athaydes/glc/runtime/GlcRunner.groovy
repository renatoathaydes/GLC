package com.athaydes.glc.runtime

import com.athaydes.glc.GlcProcedure
import groovy.transform.CompileStatic

@CompileStatic
class GlcRunner {

    void run( List<GlcProcedure> glcProcedures ) {
        final immediateRun = glcProcedures.findAll { GlcProcedure procedure -> procedure.inputs.empty }
        immediateRun*.runnable*.call()
    }

}