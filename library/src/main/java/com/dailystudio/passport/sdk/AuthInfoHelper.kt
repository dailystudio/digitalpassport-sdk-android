package com.dailystudio.passport.sdk

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.dailystudio.devbricksx.development.Logger
import com.google.gson.Gson
import java.lang.Exception

object AuthInfoHelper {

    private const val AUTH_PREF = "passport-auth"
    private val GSON: Gson = Gson();

    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            AUTH_PREF,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAuthInfo(context: Context,
                     authInfo: AuthInfo?) {
        Logger.debug("save auth info: $authInfo")
        if (authInfo == null) {
            return
        }

        val preferences = getEncryptedSharedPreferences(context)

        preferences.edit().apply {
            putString(SdkConstants.PREF_KEY_ACCESS_TOKEN, authInfo.accessToken)
            putString(SdkConstants.PREF_KEY_REFRESH_TOKEN, authInfo.refreshToken)
            putLong(SdkConstants.PREF_KEY_EXPIRES_IN, authInfo.expiresIn)
        }.apply()
    }

    fun loadAuthInfo(context: Context): AuthInfo? {
        val preferences = getEncryptedSharedPreferences(context)

        val accessToken = preferences.getString(SdkConstants.PREF_KEY_ACCESS_TOKEN, null) ?: return null
        val refreshToken = preferences.getString(SdkConstants.PREF_KEY_REFRESH_TOKEN, null)
        val expiresIn = preferences.getLong(SdkConstants.PREF_KEY_EXPIRES_IN, 0L)

        return AuthInfo(accessToken, refreshToken, expiresIn)
    }

    fun clearAuthInfo(context: Context) {
        val preferences = getEncryptedSharedPreferences(context)

        preferences.edit().apply {
            putString(SdkConstants.PREF_KEY_ACCESS_TOKEN, null)
            putString(SdkConstants.PREF_KEY_REFRESH_TOKEN, null)
            putLong(SdkConstants.PREF_KEY_EXPIRES_IN, 0)
        }.apply()
    }

    fun saveUserProfile(context: Context,
                        profile: UserProfile?) {
        if (profile == null) {
            return
        }

        val preferences = getEncryptedSharedPreferences(context)

        preferences.edit().apply {
            putString(SdkConstants.PREF_USER, GSON.toJson(profile))
        }.apply()
    }

    fun loadUserProfile(context: Context): UserProfile? {
        val preferences = getEncryptedSharedPreferences(context)

        val userString = preferences.getString(SdkConstants.PREF_USER, null) ?: return null

        return try {
            GSON.fromJson(userString, UserProfile::class.java)
        } catch (e: Exception) {
            Logger.warn("failed to parse user profile from [$userString]: $e")
            null
        }
    }

    fun clearUserProfile(context: Context) {
        val preferences = getEncryptedSharedPreferences(context)

        preferences.edit().apply {
            putString(SdkConstants.PREF_USER, null)
        }.apply()
    }

}