package com.athaydes.glc.io.internal

import com.athaydes.glc.io.api.GlcDriver
import groovy.transform.CompileStatic

import java.util.function.Consumer

@CompileStatic
class FileDriver implements GlcDriver<File, FileName> {

    final Class<File> inputType = File
    final Class<FileName> config = FileName

    @Override
    void watch( FileName fileName, Consumer<File> onChange ) {
        // TODO
    }

}

@interface FileName {
    String name();
}
