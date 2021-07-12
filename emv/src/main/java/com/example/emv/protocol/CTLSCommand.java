package com.example.emv.protocol;

import java.io.IOException;
import java.util.Arrays;

public class CTLSCommand extends BasicCommand{


    private final byte CHNL_CTLS = 0x21;
    private final byte CMD_CTLS_SET_EMV_TAG_VALUE = 0x01;
    private final byte CMD_CTLS_READY_FOR_SALE = 0x02;
    private final byte CMD_CTLS_INIT_KERNEL = 0x03;



    public byte[] setEMVTagValuePacket(byte[] data){
        if(data == null)
            throw new IllegalArgumentException();
        return createPacket(CHNL_CTLS, CMD_CTLS_SET_EMV_TAG_VALUE, data);
    }

    public byte[] readyForSalePacket(byte[] amount){
        if(amount == null)
            throw new IllegalArgumentException();
        if(amount.length != 6)
            throw new IllegalArgumentException();
        return createPacket(CHNL_CTLS, CMD_CTLS_READY_FOR_SALE, amount);
    }

    public byte[] initKernel(){
        return createPacket(CHNL_CTLS, CMD_CTLS_INIT_KERNEL, new byte[0]);
    }

    public CtlsData parseCtlsData(byte[] data) throws IOException {

        int cursor = 0;
        byte tag;
        byte len_tag;
        if(data == null || data.length == 0)
            throw new IOException();

        CtlsData ctlsData = new CtlsData();
        ctlsData.setResponseCode(data[cursor]);
        cursor += 1;
        if(data.length > 1){

            ctlsData.setSchemeID(data[cursor]);
            cursor += 1;
            ctlsData.setDataTime(Arrays.copyOfRange(data,cursor, cursor + 7));
            cursor += 7;

            while (data[cursor] == (byte) 0xD1 || data[cursor] ==  (byte) 0xD2 || data[cursor] == (byte) 0xD3){
                tag = data[cursor];
                cursor += 1;
                int dataLen = 0;
                if(tag == (byte) 0xD3){
                    len_tag = data[cursor];
                    cursor += 1;
                    if(len_tag == (byte) 0x81){
                        dataLen = convertByteTOInt(data[cursor]);
                        cursor += 1;
                    }
                    else{
                        dataLen = convertByteTOInt(data[cursor]) * 256 + convertByteTOInt(data[cursor+1]);
                        cursor += 2;
                    }
                }
                else {
                    dataLen = convertByteTOInt(data[cursor]);
                    cursor += 1;
                }

                byte[] tempData = Arrays.copyOfRange(data,cursor, cursor + dataLen);
                if(tag == (byte) 0xD1)
                    ctlsData.setTrack1Data(tempData);
                else if(tag == (byte)0xD2)
                    ctlsData.setTrack2Data(tempData);
                else
                    ctlsData.setChipData(tempData);

                cursor += dataLen;
            }

        }

        return ctlsData;
    }

}
