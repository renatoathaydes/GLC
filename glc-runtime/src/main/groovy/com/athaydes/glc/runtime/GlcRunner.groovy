package com.athaydes.glc.runtime

import com.athaydes.glc.io.api.GlcIn
import com.athaydes.glc.procedure.AnnotationInfo
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
        List<GlcProcedure> emptyInputProcedures = [ ]
        List<GlcProcedure> driverInputProcedures = [ ]

        glcProcedures.allProcedures.each { GlcProcedure procedure ->
            if ( procedure.inputs.empty )
                emptyInputProcedures << procedure
            else if ( hasOnlyDriverInputParameters( procedure ) )
                driverInputProcedures << procedure
        }

        emptyInputProcedures.parallelStream().forEach { GlcProcedure procedure ->
            def value = procedure.runnable.call()
            valueByParameter[ procedure.output ] = value
        }

        driverInputProcedures.parallelStream().forEach { GlcProcedure procedure ->
            runDriverProcedure( procedure )
        }

        log.debug( "Values initialized in the initial run: {}", valueByParameter )
    }

    boolean hasOnlyDriverInputParameters( GlcProcedure glcProcedure ) {
        for ( input in glcProcedure.inputs ) {
            if ( !( ( GlcProcedureParameter ) input ).type.annotations ) {
                return false
            }
        }
        return true
    }

    private void runDriverProcedure( GlcProcedure procedure ) {
        for ( inputParameter in procedure.inputs ) {
            GlcProcedureParameter inputParam = (GlcProcedureParameter) inputParameter
            for ( AnnotationInfo annotation in inputParam.type.annotations ) {
                def driver = annotation.driverType.newInstance() as GlcIn
                driver.provide { input ->
                    // FIXME the procedure may actually have more than one parameter!
                    def newValue = apply( procedure, [ input ] )
                    // FIXME naive implementation, just writing to the Map directly causes Thread issues
                    valueByParameter[ inputParam ] = newValue
                }
            }
        }
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