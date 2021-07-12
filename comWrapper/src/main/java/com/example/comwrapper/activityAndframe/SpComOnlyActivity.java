package com.example.comwrapper.activityAndframe;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.comwrapper.SerialDeviceWrapper;
import com.example.comwrapper.SerialPortWrapper;
import com.example.comwrapper.USBAcmCom;

import java.io.IOException;




/**
 *
 *      this is a Activity will set com port automated when you onResurme the class
 *      and also stop when you call onPause
 *
 *      this also provide any fragment (by ActivitySpComCallBack)
 *      to use the com port to do their job.
 */


abstract public class SpComOnlyActivity extends AppCompatActivity implements ActivitySPComCallBack {


    private SerialDeviceWrapper spCom;
    protected byte[] spBuffer;


    protected abstract byte[] setSPComInitBuffer();
    protected abstract String getActivityName();
    protected abstract int getDefaultBaudRate();
    protected abstract void onSPComCreate(); // this is use as a call back for displaying UI
    protected abstract void onSPComDisconnect(); // this is use as a call back for displaying UI


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spBuffer = setSPComInitBuffer();
    }







    @Override
    public void onResume(){
        super.onResume();
        try {
            connectSPCom(getDefaultBaudRate());
            Toast.makeText(getApplicationContext(), getActivityName()+" sp port connect success", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e){
            Toast.makeText(getApplicationContext(), getActivityName()+" sp port connect failed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onPause(){
        super.onPause();
        try{
            disconnectSPCom();
            Toast.makeText(getApplicationContext(), getActivityName()+" sp port disconnect success", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e){
            Toast.makeText(getApplicationContext(), getActivityName()+" sp port disconnect failed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public SerialDeviceWrapper getSPCom() {
        return spCom;
    }

    @Override
    public int getSPBaudRate() {
        if(spCom == null)
            return 0;
        return  spCom.getBaudRate();
    }

    @Override
    public boolean SPComIsConnected() {
        if(spCom != null)
            return true;
        return false;
    }



    @Override
    public void connectSPCom(int baudRate) throws IOException{
        if(spCom != null) {
            if (spCom.getBaudRate() == baudRate)
                return;
            spCom.close();
        }
        spCom = new SerialPortWrapper(spBuffer, baudRate);
        onSPComCreate();
    }

    @Override
    public void disconnectSPCom() throws IOException{
        if(spCom == null)
            return;
        spCom.close();
        spCom = null;
        onSPComDisconnect();
    }
}
