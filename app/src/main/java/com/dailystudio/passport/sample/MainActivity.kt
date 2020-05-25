package com.dailystudio.passport.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dailystudio.passport.sdk.PassportSdk

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInBtn: View? = findViewById(R.id.sign_button)
        signInBtn?.setOnClickListener {
            PassportSdk("passport-sample", "cn").signIn(this);
        }
    }
}
