package com.peerbits.nfccardread

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.peerbits.nfccardread.databinding.ActivityTransactionSignBinding
import com.populstay.safnect.key.KeyUtil
import com.populstay.safnect.nfc.FileUtils
import com.populstay.safnect.nfc.NFC

class TransactionSignActivity : AppCompatActivity() {

    companion object{
        const val TAG = "TransactionSignActivity"
    }

    lateinit var binding : ActivityTransactionSignBinding
    val keyShareList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionSignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.TransactionSignBtn.setOnClickListener {
            keyShareList.clear()
            // 1、录入交易信息后，生成交易请求（具体看MVC链SDk）
            // 2、读取私钥分片进行私钥恢复
            // 3、使用私钥完成签名
            // 手机本地的私钥分片
            val localKeyShare = FileUtils.readFromPrivateFile(this,"key.jks")
            Log.d(TAG,"localKeyShare =  $localKeyShare")
            keyShareList.add(localKeyShare)
            // 卡片私钥匙分片
            startForResult.launch(NFC.readNFC(this))
        }

    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val cardKeyShare = data?.getStringExtra("keyShare")
            Log.d(TAG,"cardKeyShare =  $cardKeyShare")
            cardKeyShare?.let {
                keyShareList.add(it)
            }
            val privateKey = KeyUtil.restoreKey(this@TransactionSignActivity,keyShareList)
            Log.d(TAG,"私钥恢复后去交易签名privateKey =  $privateKey")
            showAlertDialog( "提示", "私钥已经恢复成功，请开始签名交易 privateKey = $privateKey", "确认", "取消",
                {
                    // 请开始给交易签名

                },
                {

                })


        }else{
            // 失败 或者 取消
        }
    }

    fun showAlertDialog(title: String, message: String, positiveText: String, negativeText: String, positiveClick: () -> Unit, negativeClick: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveText) { dialog, _ -> positiveClick() }
        //builder.setNegativeButton(negativeText) { dialog, _ -> negativeClick() }
        val alertDialog = builder.create()
        alertDialog.show()
    }
}