package com.populstay.safnect.nfc.huada;

import android.app.Activity;
import android.content.Intent;
import android.nfc.TagLostException;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.populstay.safnect.key.R;


public class NFCHuadaActivity extends Activity implements IHuaDaNfcStatusCallBack{

    public static final String TAG = NFCHuadaActivity.class.getSimpleName();
    protected TextView hintTv,titleTv;
    protected Button cancelBtn;
    protected ImageView imgRead;
    protected HuaDaNfcController mHuaDaNfcController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_read_and_write_activity);
        initHuaDaNfcController();
        initWindowLocation();
        initViews();
    }

    private void initHuaDaNfcController() {
        mHuaDaNfcController = new HuaDaNfcController();
        mHuaDaNfcController.initNfcAdapter(this,this);
        //mHuaDaNfcController.setAction(IHuaDaNfcController.ACTION_WRITE);
        //mHuaDaNfcController.setAction(IHuaDaNfcController.ACTION_READ);
    }

    private void initWindowLocation() {
        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    protected void initViews() {
        titleTv = findViewById(R.id.titleTv);
        titleTv.setText(R.string.ready_to_scan);
        hintTv = findViewById(R.id.hintTv);
        hintTv.setText(R.string.ready_card_hint);
        cancelBtn = findViewById(R.id.cancelBtn);
        imgRead = findViewById(R.id.imgRead);
        imgRead.setImageResource(R.mipmap.nfc_read_icon);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mHuaDaNfcController){
            mHuaDaNfcController.enableForegroundDispatch();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mHuaDaNfcController){
            mHuaDaNfcController.disableForegroundDispatch();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (null != mHuaDaNfcController){
            mHuaDaNfcController.onNewIntent(intent);
        }
    }

    @Override
    public void onStatusCallBack(int code, String desc) {

    }
}
