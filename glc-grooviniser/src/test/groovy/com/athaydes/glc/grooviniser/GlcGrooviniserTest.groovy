package com.athaydes.glc.grooviniser

import com.athaydes.glc.api.Glc
import com.athaydes.glc.internal.GlcImpl
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 *
 */
class GlcGrooviniserTest {

	GlcGrooviniser grooviniser

	@Before
	void setup() {
		Glc glc = new GlcImpl()
		grooviniser = new GlcGrooviniser( glc )
	}

	@Test
	void createsValueIfAutoCreateIsTrue() {
		grooviniser.with {
			autoCreateValues = true
			when( 'input', Boolean )
		}
		assertTrue( grooviniser.getValue( 'input', Boolean ).isPresent() )
	}

	@Test
	void shouldBeEasyToUse() {
		grooviniser.with {
			autoCreateValues = true

			when( 'input', Boolean ).is( true ).then {

			}.otherwise {

			}
		}
	}

}
