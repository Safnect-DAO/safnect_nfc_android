package com.populstay.safnect.nfc.huada;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.populstay.safnect.key.KeyShareWrapper;
import com.populstay.safnect.key.R;
import com.populstay.safnect.nfc.KeyStorageManager;
import com.populstay.safnect.nfc.bean.PrivateKeyShareInfoBean;

/**
 * 华大卡片写数据页面
 */
public class HuadaNFCWriteActivity extends NFCHuadaActivity {

    PrivateKeyShareInfoBean mPrivateKeyShareInfoBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPrivateKeyShareInfoBean = (PrivateKeyShareInfoBean) getIntent().getSerializableExtra(KeyStorageManager.PARA_KEY_SHARE_INFO);
        super.onCreate(savedInstanceState);

        String writeTargetData = getWriteTargetData();
        if (TextUtils.isEmpty(writeTargetData)){
            Toast.makeText(this,"Please enter the text to write",Toast.LENGTH_LONG).show();
        }else {
            mHuaDaNfcController.putWriteData(writeTargetData);
            mHuaDaNfcController.setAction(IHuaDaNfcController.ACTION_WRITE);
        }
    }

    protected void initViews() {
        super.initViews();
        titleTv.setText(R.string.write_to_scan);
        imgRead.setImageResource(R.mipmap.nfc_write_icon);
        if (mPrivateKeyShareInfoBean.getCurIndex() == 1){
            hintTv.setText(R.string.write_card_first_hint);
        }else{
            hintTv.setText(R.string.write_card_second_hint);
        }
    }

    private String getWriteTargetData(){
        String messageToWrite = mPrivateKeyShareInfoBean.getCurKeySare();
        if (!TextUtils.equals(messageToWrite, "null") && !TextUtils.isEmpty(messageToWrite)) {
            String keyShareEncrypt = KeyShareWrapper.INSTANCE.pack(messageToWrite);
            Log.d(TAG,"writeToNFC keyShare = " + messageToWrite + "keyShareEncrypt = " + keyShareEncrypt);
            return keyShareEncrypt;
        }
        return null;
    }

    @Override
    public void onStatusCallBack(int code, String desc) {
        super.onStatusCallBack(code, desc);
        // todo 异常状态还未处理
        if (code == Status.OK) {
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, (getString(R.string.message_write_error)), Toast.LENGTH_SHORT).show();
        }

    }
}
