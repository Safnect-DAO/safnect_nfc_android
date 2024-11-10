package com.populstay.safnect.nfc.huada;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.populstay.safnect.key.R;
import com.populstay.safnect.sm4.Sm4MacUtil;
import com.populstay.safnect.sm4.Sm4Util;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class NFCHuadaTestActivity extends Activity {

    public static final String TAG = NFCHuadaTestActivity.class.getSimpleName();
    private TextView hintTv,titleTv;
    private NfcAdapter mNfcAdapter;
    private Button cancelBtn;

    private ImageView imgRead;


    // 内部认证密钥(App识别卡片)
    static String INTERNAL_AUTH_KEY = "99999999999999999999999999999999";
    // 数据维护密钥
    static String DATA_KEY = "63714B39F6B950FA30D8474DD4944D85";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_read_and_write_activity);
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
        titleTv = findViewById(R.id.titleTv);
        titleTv.setText(R.string.ready_to_scan);
        hintTv = findViewById(R.id.hintTv);
        hintTv.setText(R.string.ready_card_hint);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        cancelBtn = findViewById(R.id.cancelBtn);
        imgRead = findViewById(R.id.imgRead);
        imgRead.setImageResource(R.mipmap.nfc_read_icon);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);

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
        byte[] uidByte = tag.getId();
        String Uid = ByteArrayToHexString(uidByte);
        Log.d(TAG, "onNewIntent-->Uid:" + Uid);

        writeData(tag);

    }
    private void writeData(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        try{
            isoDep.connect();
            // 1、APDU=00A4040008 A000000003000000  选择安全域
            String selectSafeApDu = "00A4040008A000000003000000";
            byte[] selectSafeCommand = ByteUtils.fromHexString(selectSafeApDu);
            byte[] selectSafeResponse = isoDep.transceive(selectSafeCommand);
            Log.d(TAG, "选择安全域发送指令-->command:" + selectSafeApDu +", 指令响应response:" + ByteUtils.toHexString(selectSafeResponse));

            // 2、APDU=00A4040009 53532EB1B1BEA9CDA8 选择应用
            String selectAppApDu = "00A404000953532EB1B1BEA9CDA8";
            byte[] selectAppCommand = ByteUtils.fromHexString(selectAppApDu);
            byte[] selectAppResponse = isoDep.transceive(selectAppCommand);
            Log.d(TAG, "选择应用发送指令-->command:" + selectAppApDu +", 指令响应response:" + ByteUtils.toHexString(selectAppResponse));

            // 3、内部认证(App识别卡片)
            // ① APDU=0084000008 取8位随机数
            String authRandom8bit = getRandom8ByApDu(isoDep);
            String authRandom = authRandom8bit;
            // ② 补位0000000000000000
            authRandom += "0000000000000000";
            Log.d(TAG, "内部认证--取随机数发送指令-->authRandom:" + authRandom);
            // ③ 计算认证数据
            String encodeAuthData = ByteUtils.toHexString(Sm4Util.encrypt_ECB_NoPadding(ByteUtils.fromHexString(INTERNAL_AUTH_KEY),ByteUtils.fromHexString(authRandom)));
            // 解密出来看下加密是否正确（调试使用）
            String decodeAuthData = ByteUtils.toHexString(Sm4Util.decrypt_ECB_NoPadding(ByteUtils.fromHexString(INTERNAL_AUTH_KEY),ByteUtils.fromHexString(encodeAuthData)));
            Log.d(TAG, "内部认证--计算认证数据-->encodeAuthData = " + encodeAuthData+",decodeAuthData = " + decodeAuthData);
            // 加密数据取左/右8字节
            String leftEnRandom = encodeAuthData.substring(0,16);
            String rightEnRandom = encodeAuthData.substring(16,32);
            Log.d(TAG, "内部认证--加密数据取左右8字节-->leftEnRandom:" + leftEnRandom +", rightEnRandom:" + rightEnRandom);
            // 做xor运算
            byte[] leftEnRandomByte = ByteUtils.fromHexString(leftEnRandom);
            byte[] rightEnRandomByte = ByteUtils.fromHexString(rightEnRandom);
            byte[] xorResult = ByteUtils.xor(leftEnRandomByte,rightEnRandomByte);
            String resultHex = ByteUtils.toHexString(xorResult);
            Log.d(TAG, "内部认证-->异或结果resultHex:" + resultHex);
            //;0088000108|鉴别数据输入因子|08
            // ④ APDU = 0088000008 (RESP)08,0x9000 发送指令鉴别数据
            String getAuthDataApDu = "0088000008"+ authRandom8bit + "08";
            byte[] getAuthDataCommand = ByteUtils.fromHexString(getAuthDataApDu);
            byte[] getAuthDataResponse = isoDep.transceive(getAuthDataCommand);
            String getAuthDataResult = ByteUtils.toHexString(getAuthDataResponse);
            getAuthDataResult = getAuthDataResult.substring(0,16);
            Log.d(TAG, "内部认证-->发送指令鉴别数据-->getAuthDataResult:" + getAuthDataResult);
            // ⑤ 开始做内部认证
            if (TextUtils.equals(resultHex.toUpperCase(),getAuthDataResult.toUpperCase())){
                Log.d(TAG, "内部认证-->认证通过");
                // 4、APDU=00A4040008 53532EB1B1BEA9CDA8 , 0x9000 选择文件
                String selectFileApDu = "00A40000020015";
                byte[] selectFileCommand = ByteUtils.fromHexString(selectFileApDu);
                byte[] selectFileResponse = isoDep.transceive(selectFileCommand);
                Log.d(TAG, "选择文件发送指令-->command:" + selectFileApDu +", 指令响应response:" + ByteUtils.toHexString(selectFileResponse));

                // 5、写文件
                // ① 数据长度 + 数据 拼接
                String dataTarget = "11aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbb22";
                //dataTarget = "cR9JHCFLVjPMNLFVmqZ2nDA9nrV8+fYkzdQgNxIMo8MnmTwd9oJuIx20tdQtp+b6+ZK6BwLfJpsiVqjfATWFauA04UNFlwFqJj9sImPYDFGq2Fk2e7skvs6V9Q1+pCS82k4VVwerXn1d7oIV0GSpG/QM7dIeE93JfcgiR7XS220MfPtsGS4ohTWrHwSe2uwt";
                dataTarget = "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
                int dataTargetLen = dataTarget.length() / 2;
                String len = Sm4MacUtil.convertToEvenHex(dataTargetLen);
                String lenAndData = len + dataTarget;
                Log.d(TAG, "-->明文数据:" + dataTarget);
                Log.d(TAG, "-->明文数据长度:" + len);
                Log.d(TAG, "-->明文数据与长度拼接:" + lenAndData);
                // ② 数据补位到16的倍数
                String paddingResult = Sm4MacUtil.padToMultipleOf32(lenAndData);// 动态补位到32的整数倍（即16字节的整数倍），首先使用80，后续跟着00
                Log.d(TAG, "-->明文数据与长度拼接后的数据补位:" + paddingResult);
                // ③ 数据加密（SM4）
                String chiperdata = ByteUtils.toHexString(Sm4Util.encrypt_ECB_NoPadding(ByteUtils.fromHexString(DATA_KEY), ByteUtils.fromHexString(paddingResult)));
                Log.d(TAG, "-->补位并加密后的数据= " +  chiperdata);
                Log.d(TAG, "-->完成数据加密------------------------------------ ");
                int chiperdataLen = chiperdata.length() / 2;
                String chiperdataLenHex = Sm4MacUtil.convertToEvenHex(chiperdataLen);
                Log.d(TAG, "-->加密数据后的长度= " +  chiperdataLenHex);
                String chiperdataLenHexAdd4 = Sm4MacUtil.convertToEvenHex(chiperdataLen + 4);
                Log.d(TAG, "-->加密数据后的长度+mac前4字节长度= " +  chiperdataLenHexAdd4);

                String iv = getRandom8ByApDu(isoDep) + "0000000000000000";
                Log.d(TAG, "-->向量初值iv= " +  iv);
                Log.d(TAG, "-->维护密钥key= " + DATA_KEY);

                // 计算mac传入：IV是向SE取8位随机数再补8字节00，data是04D60000+len（加密数据长度+4字节mac长度）+加密数据+80+...+00补位规则。
                // 发送给安全芯片的APDU指令：04D60000+len（加密数据长度+4字节mac长度）+加密数据+4字节mac
                String HEAD = "04D60000";
                String mac_data =  HEAD + chiperdataLenHexAdd4 + chiperdata;
                mac_data = Sm4MacUtil.padToMultipleOf32(mac_data);// 动态补位到32的整数倍（即16字节的整数倍），首先使用80，后续跟着00
                Log.d(TAG, "-->计算mac地址使用的数据= " +  mac_data);
                String MAC = ByteUtils.toHexString(Sm4Util.doCBCMac(ByteUtils.fromHexString(DATA_KEY),ByteUtils.fromHexString(iv),null,ByteUtils.fromHexString(mac_data)));
                Log.d(TAG, "-->计算得出的MAC= " +  MAC);

                MAC = Sm4MacUtil.getSm4mac(iv, DATA_KEY,mac_data);
                Log.d(TAG, "-->计算得出的MAC= " +  MAC);
                Log.d(TAG, "-->MAC前4字节= " + getFront8(MAC));

                Log.d(TAG, "-->完成MAC地址计算------------------------------------ ");

                // 写入数据 APDU=(HEAD)(ChiperLen)(Chiperdata)(MAC)
               String writeDataApdu = HEAD + chiperdataLenHexAdd4 + chiperdata + getFront8(MAC);
                Log.d(TAG, "-->写入指令Apdu= " +  writeDataApdu);
                byte[] writeDataCommand = ByteUtils.fromHexString(writeDataApdu.toUpperCase());
                byte[] writeDataCommandResponse = isoDep.transceive(writeDataCommand);
                String writeDataCommandResponseResult = ByteUtils.toHexString(writeDataCommandResponse);
                Log.d(TAG, "-->写入指令响应Result= " +  writeDataCommandResponseResult);

                // 6、读取文件
                // ① APDU=0084000008 取8位随机数
                String readIv = getRandom8ByApDu(isoDep) + "0000000000000000";
                // 读取文件固定补充"8000000000000000000000"
                String MAC_DATA = "04B0000004"+ "8000000000000000000000";
                String readMAC = Sm4MacUtil.getSm4mac(readIv, DATA_KEY,MAC_DATA);
                Log.d(TAG, "-->readMAC前4字节= " + getFront8(readMAC));
                Log.d(TAG, "读取文件发送指令-->readIv:" + readIv +", MAC_DATA:" + MAC_DATA);
                // ② 发送读文件指令
                String readFileApDu = "04B0000004"+ getFront8(readMAC);
                byte[] readFileCommand = ByteUtils.fromHexString(readFileApDu);
                byte[] readFileResponse = isoDep.transceive(readFileCommand);
                // 结果长这样：7cceeef4c610ca854dd4bd5ff16e2817aa4e5bee35d55dfe8ccc9d06d6bcdc709000
                String readResult = ByteUtils.toHexString(readFileResponse);
                readResult= Sm4MacUtil.suffixPattern(readResult,"9000");
                Log.d(TAG, "读取文件发送指令-->command:" + readFileApDu +", 指令响应response:" + readResult);

                // 7、解密数据
                byte[] decryptData = Sm4Util.decrypt_ECB_NoPadding(ByteUtils.fromHexString(DATA_KEY),ByteUtils.fromHexString(readResult));
                Log.d(TAG, "数据解密结果-->decryptData:" + ByteUtils.toHexString(decryptData));

            }else{
                Log.d(TAG, "内部认证-->认证不通过");
                Toast.makeText(NFCHuadaTestActivity.this,"内部认证-不是您的卡片",Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (null != isoDep){
                try {
                    isoDep.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    String getFront8(String mac){
        return mac.substring(0,8);
    }

    String getRandom8ByApDu(IsoDep isoDep) throws IOException {
        // APDU=0084000008 取随机数
        String apDu4 = "0084000008";
        byte[] command4 = ByteUtils.fromHexString(apDu4);
        byte[] response4 = isoDep.transceive(command4);
        String random1 = ByteUtils.toHexString(response4);
        random1 = random1.substring(0,16);
        Log.d(TAG, "取随机数发送指令-->command:" + apDu4 +", 指令响应response:" + random1 + ",response4.length="+ response4.length);
        return random1;
    }

    //字节数组转换十六进制
    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F" };
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }
}
