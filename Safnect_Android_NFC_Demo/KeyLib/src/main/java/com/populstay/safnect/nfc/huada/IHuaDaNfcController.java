package com.populstay.safnect.nfc.huada;

import android.content.Context;
import android.content.Intent;

/**
 * 华大NFC控制器对外接口
 */
public interface IHuaDaNfcController {
    int ACTION_WRITE = 1;
    int ACTION_READ = 0;
    void initNfcAdapter(Context context,IHuaDaNfcStatusCallBack callBack);
    void enableForegroundDispatch();
    void disableForegroundDispatch();
    void onNewIntent(Intent intent);
    void setAction(int action);
    int getAction();
    boolean isWriteAction();
    void putWriteData(String data);
}
