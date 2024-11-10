package com.populstay.safnect.nfc.huada

/**
 * HuadaNFC卡片读写状态码
 * 90 00 命令执行成功
 * 67 00 Lc 长度错误
 * 69 81 当前文件不是记录文件
 * 69 82
 * 1、需要认证父目录的 MK，但未通过验证
 * 2、需要认证当前目录的 MK，但未通过验证
 * 3、需要通过应用主 PIN，但未通过验证
 * 4、安全状态字节不满足安全权限要求的安全状态
 *
 * 69 83 认证密钥锁定
 * 69 84 引用数据无效（未申请随机数）
 * 69 85
 * 1、禁止添加操作
 * 2、全局安全状态字节 X>Y 或局部安全状态字节 X>Y
 *
 * 69 86 没有选择当前文件
 * 69 88 安全信息（MAC 和加密）数据错误
 * 6A 81 功能不支持
 * 6A 82 未找到文件
 * 6A 84 记录空间已满
 * 6A 85 Lc 与 TLV 结构不匹配
 * 6A 86 P1、P2 参数错
 * 6A 88 未找到密钥数据
 * 6D 00 命令不存在
 * 6E 00 CLA 错
 * 93 03 应用永久锁定
 */


data class HuadaNFC(val result: String, val desc :String, val code : String)

object Status{
    // 卡片认证失败（不是我方产品的卡）
    const val AUTH_FAILED = 0
    // 账户匹配失败（当前分片与手机端的分片不属于同一个密钥的）
    const val ACCOUNT_MATCH_FAILED = 1
    // 卡片已经被锁定（The card is locked）
    const val ACCOUNT_CARD_LOCKED = 2
    // 操作成功
    const val OK = -1
    // 未知错误
    const val UNKNOWN_ERROR = -2
}
