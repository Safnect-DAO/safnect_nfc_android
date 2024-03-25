package com.populstay.safnect.key.bean

/**
 * privateKeyShareList 私钥分片
 * publicKeyList 公钥(x,y)
 * privateKey 完整私钥
 */
class KeyInfoBean (val privateKeyShareList: List<String>? = null, val publicKeyList: List<String>? = null, val privateKey: String? = null)