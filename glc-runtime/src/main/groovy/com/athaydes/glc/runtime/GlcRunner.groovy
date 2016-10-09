package com.athaydes.glc.runtime

import com.athaydes.glc.procedure.GlcProcedure
import com.athaydes.glc.procedure.GlcProcedureParameter
import com.athaydes.glc.procedure.GlcProcedures
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j

import java.util.concurrent.ConcurrentHashMap

@Slf4j
@CompileStatic
class GlcRunner {

    private final Map<GlcProcedureParameter, Object> valueByParameter = new ConcurrentHashMap<>()

    void run( GlcProcedures glcProcedures ) {
        initialRunOf glcProcedures
        cycle glcProcedures, valueByParameter
    }

    private void initialRunOf( GlcProcedures glcProcedures ) {
        final immediateRun = glcProcedures.emptyInputProcedures

        immediateRun.parallelStream().forEach { GlcProcedure procedure ->
            def value = procedure.runnable.call()
            valueByParameter[ procedure.output ] = value
        }

        log.debug( "Values initialized in the initial run: {}", valueByParameter )
    }

    private void cycle( GlcProcedures glcProcedures,
                        Map<GlcProcedureParameter, Object> changedValues ) {
        if ( changedValues.isEmpty() ) {
            log.info( "No more input changes possible, terminating GLC Program" )
            return
        }

        log.debug( "Changed values: {}", changedValues )

        Map<String, GlcProcedure> procedureByName = [ : ]
        Map<String, List<Object>> argumentsByProcName = [ : ]

        changedValues.each { GlcProcedureParameter parameter, Object currentValue ->
            final procedureToRun = glcProcedures.getProcedureReading( parameter )
            if ( procedureToRun.isPresent() ) {
                final proc = procedureToRun.get()
                def alreadyVisited = procedureByName.put( proc.closureName, proc )
                if ( !alreadyVisited ) {
                    def args = proc.inputs.collect { input ->
                        ( input == parameter ) ?
                                currentValue :
                                changedValues.computeIfAbsent( ( GlcProcedureParameter ) input, { p -> valueByParameter[ p ] } )
                    }
                    argumentsByProcName.put( proc.closureName, args )
                }
            }
        }

        Map<GlcProcedureParameter, Object> newChangedValues = new ConcurrentHashMap<>()

        argumentsByProcName.entrySet().parallelStream().forEach { Map.Entry<String, List<Object>> entry ->
            def procedure = procedureByName[ entry.key ]
            def args = entry.value
            def newValue = apply( procedure, args )
            newChangedValues[ procedure.output ] = newValue
            valueByParameter[ procedure.output ] = newValue
        }

        cycle( glcProcedures, newChangedValues )
    }

    @CompileDynamic
    // allows calling the closure without type-checking
    private static apply( GlcProcedure procedure, List<Object> args ) {
        procedure.runnable.call( *args )
    }

    @PackageScope
    def valueOf( GlcProcedureParameter parameter ) {
        valueByParameter[ parameter ]
    }

}