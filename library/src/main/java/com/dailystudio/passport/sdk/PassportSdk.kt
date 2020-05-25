package com.dailystudio.passport.sdk

import android.content.Context
import com.dailystudio.devbricksx.app.activity.ActivityLauncher


class PassportSdk (val clientId: String,
                   val region: String = "") {

    fun signIn(context: Context) {
        val intent = AuthManagementActivity.createAuthIntent(
            context,
            clientId,
            "auth://${context.packageName}",
            region
        )

        ActivityLauncher.launchActivity(context, intent)
    }

}