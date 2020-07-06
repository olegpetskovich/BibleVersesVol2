package com.android.bible.knowbible.data.network.retrofit

import com.android.bible.knowbible.BuildConfig
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MyRetrofit {
//    fun buildRetrofit(): Retrofit {
//        val okClientBuilder = initOkHttp()
//
//        val builder = Retrofit.Builder()
////                .baseUrl("https://dbt.io/library/")
//                .baseUrl("https://api.scripture.api.bible/v1/")
//                .client(okClientBuilder.build()) //
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
//                .addConverterFactory(GsonConverterFactory.create())
//
//        return builder.build()
//    }
//
//    private fun initOkHttp(): OkHttpClient.Builder {
//        val okClientBuilder = OkHttpClient.Builder()
//        okClientBuilder.connectTimeout(10, TimeUnit.SECONDS)
//        okClientBuilder.readTimeout(10, TimeUnit.SECONDS)
//        if (BuildConfig.DEBUG) {
//            val interceptor = HttpLoggingInterceptor()
//            interceptor.level = HttpLoggingInterceptor.Level.BODY
//            okClientBuilder.addInterceptor(interceptor)
//        }
//        return okClientBuilder
//    }
}