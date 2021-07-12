package com.example.emvlibiarytesing;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.comwrapper.SerialDeviceWrapper;
import com.example.comwrapper.USBAcmCom;
import com.example.comwrapper.activityAndframe.UsbPermissionCallBack;
import com.example.emv.EMVController;
import com.example.emv.EMVKeyboardCallBack;
import com.example.emv.com.SerialPortWrapper;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements ActivityGetControllerCallBack, UsbPermissionCallBack, ActivityGetOnlinePortCallBack,
        ActivityPinBlockCallBack {



    @Override
    public EMVController getController() {
        return emvController;
    }

    private EMVController emvController;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter filter;
    private String INTENT_ACTION_GRANT_USB;
    private SerialDeviceWrapper pcCom;
//    private int baudRate = 115200;
    private int baudRate = 9600;
    private byte[] pcBuffer;
    private PinBlockCallBack pinBlockCallBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        try {
            emvController = new EMVController();

        } catch (IOException e) {
            e.printStackTrace();
        }
        filter = new IntentFilter();
        //filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getApplicationContext(), intent.getAction(), Toast.LENGTH_SHORT).show();
                if (intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                    try {
                        connectPCCom(baudRate);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
                    try {
                        disconnectPCCom();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        pcBuffer = new byte[1024];
    }



    public void connectPCCom(int baudRate) throws IOException{

        if(pcCom != null) {
            if (pcCom.getBaudRate() == baudRate)
                return;
            pcCom.close();
        }

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        pcCom = new USBAcmCom(manager, pcBuffer, baudRate, this);

    }


    public void disconnectPCCom() throws IOException {
        if(pcCom == null)
            return;
        pcCom.close();
        pcCom = null;
    }


    public void getUsbPermissionCallBack(UsbManager manager, UsbDevice device) {
        PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
        manager.requestPermission(device, usbPermissionIntent);
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(broadcastReceiver, filter);
        if(emvController != null){
            try {
                emvController.connected();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            connectPCCom(baudRate);
            Toast.makeText(getApplicationContext(), BuildConfig.APPLICATION_ID+" pc port connect success", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e){
            Toast.makeText(getApplicationContext(), BuildConfig.APPLICATION_ID+" pc port connect failed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStop(){
        super.onStop();
        unregisterReceiver(broadcastReceiver);
        if(emvController != null) {
            try {
                emvController.disconnected();
                emvController.closeKeyBoard();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(pcCom != null) {
            try {
                disconnectPCCom();
                Toast.makeText(getApplicationContext(), BuildConfig.APPLICATION_ID + " pc port disconnect success", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), BuildConfig.APPLICATION_ID + " pc port disconnect failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public SerialDeviceWrapper getOnlinePort() {
        return pcCom;
    }

    @Override
    public void setPinBlockCallBack(PinBlockCallBack callBack) {
        pinBlockCallBack = callBack;
    }

    @Override
    public void runPinBlockCallBack(byte[] data) {
        if(pinBlockCallBack != null)
            pinBlockCallBack.onGetPinBlock(data);
    }
}
