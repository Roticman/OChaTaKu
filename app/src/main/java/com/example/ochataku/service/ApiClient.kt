package com.example.ochataku.service

import io.socket.client.IO
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    const val BASE_URL = "http://192.168.1.2:3000"
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    val socket = IO.socket(BASE_URL) // 换成你的服务地址

    fun connectAndListen(convId: Long, onMessageReceived: (JSONObject) -> Unit) {
        socket.connect()

        socket.on("chat:$convId") { args ->
            val data = args[0] as JSONObject
            onMessageReceived(data)
        }
    }
}



