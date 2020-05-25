package com.dailystudio.passport.sdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class AuthState {

    None,
    Authentication,
    Token,
    Done,

}

class AuthManagementActivity : AppCompatActivity() {

    companion object {

        fun createAuthIntent(context: Context,
                             clientId: String,
                             redirectUri: String,
                             region: String) : Intent {
            val intent = createBaseIntent(context)

            intent.putExtra(SdkConstants.EXTRA_CLIENT_ID, clientId)
            intent.putExtra(SdkConstants.EXTRA_REDIRECT_URI, redirectUri)
            intent.putExtra(SdkConstants.EXTRA_REGION, region)

            return intent
        }

        fun createBaseIntent(context: Context): Intent {
            return Intent(context, AuthManagementActivity::class.java)
        }

    }

    private var authState: AuthState = AuthState.None

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        setIntent(intent)
    }


    override fun onResume() {
        super.onResume()
        Logger.debug("auth state: $authState")

        when (authState) {
            AuthState.None -> {
                startAuth(intent)
                authState = AuthState.Authentication
            }

            AuthState.Authentication -> {
                val code = intent?.data?.getQueryParameter("code")
                Logger.debug("code: $code")

                code?.let {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val accessToken = PassportAuthApi().accessToken(this@AuthManagementActivity,
                            it,
                            PassportAuthInterface.GRANT_TYPE_CODE,
                            null
                        )
                        Logger.debug("accessToken: $accessToken")
                    }
                }
            }
        }
    }

    private fun startAuth(intent: Intent) {
        val clientId = intent.getStringExtra(SdkConstants.EXTRA_CLIENT_ID)
        val redirectUri = intent.getStringExtra(SdkConstants.EXTRA_REDIRECT_URI)
        val region = intent.getStringExtra(SdkConstants.EXTRA_REGION) ?: ""

        Logger.debug("clientId = $clientId, redirect = $redirectUri, region = $region")

        val builder = CustomTabsIntent.Builder()

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this,
            PassportAuthInterface.buildAuthUri(clientId, redirectUri, region))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(SdkConstants.EXTRA_AUTH_STATE, authState.toString())
    }

    private fun restoreState(savedInstanceState: Bundle) {
        val authStateStr =
            savedInstanceState.getString(SdkConstants.EXTRA_AUTH_STATE)

        authStateStr?.let {
            authState = try {
                AuthState.valueOf(it)
            } catch (e: Exception) {
                Logger.warn("cannot restore auth state from string [$authStateStr]: $e")

                AuthState.None
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        restoreState(savedInstanceState)
    }

}