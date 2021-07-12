package com.example.emvlibiarytesing.ui.rtc;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.emv.EMVController;
import com.example.emvlibiarytesing.tool.BCDConverter;
import com.example.emvlibiarytesing.tool.ByteArrayConverter;

import java.io.IOException;

public class RtcViewModel extends ViewModel {

    private EMVController emvController;

    /**
     * function for fragment to provide emvController
     * @param controller
     */
    public void setEmvController(EMVController controller) {
        emvController = controller;
    }

    /**
     * To read the current date and time from SP RTC device
     * @return
     * @throws IOException
     */
    public String getRtcTime() throws IOException {
        if(emvController == null)
            throw new IOException();

        //RTC time in BCD format: YY YY MM DD hh mm ss
        byte[] bcdDateTime = emvController.getRtcTime();
        String dateTimeStr = ByteArrayConverter.ByteArrayToStringHex(bcdDateTime, 7).replace(" ", "");

        StringBuilder stringBuilder = new StringBuilder(dateTimeStr);
        stringBuilder.insert(4, "/");
        stringBuilder.insert(7, "/");
        stringBuilder.insert(10, " ");
        stringBuilder.insert(13, ":");
        stringBuilder.insert(16, ":");
        dateTimeStr = stringBuilder.toString();

        return dateTimeStr;
    }

    public void setRtcTime(String rtcTime) throws IOException {
        if(emvController == null)
            throw new IOException();

        Log.d("RTC String", rtcTime);

        byte[] bcdDateTime = new byte[7];
        byte[] ascDateTime = rtcTime.getBytes();

        //Convert ASCII RTC time to BCD RTC time
        for(int i = 0, j = 0 ; i < 14 ; i += 2, j++) {
            bcdDateTime[j] = (byte)(((ascDateTime[i] & 0x0F) << 4) | ((ascDateTime[i+1] & 0x0F)));
        }
        Log.d("BCD String", ByteArrayConverter.ByteArrayToStringHex(bcdDateTime, 7));

        emvController.setRtcTime(bcdDateTime);
    }
}
