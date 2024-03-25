package com.peerbits.nfccardread

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.populstay.safnect.nfc.bean.PrivateKeyShareInfoBean
import com.peerbits.nfccardread.databinding.ActivityCreateAccountBinding
import com.populstay.safnect.key.KeyUtil
import com.populstay.safnect.nfc.FileUtils
import com.populstay.safnect.nfc.NFC

class CreateAccountActivity : AppCompatActivity() {

     lateinit var binding : ActivityCreateAccountBinding
     var privateKeyShareInfoList = mutableListOf<PrivateKeyShareInfoBean>()
     companion object{
         const val TAG = "CreateAccountActivity"
     }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.CreateAccountDemoBtn.setOnClickListener {
            privateKeyShareInfoList.clear()
            // 1、完成私钥分片：拿到私钥分片、公钥、完整私钥（是否需要上链联调决定）
            val keyInfo = KeyUtil.sharesAndPublicKey(this)
            Log.d(TAG,"完成私钥分片 keyInfo = $keyInfo")
            // 2、上链获取钱包地址(这里就模拟一下)
            Log.d(TAG,"完成上链 模拟......")
            // 3、存储私钥分片，完成账户创建（调起NFC写入页面）
            keyInfo.privateKeyShareList?.let { shareList ->
                shareList.forEachIndexed { index, keyShare ->
                    if (0 == index){
                        // 第一片存储到手机端本地
                        FileUtils.writeToPrivateFile(this@CreateAccountActivity,"key.jks",keyShare)
                        return@forEachIndexed
                    }

                    // 剩下的存储到卡片
                    privateKeyShareInfoList.add(PrivateKeyShareInfoBean(shareList.size - 1,index,keyShare,""))
                }
            }

            // 开始触发存储第一张卡片
            dealNext()
        }
    }

    private fun dealNext() {
        if (privateKeyShareInfoList.isNotEmpty()){
            startForResult.launch(NFC.writeNFC(this, privateKeyShareInfoList.removeAt(0)))
        }
    }



    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (privateKeyShareInfoList.isNotEmpty()){
                showAlertDialog( "提示", "第1个分片已经存储成功，请放置第2张卡片", "确认", "取消",
                    {
                        dealNext()
                    },
                    {

                    })
            }else{
                showAlertDialog( "提示", "第2个私钥分片已经存储成功，账户已经创建完成，开始使用钱包吧", "确认", "取消",
                    {
                        finish()
                    },
                    {

                    })
            }
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