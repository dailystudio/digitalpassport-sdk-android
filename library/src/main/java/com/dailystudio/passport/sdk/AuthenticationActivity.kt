package com.dailystudio.passport.sdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dailystudio.devbricksx.app.activity.ActivityLauncher
import com.dailystudio.devbricksx.development.Logger

class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.debug("data: ${intent.data}")

        val intent = AuthManagementActivity.createBaseIntent(this).apply {
            data = intent.data
        }

        ActivityLauncher.launchActivity(this, intent)

        finish()
    }

}