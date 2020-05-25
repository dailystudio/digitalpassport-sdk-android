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
import retrofit2.http.POST
import java.io.IOException

data class TokenRet(val code: Int,
                    val message: String)

interface PassportAuthInterface {

    companion object {
        const val BASE_URL = SdkConstants.PASSPORT_URL

        const val BASE_PATH = "v1/passport"
        const val API_PATH_TOKEN = "token"
        const val API_PATH_AUTHORIZE = "authorize"

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
                append("$BASE_URL/$BASE_PATH/$API_PATH_AUTHORIZE")
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
    @POST("/$BASE_PATH/$API_PATH_TOKEN")
    fun accessToken(@Field(PARAM_CODE) code: String,
                    @Field(PARAM_GRANT_TYPE) grantType: String,
                    @Field(PARAM_REDIRECT_URI) redirectUri: String
    ): Call<TokenRet>
}


class PassportAuthApi: BaseApi<PassportAuthInterface>() {

    companion object {
        private var sInterface: PassportAuthInterface? = null

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
        val cuteRobotInterface = getInterface()

        val call = cuteRobotInterface?.accessToken(code, type,
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

                ret = TokenRet(400, e.toString())
            }

            return ret
        }

        call?.enqueue(callback)

        return null
    }

    @Synchronized
    private fun getInterface(): PassportAuthInterface? {
        if (sInterface == null) {
            sInterface = createInterface(
                PassportAuthInterface.BASE_URL,
                PassportAuthInterface::class.java,
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
