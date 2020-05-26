package com.dailystudio.passport.sdk

import android.content.Context
import android.net.Uri
import cn.orangelabschina.cuterobot.onrobotapp.api.BaseApi
import com.dailystudio.devbricksx.development.Logger
import okhttp3.Credentials
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import java.io.IOException

class Profile (val name: String?,
               val phone: String?,
               val email: String?) {

    override fun toString(): String {
        return buildString {
            append("name: $name, ")
            append("phone: $phone, ")
            append("email: $email")
        }
    }
}

class TokenRet(val access_token: String? = null,
               val token_type: String? = null,
               val expires_in: Long = 0,
               val refresh_token: String? = null) {

    fun toAuthInfo(): AuthInfo? {
        if (access_token == null) {
            return null
        }

        return AuthInfo(access_token, refresh_token, expires_in)
    }

    override fun toString(): String {
        return buildString {
            append("access_token: $access_token, ")
            append("token_type: $token_type, ")
            append("expires_in: $expires_in, ")
            append("refresh_token: $refresh_token")
        }
    }

}

class UserProfileRet(val code: Int,
                     val message: String?,
                     val uid: String? = null,
                     val profile: Profile? = null) {

    fun toUser(): UserProfile? {
        if (code != 200 || uid == null) {
            return null
        }

        return UserProfile(uid, profile?.name, profile?.phone, profile?.email)
    }

    override fun toString(): String {
        return buildString {
            append("code: $code, ")
            append("message: $message, ")
            append("uid: $uid, ")
            append("profile: $profile")
        }
    }

}

interface PassportInterface {

    companion object {
        const val BASE_URL = SdkConstants.PASSPORT_URL

        const val BASE_PATH = "v1"
        const val PASSPORT_PATH = "passport"
        const val USER_PATH = "userinfo"

        const val API_PATH_TOKEN = "token"
        const val API_PATH_AUTHORIZE = "authorize"
        const val API_PATH_PROFILE = "profile"

        const val PARAM_GRANT_TYPE = "grant_type"
        const val PARAM_CODE = "code"
        const val PARAM_REDIRECT_URI = "redirect_uri"
        const val PARAM_CLIENT_ID = "client_id"
        const val PARAM_RESPONSE_TYPE = "response_type"
        const val PARAM_REGION = "region"

        const val GRANT_TYPE_CODE = "authorization_code"
        const val GRANT_TYPE_REFRESH = "refresh_token"

        const val RESPONSE_TYPE_CODE = "code"

        fun buildAuthUri(clientId: String,
                         redirectUri: String,
                         region: String) : Uri {
            return Uri.parse(buildString {
                append("$BASE_URL/$BASE_PATH/$PASSPORT_PATH/$API_PATH_AUTHORIZE")
                append("?")
                append("$PARAM_RESPONSE_TYPE=$RESPONSE_TYPE_CODE")
                append("&")
                append("$PARAM_REDIRECT_URI=$redirectUri")
                append("&")
                append("$PARAM_CLIENT_ID=$clientId")
                append("&")
                append("$PARAM_REGION=$region")
            })
        }

    }

    @FormUrlEncoded
    @POST("/$BASE_PATH/$PASSPORT_PATH/$API_PATH_TOKEN")
    fun accessToken(@Field(PARAM_CODE) code: String,
                    @Field(PARAM_GRANT_TYPE) grantType: String,
                    @Field(PARAM_REDIRECT_URI) redirectUri: String
    ): Call<TokenRet>

    @GET("/$BASE_PATH/$USER_PATH/$API_PATH_PROFILE")
    fun userProfile(): Call<UserProfileRet>
}


class PassportAuthApi: BaseApi<PassportInterface>() {

    companion object {
        private var sInterface: PassportInterface? = null

        @Synchronized
        fun resetApiInterface() {
            sInterface = null
        }

    }

    fun accessToken(
        context: Context,
        code: String,
        type: String,
        callback: Callback<TokenRet>?
    ): TokenRet? {
        val authInterface = getInterface()

        val call = authInterface?.accessToken(code, type,
        "auth://${context.packageName}")

        if (callback == null) {
            var ret: TokenRet? = null
            try {
                val response = call?.execute()
                if (response != null) {
                    ret = response.body()
                }
            } catch (e: IOException) {
                Logger.error(
                    "get accessToken failed: %s",
                    e.toString()
                )

                ret = TokenRet(null)
            }

            return ret
        }

        call?.enqueue(callback)

        return null
    }

    @Synchronized
    private fun getInterface(): PassportInterface? {
        if (sInterface == null) {
            sInterface = createInterface(
                PassportInterface.BASE_URL,
                PassportInterface::class.java,
                listOf(mHeaderInterceptor)
            )
        }

        return sInterface
    }

    private var mHeaderInterceptor = object : HeaderInterceptor() {

        override fun getHeaders(): Map<String, String>? {
            val basicAuthToken =
                Credentials.basic("passport-sample", "6cNBvn+dxIxNGUcW2Unm/2APm0Qm3Ylk");

            return mutableMapOf(
                "Authorization" to basicAuthToken
            )
        }

    }

}

class PassportUserApi(private val accessToken: String): BaseApi<PassportInterface>() {

    companion object {
        private var sInterface: PassportInterface? = null

        @Synchronized
        fun resetApiInterface() {
            sInterface = null
        }

    }

    fun getUserProfile(
        callback: Callback<UserProfileRet>?
    ): UserProfileRet? {
        val userInterface = getInterface()

        val call = userInterface?.userProfile()

        if (callback == null) {
            var profileRet: UserProfileRet? = null
            try {
                val response = call?.execute()
                if (response != null) {
                    profileRet = response.body()
                }
            } catch (e: IOException) {
                Logger.error(
                    "get accessToken failed: %s",
                    e.toString()
                )

                profileRet = UserProfileRet(400, null)
            }

            return profileRet
        }

        call?.enqueue(callback)

        return null
    }

    @Synchronized
    private fun getInterface(): PassportInterface? {
        if (sInterface == null) {
            sInterface = createInterface(
                PassportInterface.BASE_URL,
                PassportInterface::class.java,
                listOf(mHeaderInterceptor)
            )
        }

        return sInterface
    }

    private var mHeaderInterceptor = object : HeaderInterceptor() {

        override fun getHeaders(): Map<String, String>? {
            val bearerToken = "Bearer $accessToken"

            return mutableMapOf(
                "Authorization" to bearerToken
            )
        }

    }

}
