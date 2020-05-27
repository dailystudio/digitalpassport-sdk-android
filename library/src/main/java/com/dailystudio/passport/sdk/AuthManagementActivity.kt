package com.dailystudio.passport.sdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.rasalexman.kdispatcher.KDispatcher
import com.rasalexman.kdispatcher.call
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class AuthState {

    None,
    Authentication,
    Token,
    Profile,
    Done,
    Error,

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
            return Intent(context, AuthManagementActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        }

    }

    private var authState: AuthState = AuthState.None
    private var authStateView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.auth_management)

        setupViews()
    }

    private fun setupViews() {
        authStateView = findViewById(R.id.auth_state_prompt)
        setAuthState(AuthState.None)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        Logger.debug("auth state: $authState")

        when (authState) {
            AuthState.None -> {
                setAuthState(AuthState.Authentication)
                startAuth(intent)
            }

            AuthState.Authentication -> {
                val code = intent?.data?.getQueryParameter("code")
                Logger.debug("code: $code")

                if (code != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        setAuthState(AuthState.Token)

                        val authInfo = getAccessToken(code)
                        authInfo?.accessToken?.let {
                            setAuthState(AuthState.Profile)
                            val user = getUser(it)

                            if (user != null) {
                                setAuthState(AuthState.Done)
                                authSuccess()
                            } else {
                                setAuthState(AuthState.Error)
                                authFailed()
                            }
                        }
                    }
                } else {
                    authFailed()
                }
            }
        }
    }

    private fun setAuthState(newState: AuthState) {
        this.authState = newState

        authStateView?.setText(when(authState) {
            AuthState.None, AuthState.Authentication -> R.string.auth_state_authentication
            AuthState.Token -> R.string.auth_state_access_token
            AuthState.Profile -> R.string.auth_state_profile
            AuthState.Done -> R.string.auth_state_done
            AuthState.Error -> R.string.auth_state_error
        })
    }

    private fun authSuccess() {
        KDispatcher.call(SdkConstants.SUB_AUTH, true)
        finish()
    }

    private fun authFailed() {
        KDispatcher.call(SdkConstants.SUB_AUTH, false)
        finish()
    }

    private fun startAuth(intent: Intent) {
        val clientId = intent.getStringExtra(SdkConstants.EXTRA_CLIENT_ID)
        val redirectUri = intent.getStringExtra(SdkConstants.EXTRA_REDIRECT_URI)
        val region = intent.getStringExtra(SdkConstants.EXTRA_REGION) ?: ""

        val uri = PassportInterface.buildAuthUri(clientId, redirectUri, region)

        Logger.debug("clientId = $clientId, redirect = $redirectUri, region = $region -> uri = $uri")

        val builder = CustomTabsIntent.Builder()

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, uri)
    }

    private suspend fun getAccessToken(code: String): AuthInfo? {
        val tokenRet = PassportAuthApi().accessToken(this@AuthManagementActivity,
            code,
            PassportInterface.GRANT_TYPE_CODE,
            null
        )

        return if (tokenRet != null) {
            tokenRet.toAuthInfo()?.also {
                AuthInfoHelper.saveAuthInfo(
                    this@AuthManagementActivity, it)
            }
        } else {
            null
        }
    }

    private suspend fun getUser(accessToken: String): UserProfile? {
        val userRet = PassportUserApi(accessToken).getUserProfile(
            null
        )

        return if (userRet != null) {
            userRet.toUser()?.also {
                AuthInfoHelper.saveUserProfile(
                    this@AuthManagementActivity, it)
            }
        } else {
            null
        }
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