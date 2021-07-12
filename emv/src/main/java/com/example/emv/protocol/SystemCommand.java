package com.example.emv.protocol;

import android.util.Log;

import com.example.emv.tool.ByteArrayConverter;

public class SystemCommand extends BasicCommand {

    private static final byte CHNL_SYSTEM = 0x06;
    private static final byte CMD_SYS_GET_DATE_TIME = (byte)0xC6;
    private static final byte CMD_SYS_SET_DATE_TIME = (byte)0xC7;

    public byte[] getDateTimePacket() {
        return createPacket(CHNL_SYSTEM, CMD_SYS_GET_DATE_TIME, new byte[0]);
    }

    public byte[] setDateTimePacket(byte[] data) {
        Log.d("set RTC packet", ByteArrayConverter.ByteArrayToStringHex(createPacket(CHNL_SYSTEM, CMD_SYS_SET_DATE_TIME, data), 10 + 7));
        return createPacket(CHNL_SYSTEM, CMD_SYS_SET_DATE_TIME, data);
    }
}
