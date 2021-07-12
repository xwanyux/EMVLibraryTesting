package com.example.emv.tool;

import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;

public class RSAKeyStore implements RSAThread.RSACallBack {

    /* this is tend to decrease the latency of the rsa generation (just have a thread keep generate 3 rsa key pair )*/
    /* idea is simple, but where to put it  in EMV controller??*/
    /* not implement the locking scheme for count (need to solve)*/

    KeyPair[] keyPairStore;
    int count;
    int max;
    MonitorRSAThread thread;
    RSAThread rsaThread;

    private class MonitorRSAThread extends Thread{

        @Override
        public void run(){

            while(!isInterrupted()){
                if(count <= max){
                    if(rsaThread.isFinish() && !isInterrupted()){
                        rsaThread = new RSAThread(RSAKeyStore.this);
                        rsaThread.start();
                    }


                }else{
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public RSAKeyStore(int numKey){

        count = 0;
        keyPairStore = new KeyPair[numKey];
        max = numKey;
        thread = new MonitorRSAThread();
        thread.start();
        rsaThread = new RSAThread(RSAKeyStore.this);
        if(numKey != 0)
            rsaThread.start();
    }


    public void stop(){
        thread.interrupt();
        rsaThread.interrupt();
    }


    synchronized public KeyPair getKeyPair() throws IOException {
        if(count == 0)
            throw new IOException("no rsa key");

        KeyPair keyPair = keyPairStore[count - 1];
        count -= 1;
        return keyPair;
    }


    @Override
    public void onFailed() {

    }

    @Override
    synchronized public void onGetKeyPair(KeyPair keyPair) {
        keyPairStore[count] = keyPair;
                count += 1;

    }
}
