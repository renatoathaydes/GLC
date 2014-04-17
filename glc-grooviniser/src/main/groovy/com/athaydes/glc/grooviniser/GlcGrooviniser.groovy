package com.athaydes.glc.grooviniser

import com.athaydes.glc.api.Glc
import com.athaydes.glc.api.Question

/**
 *
 */
class GlcGrooviniser {

	@Delegate
	private final Glc glc;

	boolean autoCreateValues = false

	GlcGrooviniser( Glc glc ) {
		this.glc = glc;
	}

	@Override
	def <T> Question<T> when( String value, Class<T> type ) {
		if ( autoCreateValues ) {
			glc.addValue( value, type, defaultValueFor( type ) )
		}
		glc.when( value, type )
	}

	private static defaultValueFor( Class type ) {
		switch ( type ) {
			case Number: return 0
			case Boolean: return false
			case String: return ""
		}
		return null
	}

}
