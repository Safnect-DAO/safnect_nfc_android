package com.populstay.safnect.nfc

import android.content.Context
import android.content.Intent
import android.util.Log
import com.populstay.safnect.key.EncryptionUtils
import com.populstay.safnect.key.KeyShareWrapper
import com.populstay.safnect.nfc.bean.PrivateKeyShareInfoBean
import com.populstay.safnect.nfc.huada.HuadaNFCReadActivity
import com.populstay.safnect.nfc.huada.HuadaNFCWriteActivity

object KeyStorageManager {

    private const val TAG = "KeyStorageManager"
    const val PARA_KEY_SHARE_INFO = "keyShareInfo"

    fun readNFC(context: Context,isUseHuada : Boolean = false) : Intent{
        if (isUseHuada){
            return Intent(context, HuadaNFCReadActivity::class.java)
        }
        return Intent(context, NFCReadActivity::class.java)
    }

    fun writeNFC(context: Context,keyShareInfo : PrivateKeyShareInfoBean,isUseHuada : Boolean = false) : Intent{
        val intent = if (isUseHuada){
            Intent(context, HuadaNFCWriteActivity::class.java)
        }else{
            Intent(context, NFCWriteActivity::class.java)
        }
        intent.putExtra(PARA_KEY_SHARE_INFO,keyShareInfo)
        return intent
    }

    fun readMobileLocal(context: Context) :String?{
        val localKeyShare = FileUtils.readFromPrivateFile(context,"key.jks")
        val keyStorageBean = KeyShareWrapper.unPack(localKeyShare)
        val localKeyShareDecrypt = keyStorageBean?.keyShare
        Log.d(TAG,"readMobileLocal localKeyShare = $localKeyShare,localKeyShareDecrypt = $localKeyShareDecrypt")
        return localKeyShareDecrypt
    }

    fun writeMobileLocal(context: Context,keyShare : String){
        val keyShareEncrypt = KeyShareWrapper.pack(keyShare)
        Log.d(TAG,"readMobileLocal keyShare = $keyShare,keyShareEncrypt = $keyShareEncrypt")
        FileUtils.writeToPrivateFile(context,"key.jks",keyShareEncrypt)
    }

}