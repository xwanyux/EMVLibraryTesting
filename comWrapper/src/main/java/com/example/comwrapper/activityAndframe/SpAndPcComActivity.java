package com.example.comwrapper.activityAndframe;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.comwrapper.SerialDeviceWrapper;
import com.example.comwrapper.SerialPortWrapper;
import com.example.comwrapper.USBAcmCom;

import java.io.IOException;

public abstract class SpAndPcComActivity  extends AppCompatActivity implements ActivitySPComCallBack,ActivityPCComCallBack,UsbPermissionCallBack{



    private SerialDeviceWrapper spCom;
    private SerialDeviceWrapper pcCom;
    private byte[] spBuffer;
    private byte[] pcBuffer;

    private BroadcastReceiver broadcastReceiver;
    private IntentFilter filter;
    private String INTENT_ACTION_GRANT_USB;

    protected abstract String getActivityName();

    protected abstract byte[] setSPComInitBuffer();
    protected abstract int getSPDefaultBaudRate();

    protected abstract byte[] setPCComInitBuffer();
    protected abstract int getPCDefaultBaudRate();

    protected abstract void onSPComCreate(); // this is use as a call back for displaying UI
    protected abstract void onSPComDisconnect(); // this is use as a call back for displaying UI

    protected abstract void onPCComCreate(); // this is use as a call back for displaying UI
    protected abstract void onPCComDisconnect(); // this is use as a call back for displaying UI

    protected abstract String getAppName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spBuffer = setSPComInitBuffer();
        pcBuffer = setPCComInitBuffer();

        filter = new IntentFilter();
        //filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        INTENT_ACTION_GRANT_USB = getAppName()+".GRANT_USB";

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getApplicationContext(), intent.getAction(), Toast.LENGTH_SHORT).show();
                if(intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                    try {
                        connectPCCom(getPCBaudRate());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_DETACHED")){
                    try {
                        disconnectPCCom();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }


    public void getUsbPermissionCallBack(UsbManager manager, UsbDevice device) {
        PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
        manager.requestPermission(device, usbPermissionIntent);
    }



    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(broadcastReceiver, filter);
        try {
            connectSPCom(getSPDefaultBaudRate());
            Toast.makeText(getApplicationContext(), getActivityName()+" sp port connect success", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e){
            Toast.makeText(getApplicationContext(), getActivityName()+" sp port connect failed", Toast.LENGTH_SHORT).show();
        }

        try {
            connectPCCom(getPCDefaultBaudRate());
            Toast.makeText(getApplicationContext(), getActivityName()+" pc port connect success", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e){
            Toast.makeText(getApplicationContext(), getActivityName()+" pc port connect failed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        try{
            disconnectSPCom();
            Toast.makeText(getApplicationContext(), getActivityName()+" sp port disconnect success", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e){
            Toast.makeText(getApplicationContext(), getActivityName()+" sp port disconnect failed", Toast.LENGTH_SHORT).show();
        }

        try{
            disconnectPCCom();
            Toast.makeText(getApplicationContext(), getActivityName()+" pc port disconnect success", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e){
            Toast.makeText(getApplicationContext(), getActivityName()+" pc port disconnect failed", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public SerialDeviceWrapper getPCCom() {
        return pcCom;
    }

    @Override
    public int getPCBaudRate() {
        if(pcCom == null)
            return 0;
        return pcCom.getBaudRate();
    }

    @Override
    public boolean PComIsConnected() {
        if(pcCom == null)
            return false;
        return true;
    }

    @Override
    public void connectPCCom(int baudRate) throws IOException{

        if(pcCom != null) {
            if (pcCom.getBaudRate() == baudRate)
                return;
            pcCom.close();
        }

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        pcCom = new USBAcmCom(manager, pcBuffer, baudRate, this);
        onPCComCreate();

    }

    @Override
    public void disconnectPCCom() throws IOException {

        if(pcCom == null)
            return;
        pcCom.close();
        pcCom = null;
        onPCComDisconnect();
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
