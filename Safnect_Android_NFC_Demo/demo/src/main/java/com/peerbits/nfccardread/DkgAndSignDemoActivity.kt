package com.peerbits.nfccardread

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.peerbits.nfccardread.databinding.ActivityDkgAndSignDemoBinding

class DkgAndSignDemoActivity : Activity() {

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
    }
}