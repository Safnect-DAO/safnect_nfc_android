package com.populstay.safnect.nfc

import android.content.Context
import android.content.Intent
import com.populstay.safnect.nfc.bean.PrivateKeyShareInfoBean

object NFC {

    const val PARA_KEY_SHARE_INFO = "keyShareInfo"

    fun readNFC(context: Context) : Intent{
        return Intent(context, NFCReadActivity::class.java)
    }

    fun writeNFC(context: Context,keyShareInfo : PrivateKeyShareInfoBean) : Intent{
        val intent = Intent(context, NFCWriteActivity::class.java)
        intent.putExtra(PARA_KEY_SHARE_INFO,keyShareInfo)
        return intent
    }

}