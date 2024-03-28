package com.populstay.safnect.nfc.bean




data class KeyStorageBean(val tag : String = APP_TAG, val keyShare: String, val extendInfo : String? = null){
    companion object{
        const val APP_TAG = "com.populstay.wallet.safnect"
    }
}