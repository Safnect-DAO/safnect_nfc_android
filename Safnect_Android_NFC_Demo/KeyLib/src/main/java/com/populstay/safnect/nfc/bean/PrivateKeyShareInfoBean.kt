package com.populstay.safnect.nfc.bean

import java.io.Serializable

/**
 * count 私钥分片数量（需存储到卡片的数量）
 * curIndex 当前操作存储的下标
 * curKeySare 当前操作存储的分片
 * curHint 当前操作存储的提示语
 */
data class PrivateKeyShareInfoBean(val count: Int, val curIndex :Int, val curKeySare :String, val curHint : String) : Serializable