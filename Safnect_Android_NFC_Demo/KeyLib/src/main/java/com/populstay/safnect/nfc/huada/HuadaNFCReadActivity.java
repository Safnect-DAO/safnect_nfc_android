package com.populstay.safnect.nfc.huada;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.populstay.safnect.key.KeyShareWrapper;
import com.populstay.safnect.key.R;
import com.populstay.safnect.nfc.bean.KeyStorageBean;

/**
 * 华大卡片读取数据页面
 */
public class HuadaNFCReadActivity extends NFCHuadaActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHuaDaNfcController.setAction(IHuaDaNfcController.ACTION_READ);
    }

    protected void initViews() {
        super.initViews();
        titleTv.setText(R.string.ready_to_scan);
        imgRead.setImageResource(R.mipmap.nfc_read_icon);
        hintTv.setText(R.string.ready_card_hint);
    }

    @Override
    public void onStatusCallBack(int code, String desc) {
        super.onStatusCallBack(code, desc);

        // todo 异常状态还未处理
        if (code == Status.OK) {
            String keyShare = desc;

            KeyStorageBean keyStorageBean = KeyShareWrapper.INSTANCE.unPack(keyShare);
            if (null == keyStorageBean || !KeyStorageBean.APP_TAG.equalsIgnoreCase(keyStorageBean.getTag())){
                Toast.makeText(this, "This card is not supported. Please confirm if the card is correct", Toast.LENGTH_LONG).show();
                return;
            }
            String keyShareDecrypt = keyStorageBean.getKeyShare();
            Log.d(TAG,"readFromNFC keyShare = " + keyShare + "keyShareDecrypt = " + keyShareDecrypt);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("keyShare", keyShareDecrypt);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }
}
