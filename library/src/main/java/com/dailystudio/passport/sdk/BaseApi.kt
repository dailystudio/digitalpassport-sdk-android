package cn.orangelabschina.cuterobot.onrobotapp.api

import android.content.Context
import android.text.TextUtils
import com.dailystudio.devbricksx.development.Logger
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val DEBUG_API = true
private const val DEFAULT_TIMEOUT: Long = 10000

open class BaseApi<Interface> {

    internal class RawResponseDebugInterceptor: Interceptor {

            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val request = chain.request()
                val response = chain.proceed(request)

                val responseBody = response.body()
                val responseBodyString = response.body()!!.string()
                Logger.debug("response [raw]: %s", responseBodyString)

                return response.newBuilder().body(
                    ResponseBody.create(
                        responseBody!!.contentType(),
                        responseBodyString.toByteArray()
                    )
                ).build()
            }
    }

    internal abstract class HeaderInterceptor : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()

            val builder = original.newBuilder()
                .method(original.method(), original.body())

            val headers = getHeaders()

            headers?.entries?.forEach{
                builder.addHeader(it.key, it.value)
            }

            val request = builder.build()

            return chain.proceed(request)
        }

        abstract fun getHeaders(): Map<String, String>?

    }

    companion object {

        fun debugApi(format: String, vararg args: Any?) {
            if (DEBUG_API) {
                val builder = StringBuilder("[RESTFul API] ")

                builder.append(format)

                Logger.debug(builder.toString(), *args)
            }
        }

        fun warnApi(format: String, vararg args: Any?) {
            val builder = StringBuilder("[RESTFul API] ")

            builder.append(format)

            Logger.warn(builder.toString(), *args)
        }

        fun errorApi(format: String, vararg args: Any?) {
            val builder = StringBuilder("[RESTFul API] ")

            builder.append(format)

            Logger.error(builder.toString(), *args)
        }

    }

    private val mConnTimeout = DEFAULT_TIMEOUT

    fun <T> callSync(
        context: Context?,
        call: Call<T>): T? {

        return callApi(context, call, null)
    }

    fun <T> callAsync(
        context: Context?,
        call: Call<T>,
        callback: BaseApiCallback<T>) {

        callApi(context, call, callback)
    }

    private fun <T> callApi(
        context: Context?,
        call: Call<T>,
        callback: BaseApiCallback<T>?): T? {
        if (context == null
            || callback == null) {
            return null
        }

        if (callback == null) {
            var response = call.execute()

            return response.body()
        }


        call.enqueue(callback)

        return null
    }

    protected fun createInterface(
        baseUrl: String,
        classOfInterface: Class<Interface>?,
        interceptors: List<Interceptor>? = null
    ): Interface? {
        if (TextUtils.isEmpty(baseUrl) || classOfInterface == null) {
            return null
        }

        val clientBuilder = OkHttpClient.Builder()
            .hostnameVerifier { hostname, session -> true }
            .connectTimeout(mConnTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(mConnTimeout, TimeUnit.MILLISECONDS)
            .writeTimeout(mConnTimeout, TimeUnit.MILLISECONDS)
            .addNetworkInterceptor { chain ->
                val request = chain.request()
                debugApi("request [headers]: %s", request.headers())
                debugApi("request [url]: %s", request.url())
                var buffer = Buffer()
                request.body()?.writeTo(buffer)
                debugApi("request [body]: %s", buffer.readUtf8())

                chain.proceed(request)
            }
            .addInterceptor(RawResponseDebugInterceptor())

        if (interceptors != null && interceptors.isNotEmpty()) {
            interceptors.forEach {
                clientBuilder.addInterceptor(it)
            }
        }

        var client = clientBuilder.build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(baseUrl)
            .build()

        return retrofit.create(classOfInterface)
    }

}