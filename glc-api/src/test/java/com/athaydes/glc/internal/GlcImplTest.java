package com.athaydes.glc.internal;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class GlcImplTest {

    GlcImpl glc = new GlcImpl();

    @Test
    public void whenThen() {
        glc.addValue( "i0", Boolean.class, false );
        glc.addValue( "o0", Boolean.class, false );

        MutableValueImpl<Boolean> input = ( MutableValueImpl<Boolean> )
                glc.getValue( "i0", Boolean.class ).get();
        MutableValueImpl<Boolean> output = ( MutableValueImpl<Boolean> )
                glc.getValue( "o0", Boolean.class ).get();

        // create 'partial' connection i0 -> o0
        glc.when( "i0", Boolean.class ).is( true )
                .then( () -> output.setValue( true ) );

        glc.cycle();

        assertThat( output.getValue(), is( false ) );

        input.setValue( true );
        glc.cycle();

        assertThat( output.getValue(), is( true ) );

        input.setValue( false );
        glc.cycle();

        // value will not change back because there's no 'else' clause
        assertThat( output.getValue(), is( true ) );
    }

    @Test
    public void whenThenOtherwise() {
        glc.addValue( "i0", Boolean.class, false );
        glc.addValue( "o0", Boolean.class, false );

        MutableValueImpl<Boolean> input = ( MutableValueImpl<Boolean> )
                glc.getValue( "i0", Boolean.class ).get();
        MutableValueImpl<Boolean> output = ( MutableValueImpl<Boolean> )
                glc.getValue( "o0", Boolean.class ).get();

        // create 'full' connection i0 -> o0
        glc.when( "i0", Boolean.class ).is( true )
                .then( () -> output.setValue( true ) )
                .otherwise( () -> output.setValue( false ) );

        glc.cycle();

        assertThat( output.getValue(), is( false ) );

        input.setValue( true );
        glc.cycle();

        assertThat( output.getValue(), is( true ) );

        input.setValue( false );
        glc.cycle();

        assertThat( output.getValue(), is( false ) );
    }

    @Test
    public void whenTurns() {
        glc.addValue( "i0", Boolean.class, false );
        glc.addValue( "o0", Boolean.class, false );

        MutableValueImpl<Boolean> input = ( MutableValueImpl<Boolean> )
                glc.getValue( "i0", Boolean.class ).get();
        MutableValueImpl<Boolean> output = ( MutableValueImpl<Boolean> )
                glc.getValue( "o0", Boolean.class ).get();

        // create 'edge' connection i0 |-> o0
        glc.when( "i0", Boolean.class ).turns( true )
                .then( () -> output.setValue( true ) );

        glc.cycle();
        assertThat( output.getValue(), is( false ) );

        input.setValue( true );
        glc.cycle();
        assertThat( output.getValue(), is( true ) );
        glc.cycle();
        assertThat( output.getValue(), is( true ) );
        input.setValue( false );
        glc.cycle();
        assertThat( output.getValue(), is( true ) );
    }

    @Test
    public void realExample() {
        glc.addValue( "a", String.class, "" );
        List<String> captor = new ArrayList<>( 10 );

        glc.when( "a", String.class )
                .is( "START" )
                .then( () -> captor.add( "THEN" ) )
                .otherwise( () -> captor.add( "OTHER" ) );

        for ( int i = 0; i < 4; i++ ) {
            if ( i == 2 ) {
                glc.getValue( "a", String.class )
                        .ifPresent( ( out ) -> out.setValue( "START" ) );
            }
            glc.cycle();
        }

        assertThat( captor, is( equalTo( asList( "OTHER", "OTHER", "THEN", "THEN" ) ) ) );
    }


}
