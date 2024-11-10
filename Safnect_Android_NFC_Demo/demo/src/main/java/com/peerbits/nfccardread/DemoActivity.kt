package com.peerbits.nfccardread

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.peerbits.nfccardread.databinding.ActivityDkgAndSignDemoBinding
import com.peerbits.nfccardread.huada.HuaDaDemoActivity
import com.populstay.safnect.key.KeyGenerator
import com.populstay.safnect.nfc.huada.NFCHuadaActivity
import com.populstay.safnect.nfc.huada.NFCHuadaTestActivity
import com.populstay.safnect.sm4.SM4Activity

class DemoActivity : Activity() {

    companion object{
        const val TAG = "DkgAndSignDemoActivity"
    }

    lateinit var binding : ActivityDkgAndSignDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDkgAndSignDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.CreateAccountDemoBtn.setOnClickListener {
            startActivity(Intent(this,CreateAccountActivity::class.java))
        }
        binding.TransactionSignBtn.setOnClickListener {
            startActivity(Intent(this,TransactionSignActivity::class.java))
        }

        binding.restoreShareBtn.setOnClickListener {
            restoreShare()
        }


        binding.readNfcABtn.setOnClickListener {
            startActivity(Intent(this,
                NFCHuadaTestActivity::class.java))
        }

        binding.btnSM4.setOnClickListener {
            startActivity(Intent(this, SM4Activity::class.java))
        }

        binding.btnIsoDemo.setOnClickListener {
            startActivity(Intent(this, NFCHuadaActivity::class.java))
        }

        binding.btnIsoDemo2.setOnClickListener {
            startActivity(Intent(this, HuaDaDemoActivity::class.java))
        }
    }

    fun restoreShare(){
        val keyShareList = mutableListOf<String>()
        //keyShareList.add("3,9660746574333387669987758032394076171359420807832561188590977702878927779725")
        keyShareList.add("2,41095253964065988764168619629557214417927267597196692727301883413936205345443")
        keyShareList.add("1,72529761353798589858349481226720352664495114386560824266012789124993482911161")
        val restoreShare =  KeyGenerator.restoreShareAndPublicKey(this,keyShareList)
        Log.d(TAG,"restoreShare =  $restoreShare")
    }
}