package com.populstay.safnect.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.populstay.safnect.key.R;
import com.populstay.safnect.nfc.bean.PrivateKeyShareInfoBean;

import java.io.IOException;

public class NFCWriteActivity extends Activity {

    public static final String TAG = NFCWriteActivity.class.getSimpleName();
    private NfcAdapter mNfcAdapter;
    private boolean isWrite = true;

    private TextView hintTv,titleTv;
    private Button cancelBtn;

    PrivateKeyShareInfoBean mPrivateKeyShareInfoBean;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_read_and_write_activity);
        mPrivateKeyShareInfoBean = (PrivateKeyShareInfoBean) getIntent().getSerializableExtra(NFC.PARA_KEY_SHARE_INFO);
        initWindowLocation();
        initViews();
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initWindowLocation() {
        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void initViews() {
        cancelBtn = findViewById(R.id.cancelBtn);
        titleTv = findViewById(R.id.titleTv);
        titleTv.setText(R.string.write_to_scan);
        hintTv = findViewById(R.id.hintTv);
        if (mPrivateKeyShareInfoBean.getCurIndex() == 1){
            hintTv.setText(R.string.write_card_first_hint);
        }else{
            hintTv.setText(R.string.write_card_second_hint);
        }
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    public boolean writeTag(Tag tag, NdefMessage message) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    return false;
                }
                ndef.writeNdefMessage(message);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (tag != null) {
            if (isWrite) {
                String messageToWrite = mPrivateKeyShareInfoBean.getCurKeySare();

                if (messageToWrite != null && (!TextUtils.equals(messageToWrite, "null")) && (!TextUtils.isEmpty(messageToWrite))) {
                    NdefRecord record = NdefRecord.createMime(messageToWrite, messageToWrite.getBytes());
                    NdefMessage message = new NdefMessage(new NdefRecord[]{record});

                    if (writeTag(tag, message)) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, (getString(R.string.message_write_error)), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this,"Please enter the text to write",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
