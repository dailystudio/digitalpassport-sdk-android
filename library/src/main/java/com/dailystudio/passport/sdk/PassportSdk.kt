package com.dailystudio.passport.sdk

import android.content.Context
import com.dailystudio.devbricksx.app.activity.ActivityLauncher
import com.dailystudio.devbricksx.development.Logger
import com.rasalexman.kdispatcher.KDispatcher
import com.rasalexman.kdispatcher.Notification
import com.rasalexman.kdispatcher.subscribe
import com.rasalexman.kdispatcher.unsubscribe
import java.lang.ref.WeakReference

data class AuthInfo(val accessToken: String?,
                    val refreshToken: String?,
                    val expiresIn: Long) {

    override fun toString(): String {
        return buildString {
            append("accessToken: $accessToken, ")
            append("refreshToken: $refreshToken, ")
            append("expiresIn: $expiresIn")
        }
    }

}

data class UserProfile(val uid: String,
                       val name: String?,
                       val mobile: String?,
                       val email: String?) {

    override fun toString(): String {
        return buildString {
            append("uid: $uid, ")
            append("name: $name, ")
            append("mobile: $mobile, ")
            append("email: $email")
        }
    }

}

interface AuthCallback {

    fun onAuthSucceed()
    fun onAuthFailed()

}

class PassportSdk(val clientId: String,
                  val region: String = "") {

    companion object {

        private fun handleAuthEvent(notification: Notification<Boolean>) {
            Logger.debug("notification: $notification")
            Logger.debug("weakAuthCallback: $weakAuthCallback")
            weakAuthCallback?.let {
                val callback = it.get()
                Logger.debug("callback: $callback")

                notification.data?.let { succeed ->
                    if (succeed) {
                        callback?.onAuthSucceed()
                    } else {
                        callback?.onAuthFailed()
                    }
                }
            }

            weakAuthCallback = null

            KDispatcher.unsubscribe(SdkConstants.SUB_AUTH, ::handleAuthEvent)
        }

        private var weakAuthCallback: WeakReference<AuthCallback>? = null

        private fun registerCallback(callback: AuthCallback) {
            weakAuthCallback = WeakReference(callback)
            Logger.debug("weakAuthCallback: $weakAuthCallback")

            KDispatcher.subscribe(SdkConstants.SUB_AUTH, 1, ::handleAuthEvent)
        }
    }

    fun signIn(context: Context, callback: AuthCallback? = null) {
        if (callback != null) {
            Logger.debug("class: ${callback::class}");
            registerCallback(callback)
        }

        val intent = AuthManagementActivity.createAuthIntent(
            context,
            clientId,
            "auth://${context.packageName}",
            region
        )

        ActivityLauncher.launchActivity(context, intent)
    }

    fun signOut(context: Context) {
        AuthInfoHelper.clearUserProfile(context)
        AuthInfoHelper.clearAuthInfo(context)
    }

    fun currentUser(context: Context): UserProfile? {
        return AuthInfoHelper.loadUserProfile(context)
    }

}