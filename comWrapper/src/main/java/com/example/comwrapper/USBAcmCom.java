package com.example.comwrapper;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;


import com.example.comwrapper.activityAndframe.UsbPermissionCallBack;
import com.example.comwrapper.tool.ByteArrayConverter;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;


public class USBAcmCom extends SerialDeviceWrapper implements SerialInputOutputManager.Listener{

    private UsbManager manager;
    private UsbSerialPort port;
    private SerialInputOutputManager usbIoManager;
    private UsbPermissionCallBack callBack;


    public USBAcmCom(UsbManager manager, byte[] buffer, int baudRate) throws IOException {
        this.manager = manager;
        this.baudRate = baudRate;
        setBuffer(buffer);
        connect();
    }

    public USBAcmCom(UsbManager manager, byte[] buffer, int baudRate, UsbPermissionCallBack callBack) throws IOException {
        this.manager = manager;
        this.baudRate = baudRate;
        this.callBack = callBack;
        setBuffer(buffer);
        connect();
    }

    public USBAcmCom(UsbManager manager, RingBuffer buffer, int baudRate) throws IOException {
        this.manager = manager;
        this.baudRate = baudRate;
        setBuffer(buffer);
        connect();
    }

    public USBAcmCom(UsbManager manager, RingBuffer buffer, int baudRate, UsbPermissionCallBack callBack) throws IOException {
        this.manager = manager;
        this.baudRate = baudRate;
        this.callBack = callBack;
        setBuffer(buffer);
        connect();
    }


    private void connect() throws IOException{

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty())
            throw new IOException();

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);

        if(!manager.hasPermission(driver.getDevice())){
            if(callBack != null)
                callBack.getUsbPermissionCallBack(manager, driver.getDevice());
            throw new IOException();
        }

        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null)
            throw new IOException();

        port = driver.getPorts().get(0); // Most devices have just one port (port 0)

        port.open(connection);
        port.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        usbIoManager = new SerialInputOutputManager(port, this);
        Executors.newSingleThreadExecutor().submit(usbIoManager);

    }

    /*this function is implement Serial device wrapper override */
    @Override
    public void write(final byte[] src, final int timeout) throws IOException{
        port.write(src, timeout);
    }
    /*this function is implement Serial device wrapper override */
    @Override
    public void close() throws IOException{
        port.close();
    }

    /*this function is implement USB com event override */
    @Override
    public void onNewData(byte[] data) {
        if(useRingBufferFlag == USE_NORMAL_BUFFER) {
            for (int i = 0; i < data.length; i++) {
                if (currentReceive < buffer.length) {
                    buffer[currentReceive] = data[i];
                    currentReceive += 1;
                }
            }

            Log.d("receive_debug", "(ap)onDataReceived:" + ByteArrayConverter.ByteArrayToStringHex(data, data.length));
        }
        else if(useRingBufferFlag == USE_RING_BUFFER){
            for(int i = 0; i< data.length; i++){
                try {
                    ringBuffer.write(data[i]);
                }
                catch (RingBuffer.BufferFullException e) {

                 }
            }
        }
    }

    /*this function is implement USB com event override */
    @Override
    public void onRunError(Exception e) {

    }

}



