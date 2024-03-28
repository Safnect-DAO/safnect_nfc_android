package com.populstay.safnect.key

import com.google.gson.Gson

object JsonUtil {

    val gson = Gson()

    // 将对象序列化为JSON字符串
    fun toJson(obj: Any): String {
        return gson.toJson(obj)
    }

    // 将JSON字符串解析为指定类型的对象
    inline fun <reified T> fromJson(json: String): T {
        return gson.fromJson(json, T::class.java)
    }
}
