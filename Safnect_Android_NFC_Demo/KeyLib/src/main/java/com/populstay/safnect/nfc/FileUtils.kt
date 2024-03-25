package com.populstay.safnect.nfc

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

object FileUtils {
    private const val TAG = "FileUtils"

    fun writeToPrivateFile(context: Context, fileName: String?, content: String): Boolean {
        var outputStream: FileOutputStream? = null
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            outputStream.write(content.toByteArray())
            return true
        } catch (e: Exception) {
            Log.e(TAG, "写入文件出错", e)
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    Log.e(TAG, "关闭文件流出错", e)
                }
            }
        }
        return false
    }

    fun readFromPrivateFile(context: Context, fileName: String?): String {
        var inputStream: FileInputStream? = null
        val content = StringBuilder()
        try {
            inputStream = context.openFileInput(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                content.append(line)
            }
            return content.toString()
        } catch (e: Exception) {
            Log.e(TAG, "读取文件出错", e)
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    Log.e(TAG, "关闭文件输入流出错", e)
                }
            }
        }
        return ""
    }
}