package com.dailystudio.passport.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.passport.sdk.AuthCallback
import com.dailystudio.passport.sdk.PassportSdk

class MainActivity : AppCompatActivity() {

    private val sdk = PassportSdk("passport-sample", "cn")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInBtn: View? = findViewById(R.id.sign_button)
        signInBtn?.setOnClickListener {
            sdk.signIn(this, authCallback)
        }
    }

    private val authCallback = object: AuthCallback {

        override fun onAuthSucceed() {
            Logger.debug("current user: ${sdk.currentUser(this@MainActivity)}")
        }

        override fun onAuthFailed() {
            TODO("Not yet implemented")
        }

    }
}
