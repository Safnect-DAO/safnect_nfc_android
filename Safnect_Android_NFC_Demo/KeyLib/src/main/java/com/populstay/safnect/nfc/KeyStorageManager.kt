package com.populstay.safnect.nfc

import android.content.Context
import android.content.Intent
import android.util.Log
import com.populstay.safnect.key.EncryptionUtils
import com.populstay.safnect.nfc.bean.PrivateKeyShareInfoBean

object KeyStorageManager {

    private const val TAG = "KeyStorageManager"
    const val PARA_KEY_SHARE_INFO = "keyShareInfo"

    fun readNFC(context: Context) : Intent{
        return Intent(context, NFCReadActivity::class.java)
    }

    fun writeNFC(context: Context,keyShareInfo : PrivateKeyShareInfoBean) : Intent{
        val intent = Intent(context, NFCWriteActivity::class.java)
        intent.putExtra(PARA_KEY_SHARE_INFO,keyShareInfo)
        return intent
    }

    fun readMobileLocal(context: Context) :String{
        val localKeyShare = FileUtils.readFromPrivateFile(context,"key.jks")
        val localKeyShareDecrypt = EncryptionUtils.decrypt(localKeyShare)
        Log.d(TAG,"readMobileLocal localKeyShare = $localKeyShare,localKeyShareDecrypt = $localKeyShareDecrypt")
        return localKeyShareDecrypt
    }

    fun writeMobileLocal(context: Context,keyShare : String){
        val keyShareEncrypt = EncryptionUtils.encrypt(keyShare)
        Log.d(TAG,"readMobileLocal keyShare = $keyShare,keyShareEncrypt = $keyShareEncrypt")
        FileUtils.writeToPrivateFile(context,"key.jks",keyShareEncrypt)
    }

}