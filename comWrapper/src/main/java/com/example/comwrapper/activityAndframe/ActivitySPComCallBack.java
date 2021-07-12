package com.example.comwrapper.activityAndframe;

import com.example.comwrapper.SerialDeviceWrapper;

import java.io.IOException;

public interface ActivitySPComCallBack {

    SerialDeviceWrapper getSPCom();
    int  getSPBaudRate();
    boolean SPComIsConnected();
    void connectSPCom(int baudRate) throws IOException;;
    void disconnectSPCom() throws IOException;



}
