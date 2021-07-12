package com.example.comwrapper.activityAndframe;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public interface UsbPermissionCallBack {


    void getUsbPermissionCallBack(UsbManager manager, UsbDevice device);

}
