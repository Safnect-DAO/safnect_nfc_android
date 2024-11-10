package com.populstay.safnect.key;

import android.content.Context;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.populstay.safnect.key.bean.KeyInfoBean;

import java.util.ArrayList;
import java.util.List;

public class KeyGenerator {

    // 私钥分片数量
    private static final int KEY_SHAR_COUNT = 3;
    // 恢复私钥所需最小分片数量
    public static final int THRESHOLD = 2;

    public static final String TAG = "KeyGenerator";

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
            List<Integer> keyShareIndexs = new ArrayList<>();
            List<String> keyShares = new ArrayList<>();
            for (int i = 0; i < shares.size(); i++) {
                String [] keyShareArr =  shares.get(i).split(",");
                keyShareIndexs.add(Integer.valueOf(keyShareArr[0]));
                keyShares.add(keyShareArr[1]);
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

    public static String restoreShare(Context context,List<String> shares) throws Exception {
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
            Log.d(TAG,"restoreShare shares = " + shares);
            List<Integer> keyShareIndexs = new ArrayList<>();
            List<String> keyShares = new ArrayList<>();
            for (int i = 0; i < shares.size(); i++) {
                String [] keyShareArr =  shares.get(i).split(",");
                keyShareIndexs.add(Integer.valueOf(keyShareArr[0]));
                keyShares.add(keyShareArr[1]);
            }

            PyObject pythonFunction2 = python.getModule(KEY_PYTHON_FILE).callAttr("restoreShare",keyShares.toArray(),keyShareIndexs.toArray(),THRESHOLD);
            String restore_share = pythonFunction2.toString();
            Log.d(TAG,"restore_share = " + restore_share);
            return restore_share;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static KeyInfoBean restoreShareAndPublicKey(Context context,List<String> shares) throws Exception {
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
            Log.d(TAG,"restoreShare shares = " + shares);
            List<Integer> keyShareIndexs = new ArrayList<>();
            List<String> keyShares = new ArrayList<>();
            for (int i = 0; i < shares.size(); i++) {
                String [] keyShareArr =  shares.get(i).split(",");
                keyShareIndexs.add(Integer.valueOf(keyShareArr[0]));
                keyShares.add(keyShareArr[1]);
            }

            PyObject pythonFunction2 = python.getModule(KEY_PYTHON_FILE).callAttr("restoreShareAndPublicKey",keyShares.toArray(),keyShareIndexs.toArray(),THRESHOLD);

            // 从Python元组中提取返回值
            List<PyObject> resultTuple = pythonFunction2.asList();
            String restore_share = resultTuple.get(0).toString();
            List<String> restoreShares = new ArrayList<>();
            restoreShares.add(restore_share);

            List<PyObject> publicKeyPyObjList = resultTuple.get(1).asList();
            List<String> publicKeys = new ArrayList<>();
            for (int i = 0; i < publicKeyPyObjList.size(); i++) {
                publicKeys.add(publicKeyPyObjList.get(i).toString());
            }

            Log.d(TAG,"restoreShares = " + restoreShares + "\n publicKeys = " + publicKeys);
            return new KeyInfoBean(restoreShares, publicKeys,null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
