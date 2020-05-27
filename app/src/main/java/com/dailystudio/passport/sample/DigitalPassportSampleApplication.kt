package com.dailystudio.passport.sample

import com.dailystudio.devbricksx.app.DevBricksApplication
import com.dailystudio.devbricksx.development.Logger
import com.facebook.stetho.Stetho
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration

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
