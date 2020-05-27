package com.dailystudio.passport.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.passport.sdk.AuthCallback
import com.dailystudio.passport.sdk.PassportSdk
import com.dailystudio.passport.sdk.UserProfile

class MainActivity : AppCompatActivity() {

    private val sdk = PassportSdk("passport-sample", "cn")
    private var authBtn: TextView? = null
    private var userView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authBtn = findViewById(R.id.auth_button)
        userView = findViewById(R.id.user)

        applyFeatures()
    }

    private fun applyFeatures() {
        val profile = sdk.currentUser(this)
        Logger.debug("profile: $profile")

        if (profile == null) {
            userView?.text = null

            authBtn?.setText(R.string.label_sign_in)
            authBtn?.setOnClickListener {
                sdk.signIn(this, authCallback)
            }
        } else {
            userView?.text = profile.uid

            authBtn?.setText(R.string.label_sign_out)
            authBtn?.setOnClickListener {
                sdk.signOut(this)

                applyFeatures()
            }
        }
    }

    private val authCallback = object: AuthCallback {

        override fun onAuthSucceed() {
            val user = sdk.currentUser(this@MainActivity)
            Logger.debug("current user: $user")

            applyFeatures()
        }

        override fun onAuthFailed() {
            applyFeatures()
        }

    }
}
