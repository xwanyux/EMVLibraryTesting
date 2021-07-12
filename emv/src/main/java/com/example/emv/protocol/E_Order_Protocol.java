package com.example.emv.protocol;

import com.example.emv.com.SerialDeviceWithProtocol;
import com.example.emv.com.SerialDeviceWrapper;

import java.util.Arrays;

public class E_Order_Protocol extends SerialDeviceWithProtocol {

    private BasicCommand basicCommand = new BasicCommand();


    @Override
    protected boolean isValidPacket(byte[] packet, int length) {

        return  basicCommand.isValid(packet, length);
    }

    @Override
    protected byte[] getData(byte[] packet, int length) {
        return Arrays.copyOfRange(packet, 0, length);
    }
}
