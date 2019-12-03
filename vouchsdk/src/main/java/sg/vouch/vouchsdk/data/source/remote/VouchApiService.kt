package sg.vouch.vouchsdk.data.source.remote

import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import sg.vouch.vouchsdk.BuildConfig
import sg.vouch.vouchsdk.data.model.BaseApiModel
import sg.vouch.vouchsdk.data.model.config.response.ConfigResponseModel
import sg.vouch.vouchsdk.data.model.message.body.LocationBodyModel
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.data.model.message.body.ReferenceSendBodyModel
import sg.vouch.vouchsdk.data.model.message.body.SendAudioBodyModel
import sg.vouch.vouchsdk.data.model.message.response.MessageResponseModel
import sg.vouch.vouchsdk.data.model.message.response.SendAudioResponseModel
import sg.vouch.vouchsdk.data.model.message.response.UploadImageResponseModel
import sg.vouch.vouchsdk.data.model.register.RegisterBodyModel
import sg.vouch.vouchsdk.data.model.register.RegisterResponseModel
import java.util.concurrent.TimeUnit


/**
 * This is a interface created for ApiService VouchSDK
 */

internal interface VouchApiService {

    @GET("config")
    @Headers("Content-Type: application/json")
    fun getConfig(
        @Header("api-key") apiKey: String
    ): Observable<BaseApiModel<ConfigResponseModel?>>

    @POST("messages")
    @Headers("Content-Type: application/json")
    fun postReplyMessage(
        @Header("token") token: String,
        @Body data: MessageBodyModel
    ): Observable<BaseApiModel<MessageResponseModel?>>

    @GET("messages")
    @Headers("Content-Type: application/json")
    fun getListMessages(
        @Header("token") token: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<BaseApiModel<List<MessageResponseModel>?>>

    @POST("messages/referrence")
    @Headers("Content-Type: application/json")
    fun referenceSend(
        @Header("token") token: String,
        @Body data: ReferenceSendBodyModel
    ): Observable<BaseApiModel<String?>>

    @POST("location")
    @Headers("Content-Type: application/json")
    fun sendLocation(
        @Header("token") token: String,
        @Body data: LocationBodyModel
    ): Observable<BaseApiModel<Any?>>

    @POST("users/register")
    @Headers("Content-Type: application/json")
    fun registerUser(
        @Header("token") token: String,
        @Body data: RegisterBodyModel
    ): Observable<BaseApiModel<RegisterResponseModel?>>

    @POST("messages/voice")
    @Headers("Content-Type: application/json")
    fun sendAudio(
        @Header("token") token: String,
        @Body body: SendAudioBodyModel
    ): Observable<BaseApiModel<SendAudioResponseModel>>

    @Multipart
    @POST("upload")
    fun sendImage(
        @Header("token") token: String,
        @Part body: MultipartBody.Part
    ): Observable<BaseApiModel<UploadImageResponseModel>>

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
                    .readTimeout(120, TimeUnit.SECONDS)
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .build()
            } else {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request()
                        val newRequest = request.newBuilder().build()
                        chain.proceed(newRequest)
                    }
                    .readTimeout(120, TimeUnit.SECONDS)
                    .connectTimeout(120, TimeUnit.SECONDS)
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


        fun getApiServiceCustom(newUrl : String): VouchApiService {

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
                    .readTimeout(120, TimeUnit.SECONDS)
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .build()
            } else {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request()
                        val newRequest = request.newBuilder().build()
                        chain.proceed(newRequest)
                    }
                    .readTimeout(120, TimeUnit.SECONDS)
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .build()
            }

            val mRetrofit = Retrofit.Builder()
                .baseUrl("$newUrl/api/v2/widget/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(mClient)
                .build()

            return mRetrofit.create(VouchApiService::class.java)
        }
    }
}