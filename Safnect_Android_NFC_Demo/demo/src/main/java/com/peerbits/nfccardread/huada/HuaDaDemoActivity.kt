package com.peerbits.nfccardread.huada

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.peerbits.nfccardread.databinding.ActivityHuadaDemoBinding

class HuaDaDemoActivity : Activity() {

    companion object{
        const val TAG = "HuaDaDemoActivity"
    }

    lateinit var binding : ActivityHuadaDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHuadaDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.CreateAccountDemoBtn.setOnClickListener {
            startActivity(Intent(this,HuaDaCreateAccountActivity::class.java))
        }
        binding.TransactionSignBtn.setOnClickListener {
            startActivity(Intent(this,HuaDaTransactionSignActivity::class.java))
        }
    }
}