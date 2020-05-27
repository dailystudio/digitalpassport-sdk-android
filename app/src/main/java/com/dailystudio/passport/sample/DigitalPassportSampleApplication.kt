package com.dailystudio.passport.sample

import com.dailystudio.devbricksx.app.DevBricksApplication
import com.facebook.stetho.Stetho

class DigitalPassportSampleApplication : DevBricksApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.USE_STETHO) {
            Stetho.initializeWithDefaults(this)
        }
    }

    override fun isDebugBuild(): Boolean {
        return BuildConfig.DEBUG
    }

}
