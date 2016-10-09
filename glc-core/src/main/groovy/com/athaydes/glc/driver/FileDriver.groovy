package com.athaydes.glc.driver

import groovy.transform.CompileStatic

import java.util.function.Consumer

@CompileStatic
class FileDriver implements GlcDriver<File, FileName> {

    final Class<File> type = File
    final Class<FileName> requiredAnnotation = FileName

    @Override
    void watch( FileName fileName, Consumer<File> onChange ) {
        // TODO
    }

}

@interface FileName {
    String name();
}
