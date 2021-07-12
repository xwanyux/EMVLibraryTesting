package com.example.emv.tool;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSAThread extends Thread{


    public interface RSACallBack{

        void onFailed();

        void onGetKeyPair(KeyPair keyPair);
    }

    private RSACallBack rsaCallBack;
    private boolean finishFlag;


    public RSAThread(RSACallBack callBack){
        rsaCallBack = callBack;
        finishFlag = false;
    }

    public boolean isFinish(){
        return finishFlag;
    }



    @Override
    public void run(){
        try{
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.generateKeyPair();
            if(!isInterrupted())
                rsaCallBack.onGetKeyPair(keyPair);
            else
                rsaCallBack.onFailed();
        }catch (NoSuchAlgorithmException e){
            e.getStackTrace();
            rsaCallBack.onFailed();
        }

        finishFlag = true;
    }



}
