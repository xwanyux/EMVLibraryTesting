package com.example.comwrapper.activityAndframe;

import com.example.comwrapper.SerialDeviceWrapper;

import java.io.IOException;

public interface ActivityPCComCallBack {

    SerialDeviceWrapper getPCCom();
    int  getPCBaudRate();
    boolean PComIsConnected();
    void connectPCCom(int baudRate) throws IOException;
    void disconnectPCCom() throws IOException;
}
