package com.populstay.safnect.key;

import android.content.Context;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.populstay.safnect.key.bean.KeyInfoBean;

import java.util.ArrayList;
import java.util.List;

public class KeyUtil {

    // 私钥分片数量
    private static final int KEY_SHAR_COUNT = 3;
    // 恢复私钥所需最小分片数量
    public static final int THRESHOLD = 2;

    public static final String TAG = "KeyUtil";

    private static final String KEY_PYTHON_FILE = "key";

    public static KeyInfoBean sharesAndPublicKey(Context context) throws Exception {
        if (null == context){
            throw new Exception("context must not null");
        }
        try {
            if(!Python.isStarted()){
                Python.start(new AndroidPlatform(context.getApplicationContext()));
            }
            Python python = Python.getInstance();
            PyObject pythonFunction = python.getModule(KEY_PYTHON_FILE).callAttr("generate_shares_and_public_key", KEY_SHAR_COUNT, THRESHOLD,true);
            // 从Python元组中提取返回值
            List<PyObject> resultTuple = pythonFunction.asList();
            List<PyObject> sharePyObjList = resultTuple.get(0).asList();
            List<String> shares = new ArrayList<>();
            for (int i = 0; i < sharePyObjList.size(); i++) {
                shares.add((i + 1)+","+sharePyObjList.get(i).toString());
            }
            List<PyObject> publicKeyPyObjList = resultTuple.get(1).asList();
            List<String> publicKeys = new ArrayList<>();
            for (int i = 0; i < publicKeyPyObjList.size(); i++) {
                publicKeys.add(publicKeyPyObjList.get(i).toString());
            }
            String secret = resultTuple.get(2).toString();

            Log.d(TAG,"shares = " + shares + "\n publicKeys = " + publicKeys + "\n secret = " + secret);
            return new KeyInfoBean(shares, publicKeys, secret);
        } catch (Exception e) {
            e.printStackTrace();
         }
        return null;
    }

    public static String restoreKey(Context context,List<String> shares) throws Exception {
        if (null == context){
            throw new Exception("context must not null");
        }
        if (null == shares || shares.isEmpty()){
            throw new Exception("shares must not null or empty");
        }
        if (shares.size() < THRESHOLD){
            throw new Exception("The sharding required for private key recovery must be greater than " + THRESHOLD);
        }
        try {
            if(!Python.isStarted()){
                Python.start(new AndroidPlatform(context.getApplicationContext()));
            }
            Python python = Python.getInstance();
            Log.d(TAG,"restoreKey shares = " + shares);
            List<String> keyShares = new ArrayList<>();
            List<Integer> keyShareIndexs = new ArrayList<>();
            for (int i = 0; i < shares.size(); i++) {
                String [] jjj =  shares.get(i).split(",");
                keyShares.add(jjj[1]);
                keyShareIndexs.add(Integer.valueOf(jjj[0]));
            }


            PyObject pythonFunction2 = python.getModule(KEY_PYTHON_FILE).callAttr("restore",  keyShares.toArray(),keyShareIndexs.toArray(),THRESHOLD);
            String restore_key = pythonFunction2.toString();
            Log.d(TAG,"restoreKey = " + restore_key);
            return restore_key;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
