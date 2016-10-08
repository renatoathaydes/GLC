package com.athaydes.glc.runtime

import com.athaydes.glc.GlcProcedures
import groovy.transform.CompileStatic

@CompileStatic
class GlcRunner {

    void run( GlcProcedures glcProcedures ) {
        final immediateRun = glcProcedures.emptyInputProcedures
        immediateRun*.runnable*.call()
    }

}