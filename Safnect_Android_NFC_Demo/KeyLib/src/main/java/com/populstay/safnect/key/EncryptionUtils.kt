package com.populstay.safnect.key



import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {

    // AES加密算法的密钥和初始化向量
    // 注意：这些值仅用于演示，不应该用于实际的安全应用（先调通功能）
    private const val FIXED_KEY = "89012abc345de67f" // 16字节密钥 (AES-128)
    private const val FIXED_IV = "ac78b9f012de3456"; // 16字节初始化向量

    // 加密方法
    fun encrypt(plainText: String): String {
        val key = SecretKeySpec(FIXED_KEY.toByteArray(StandardCharsets.UTF_8), "AES")
        val iv = IvParameterSpec(FIXED_IV.toByteArray(StandardCharsets.UTF_8))

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)

        val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    // 解密方法
    fun decrypt(encryptedText: String): String {
        val key = SecretKeySpec(FIXED_KEY.toByteArray(StandardCharsets.UTF_8), "AES")
        val iv = IvParameterSpec(FIXED_IV.toByteArray(StandardCharsets.UTF_8))

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, iv)

        val decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}
