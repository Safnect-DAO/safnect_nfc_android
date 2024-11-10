package com.populstay.safnect.sm4

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.populstay.safnect.key.SM4UTIL
import com.populstay.safnect.key.TripleDESEncryption
import com.populstay.safnect.key.databinding.ActivitySm4Binding
import kotlinx.android.synthetic.main.activity_sm4.deBtn
import kotlinx.android.synthetic.main.activity_sm4.deBtn3des
import kotlinx.android.synthetic.main.activity_sm4.enBtn
import kotlinx.android.synthetic.main.activity_sm4.enBtn3des


class SM4Activity : Activity() {

    lateinit var binding : ActivitySm4Binding
    companion object{
        const val EN_KEY = "c436b991a673b8b0c6bc991a9b5ebfd6"
        const val EN_KEY_2 = "83fa3e48684627e1b024b1fba21d9b70"
        const val EN_KEY_3 = "随机密码"
        const val TAG = "SM4Activity"

        const val EN_KEY_3DES = "123"
    }

    var lastEncodeResult :String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySm4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        enBtn.setOnClickListener {
            SM4UTIL.getKey()

            val text = getContent()
            if (text.isNullOrEmpty()){
                Toast.makeText(this@SM4Activity,"请输入内容",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val encodeResult = SM4UTIL.encodeToHexByStr(text,EN_KEY_3)
            lastEncodeResult = encodeResult
            Log.d(TAG,"encode->text=$text->encodeResult=$encodeResult")
        }
        deBtn.setOnClickListener {
            if (TextUtils.isEmpty(lastEncodeResult)){
                Toast.makeText(this@SM4Activity,"请先操作加密",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val decodeResult = SM4UTIL.decodeFromHexByStr(lastEncodeResult,EN_KEY_3)
                Log.d(TAG,"decode->encodeText=$lastEncodeResult->decodeResult=$decodeResult")
            }catch (e : Exception){
                e.printStackTrace()
                Toast.makeText(this@SM4Activity,"秘钥错误，解密失败",Toast.LENGTH_SHORT).show()
            }
        }

        enBtn3des.setOnClickListener {
            val text = getContent()
            if (text.isNullOrEmpty()){
                Toast.makeText(this@SM4Activity,"3des 请输入内容",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val encodeResult = TripleDESEncryption.encrypt(text,EN_KEY_3DES)
            lastEncodeResult = encodeResult
            Log.d(TAG,"3des encode->text=$text->encodeResult=$encodeResult")
        }
        deBtn3des.setOnClickListener {
            if (TextUtils.isEmpty(lastEncodeResult)){
                Toast.makeText(this@SM4Activity,"3des 请先操作加密",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val decodeResult = TripleDESEncryption.decrypt(lastEncodeResult,EN_KEY_3DES)
                Log.d(TAG,"3des decode->encodeText=$lastEncodeResult->decodeResult=$decodeResult")
            }catch (e : Exception){
                e.printStackTrace()
                Toast.makeText(this@SM4Activity,"3des 秘钥错误，解密失败",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getContent(): String? {
        return binding.contentEdit.text?.toString()
    }
}