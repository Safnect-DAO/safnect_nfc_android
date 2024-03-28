package com.populstay.safnect.key

import android.util.Log
import com.populstay.safnect.nfc.bean.KeyStorageBean

object KeyShareWrapper {

    fun pack(keyShare : String) : String{
        // 打包
        val packData = KeyStorageBean(keyShare = keyShare)
        // 序列化成json
        val jsonPackData = JsonUtil.toJson(packData)
        // 加密
        val encryptData = EncryptionUtils.encrypt(jsonPackData)
        Log.d("KeyShareWrapper","pack encryptData = $encryptData")
        return encryptData
    }

    fun unPack(packData : String) : KeyStorageBean?{
        val keyStorageBean: KeyStorageBean
        try {// 解密
            val decryptData = EncryptionUtils.decrypt(packData)
            // 解析
            keyStorageBean = JsonUtil.fromJson<KeyStorageBean>(decryptData)
            Log.d("KeyShareWrapper","unPack keyStorageBean = $keyStorageBean")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("KeyShareWrapper","unPack e = ${e.message}")
            return null
        }
        return keyStorageBean
    }
}