package com.athaydes.glc.internal;

import com.athaydes.glc.api.Otherwise;

/**
 *
 */
class OtherWiseImpl implements Otherwise {

    Runnable run;

    @Override
    public void otherwise( Runnable run ) {
        this.run = run;
    }

}
