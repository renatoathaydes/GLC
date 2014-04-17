package com.athaydes.glc.internal;

import com.athaydes.glc.api.MutableValue;
import com.athaydes.glc.api.Value;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class IOTest {

    IO io = new IO();

    @Test
    public void shouldAddValue() {
        boolean addedOk = io.addValue( "i0.0", Integer.class, 10 );

        assertThat( addedOk, is( true ) );

        Optional<? extends Value<Integer>> i0_0 = io.getValue( "i0.0", Integer.class );

        assertThat( i0_0.isPresent(), is( true ) );
        assertThat( i0_0.get().getValue(), is( 10 ) );
    }

    @Test
    public void canSetValuesValue() {
        io.addValue( "o0.0", Integer.class, 0 );
        Optional<? extends MutableValue<Integer>> i0_0 = io.getValue( "o0.0", Integer.class );
        MutableValue<Integer> output = i0_0.get();
        output.setValue( 50 );

        MutableValueImpl<Integer> asMutable = ( MutableValueImpl<Integer> ) output;

        assertThat( asMutable.getValue(), is( 50 ) );
    }

    @Test
    public void canRemoveValue() {
        io.addValue( "i0.0", Integer.class, 0 );

        assertThat( io.removeValue( "i0.0", Integer.class ), is( true ) );
        assertThat( io.getValue( "i0.0", Integer.class ).isPresent(), is( false ) );
    }

    @Test
    public void willNotRemoveValueOfSameNameButWrongClass() {
        io.addValue( "o0.0", String.class, "" );

        assertThat( io.removeValue( "o0.0", Integer.class ), is( false ) );
        assertThat( io.getValue( "o0.0", Integer.class ).isPresent(), is( false ) );
        assertThat( io.getValue( "o0.0", String.class ).isPresent(), is( true ) );
    }

}
