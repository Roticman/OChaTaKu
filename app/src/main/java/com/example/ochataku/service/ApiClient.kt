package com.example.ochataku.service

import com.example.ochataku.model.ChatRequest
import com.example.ochataku.model.ChatResponse
import com.example.ochataku.model.Message
import io.socket.client.IO
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    const val BASE_URL = "http://172.20.10.6:3000" //替换成当前本地服务器的IPV4地址，或者云服务器地址。
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

object DeepSeekClient {

    private const val BASE_URL = "https://api.deepseek.com/"
    private const val API_KEY = "sk-159008c9963d4fb9928aaedda79778f8"

    private val apiService: DeepSeekApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepSeekApiService::class.java)
    }

    fun fetchResponseFor(prompt: String, callback: (String?) -> Unit) {
        val request = ChatRequest(
            model = "deepseek-chat",
            messages = listOf(
                Message("system", "你是一个智能聊天助手"),
                Message("user", prompt)
            )
        )

        val call = apiService.createCompletion(request)
        call.enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                val reply = response.body()?.choices?.getOrNull(0)?.message?.content
                callback(reply)
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                callback(null)
            }
        })
    }
}


