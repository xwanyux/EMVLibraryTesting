package com.example.emv.protocol;

import java.util.Arrays;

public class BasicCommand {

    final int channelIdx = 3;
    final int statusIdx = 4;
    final int lengthIdx = 5; // with 2 byte
    final int dataIdx = 7;
    // I think I only use three of these
    final byte RC_SUCCESS = 0x00;
    final byte RC_INVALID_INPUT = 0x01;
    final byte RC_FAILURE = (byte) 0xff;
    final byte MAGIC_OUT_OF_INDEX = 0x50; // use in some error code

    protected int convertByteTOInt(byte b){
        return (b >= 0) ? b: 256 + b;
    }

    protected int crc(int crc, int ch){
        ch <<= 8;
        for(int i = 8; i>=1; i--){
            if (((ch ^ crc) & 0x8000) > 0){
                crc = (crc << 1) ^ 0x8005;}
            else
                crc <<= 1;
            ch <<= 1;
        }
        return crc & 0xffff;
    }

    protected byte[] createPacket(byte channel, byte command, byte[] data){

        int crc16 = 0;
        byte[] pack_data = new byte[data.length + 4 + 2 + 1 + 3];

        pack_data[0] = 0x02; // start flag
        pack_data[1] = 0x47;  // appID
        pack_data[2] = 0x53; //  appID
        pack_data[3] = channel;
        pack_data[4] = command;
        pack_data[5] = (byte) ((data.length & 0xFF00) >> 8);
        pack_data[6] = (byte) (data.length & 0x00FF);
        for(int i = 0; i < data.length; i++)
            pack_data[7+i] = data[i];

        for(int i = 1; i < 1+6+data.length; i++) {
            // dealing with negative number of byte
            crc16 =  crc (crc16, convertByteTOInt(pack_data[i]));
        }

        pack_data[pack_data.length - 3] = (byte)((crc16 & 0xFF00) >> 8);
        pack_data[pack_data.length - 2] = (byte)(crc16 &0x00FF);
        pack_data[pack_data.length - 1] = 0x03;

        return pack_data;
    }

    public boolean isValid(byte[] receivePacket){
        int crc16 = 0;
        // start byte
        if( (receivePacket[0] != 0x02) && (receivePacket[receivePacket.length - 1] != 0x03))
            return false;

        for(int i = 1; i < receivePacket.length - 1; i++)
            crc16 =  crc (crc16, convertByteTOInt(receivePacket[i]));
        if(crc16 != 0)
            return false;
        return true;
    }

    // check with length
    public boolean isValid(byte[] receivePacket, int length){
        int crc16 = 0;

        if(length == 0)
            return false;

        if( (receivePacket[0] != 0x02) || (receivePacket[length - 1] != 0x03))
            return false;

        for(int i = 1; i < length - 1; i++)
            crc16 =  crc (crc16, convertByteTOInt(receivePacket[i]));
        if(crc16 != 0)
            return false;
        return true;
    }

    public boolean isSuccess(byte[] receivePacket){
        return RC_SUCCESS == getStatus(receivePacket);
    }

    public boolean isFailure(byte[] receivePacket){
        return RC_FAILURE == getStatus(receivePacket);
    }

    public boolean isInvalidInput(byte[] receivePacket){
        return RC_INVALID_INPUT == getStatus(receivePacket);
    }

    public byte getChannel(byte[] receivePacket){
        return  receivePacket[channelIdx];
    }

    public byte getStatus(byte[] receivePacket){
        if(receivePacket.length > statusIdx) {
            //Log.d("receive","status:" + (""+receivePacket[statusIdx]));
            return receivePacket[statusIdx];
        }
        //Log.d("receive","failed");
        return MAGIC_OUT_OF_INDEX; // all other thing should failed
    }


    public int getLength(byte[] receivePacket){
            int high = convertByteTOInt(receivePacket[lengthIdx]);
            int low = convertByteTOInt(receivePacket[lengthIdx + 1]);
            return  256 * high + low;
    }

    public byte[] getData(byte[] receivePacket){
        if(receivePacket.length == 0)
            return new byte[0];


        //Log.d("receive_debug", "getData: "+ ByteArrayConverter.ByteArrayToStringHex(receivePacket, receivePacket.length));
        //Log.d("receive_debug", "getData:(data len) "+ (""+getLength(receivePacket)));

        return Arrays.copyOfRange(receivePacket, dataIdx, dataIdx + getLength(receivePacket));
    }

    public boolean isValidAuxDLLPacket(byte[] receivePacket) {
        byte lrc = 0;

        if(receivePacket.length == 0)
            return false;

        if(receivePacket[0] != 0x01)
            return false;

        for(int i = 1 ; i < (receivePacket.length - 2) ; i++) {
            lrc = (byte)(lrc ^ receivePacket[i]);
        }

        if(lrc == receivePacket[receivePacket.length - 1])
            return true;

        return false;
    }

    public byte[] getAuxDLLMsg(byte[] receivePacket) {
        return Arrays.copyOfRange(receivePacket, 2, receivePacket.length-1);
    }
}
