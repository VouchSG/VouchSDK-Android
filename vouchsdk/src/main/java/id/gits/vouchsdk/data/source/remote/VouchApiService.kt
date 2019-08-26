package id.gits.vouchsdk.data.source.remote

import com.google.gson.GsonBuilder
import id.gits.vouchsdk.BuildConfig
import id.gits.vouchsdk.data.model.BaseApiModel
import id.gits.vouchsdk.data.model.register.MessageBodyModel
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.data.model.register.RegisterResponseModel
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

internal interface VouchApiService {

    @POST("messages/referrence")
    @Headers("Content-Type: application/json")
     fun postMessage(
        @Header("token") token: String,
        @Body data: MessageBodyModel
    ): Observable<BaseApiModel<String?>>

    @POST("users/register")
    @Headers("Content-Type: application/json")
    fun registerUser(
        @Header("token") token: String,
        @Body data: RegisterBodyModel
    ): Observable<BaseApiModel<RegisterResponseModel?>>


    companion object Factory {

        fun getApiService(): VouchApiService {

            val mLoggingInterceptor = HttpLoggingInterceptor()
            mLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val mClient = if (BuildConfig.DEBUG) {
                OkHttpClient.Builder()
                    .addInterceptor(mLoggingInterceptor)
                    .addInterceptor { chain ->
                        val request = chain.request()
                        val newRequest = request.newBuilder().build()
                        chain.proceed(newRequest)
                    }
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build()
            } else {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request()
                        val newRequest = request.newBuilder().build()
                        chain.proceed(newRequest)
                    }
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build()
            }

            val mRetrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL_API)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(mClient)
                .build()

            return mRetrofit.create(VouchApiService::class.java)
        }
    }
}