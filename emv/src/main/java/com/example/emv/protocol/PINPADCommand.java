package com.example.emv.protocol;

import java.security.InvalidParameterException;

public class PINPADCommand  extends BasicCommand{


    private static final byte CHNL_PINPAD = 0x25;
    private static final byte CMD_PINPAD_GET_PERMUTATION = 0x01;
    private static final byte CMD_PINPIAD_WRITE_PERMUTED_PIN = 0x02;
    private static final byte CMD_PINPAD_GET_ENCRYPTED_PIN_BLOCK = 0x03;
    private static final byte CMD_PINPAD_SET_KEY_INDEX_AND_PIN_BLOCK_ISO_FORMAT = 0x04;
    private static final byte CMD_PINPIAD_GET_PIN_MODE = 0x05;
    private static final byte CMD_PINPAD_GET_MAC_BY_KEY = 0x06;

    private static final byte EPB_ISO0 = 0x00;
    private static final byte EPB_ISO1 = 0x01;
    private static final byte EPB_ISO2 = 0x02;
    private static final byte EPB_ISO3 = 0x03;
    private static final byte EPB_ISO4 = 0x04;





    public byte[] getPermutationPacket(byte[] modulus, byte[] exponent){

        byte[] data = new byte[4 + modulus.length + exponent.length];
        data[0] = (byte) ((modulus.length & 0xFF00) >> 8);
        data[1] = (byte) (modulus.length & 0x00FF);
        System.arraycopy(modulus, 0, data, 2, modulus.length);
        data[ 2 + modulus.length] = (byte) ((exponent.length & 0xFF00) >> 8);
        data[ 3 + modulus.length] = (byte) (exponent.length & 0x00FF);
        System.arraycopy(exponent, 0, data, 4 + modulus.length, exponent.length);

        return createPacket(CHNL_PINPAD,  CMD_PINPAD_GET_PERMUTATION, data);
    }

    public byte[] writePermutedPinPacket(byte[] permutedPin){
        if(permutedPin.length != 20)
            throw new InvalidParameterException();
        return createPacket(CHNL_PINPAD, CMD_PINPIAD_WRITE_PERMUTED_PIN, permutedPin);
    }

    public byte[] getEncryptedPinBlockPacket(){
        return createPacket(CHNL_PINPAD, CMD_PINPAD_GET_ENCRYPTED_PIN_BLOCK, new byte[0]);
    }

    public byte[] setKeyIndexAndPinBlockIsoFormat(byte keyIndex, byte ISOFormat){

        if(ISOFormat < EPB_ISO0 || ISOFormat > EPB_ISO4)
            throw new IllegalArgumentException();
        byte[] data = new byte[2];
        data[0] = keyIndex;
        data[1] = ISOFormat;

        return createPacket(CHNL_PINPAD, CMD_PINPAD_SET_KEY_INDEX_AND_PIN_BLOCK_ISO_FORMAT, data);
    }

    public byte[] getPinMode(){
        return createPacket(CHNL_PINPAD, CMD_PINPIAD_GET_PIN_MODE, new byte[0]);
    }

    public byte[] getMacByCurrentKeyMode(byte macMode, byte keyIndex, byte[] initialVector, byte[] data){

        if(initialVector == null || data == null)
            throw new IllegalArgumentException();
        if(initialVector.length != 8)
            throw new IllegalArgumentException();

        if(2 + 8 + 2 + data.length > 65535)
            throw new IllegalArgumentException();

        byte[] txData = new byte[2 + 8 + 2 + data.length];
        txData[0] = macMode;
        txData[1] = keyIndex;
        System.arraycopy(initialVector, 0, txData, 2, initialVector.length);
        txData[10] = (byte) ((data.length & 0xFF00) >> 8);
        txData[11] = (byte) (data.length & 0x00FF);
        System.arraycopy(data, 0, txData, 12, data.length);

        return createPacket(CHNL_PINPAD,  CMD_PINPAD_GET_MAC_BY_KEY, txData);
    }



}
