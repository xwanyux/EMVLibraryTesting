package com.example.emvlibiarytesing.tool;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.emv.EMVController;
import com.example.emv.tool.TLVProcessing;
import com.example.emvlibiarytesing.protocol.FinancialTransactionRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ConstructFinancialTransactionRequest {


    interface kernelDataHandler{

        byte[] onProcess(byte[] data);
    }


    private static boolean isAllZero(byte[] data){
        for (byte datum : data) {
            if (datum != 0)
                return false;
        }
        return true;
    }



    static class parameter{

        public FinancialTransactionRequest.E_FinancialTransactionRequestData name;
        public byte tag1;
        public byte tag2;
        public kernelDataHandler handler;

        public parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData name, byte tag1, byte tag2, kernelDataHandler handler){
            this.name = name;
            this.tag1 = tag1;
            this.tag2 = tag2;
            this.handler = handler;
        }
    }


    static private parameter[] tagTable = new parameter[]{
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.TID, (byte)0x9f, (byte)0x1c, null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.DATE, (byte)0x00, (byte)0x9a,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.TIME, (byte)0x9f,(byte)0x21,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.TYPE, (byte)0x00, (byte)0x9c,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.AMT_AUTH, (byte)0x9f, (byte)0x02,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.AMT_OTHER, (byte)0x9f, (byte)0x03,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.AMT, (byte)0xef, (byte)0x05, null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.CC, (byte)0x5f, (byte)0x2a,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.PAN, (byte)0x00, (byte)0x5a,new PANHandler()),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.PAN_SN, (byte)0x5f, (byte)0x34,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.AIP, (byte)0xef, (byte)0x04, null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.ATC, (byte)0x9f, (byte)0x36,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.TVR, (byte)0x00, (byte)0x95,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.CNTR_CODE, (byte)0x9f, (byte) 0x1a,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.UPD_NBR, (byte)0x9f, (byte) 0x37,new UnpredictedNumberHandler()),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.ISU_AP_DATA, (byte) 0x9f, (byte)0x10,new issuerApplicationDataHandler()),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.CID, (byte)0x9f, (byte)0x27,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.AC, (byte) 0x9f, (byte)0x26, null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.PEM, (byte)0x9f, (byte) 0x39,null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.LEN_REV_ISR, (byte)0xef, (byte)0x03, new issuerScriptResultLenHandler()),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.REV_ISR, (byte)0xef, (byte)0x03, new issuerScriptResultHandler()),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.LEN_EPIN, (byte)0xef, (byte)0x01, new encipherPinLengthHandler()),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.EPIN, (byte)0xef, (byte)0x01, new encipherPinHandler()),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.TSI, (byte)0x00, (byte)0x9b, null),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.LEN_KSN, (byte)0xef, (byte)0x02, new keySerialNumberLengthHandler()),
            new parameter(FinancialTransactionRequest.E_FinancialTransactionRequestData.KSN, (byte)0xef, (byte)0x02, new keySerialNumberHandler())

    };

    /**
     * TID tag          0x9f, 0x1c
     * DATE tag         0x00, 0x9a
     * TIME tag         0x9f, 0x21
     * ARQC tag         variable in previous function
     * TYPE tag         0x00, 0x9c
     * AMT_AUTH tag     0x9F, 0x02
     * AMT_OTHER tag    0x9F, 0x03
     * AMT tag          0xef, 0x05(ADDR_TERM_TX_AMT)
     * CC tag           0x5f, 0x2a
     * PAN tag          0x00, 0x5a
     * PAN_SN tag       0x5f, 0x34
     * AIP tag          0xef, 0x04 ( ADDR_ICC_AIP)
     * ATC tag          0x9f, 0x36
     * TVR tag          0x00, 0x95
     * CNTR_CODE tag    0x9f, 0x1a
     * UPD_NBR tag      0x9f, 0x37 (need to check by ADDR_TERM_UPD_NBR_LEN which is not define) (solve by check value directly from UPB_NBR)
     * ISU_AP_DATA tag  0x9f, 0x10
     * CID tag          0x9f, 0x27
     * AC tag           0x9f, 0x26
     * PEM tag          0x9f, 0x39
     * LEN_REV_ISR tag  0xef 0x03 (ADDR_TERM_ISR)
     * REV_ISR tag      0xef 0x03 (ADDR_TERM_ISR)
     * LEN_EPIN tag     0xef 0x01 (ADDR_TERM_EPIN_DATA)
     * EPIN tag         0xef 0x01 (ADDR_TERM_EPIN_DATA)
     * TSI tag          0x00, 0x9b
     * LEN_KSN tag      0xef 0x02 (ADDR_TERM_KSN)
     * KSN tag          0xef 0x02 (ADDR_TERM_KSN)
     */

    @RequiresApi(api = Build.VERSION_CODES.N)
    static public byte[] ConstructPacket(EMVController emvController, ArrayList<TLVProcessing.Tag> chipTLVList) throws IOException {

        FinancialTransactionRequest packet = new FinancialTransactionRequest();
        byte[] data = new byte[0];
        byte[] tagBuffer;
        byte[] amountAuth = new byte[6];
        byte[] amountOther = new byte[6];
        int transAmountValue;
        boolean isFound;

        for(int i = 0; i < tagTable.length; i++){
            try {
                //Get online data from chip data first
                if(chipTLVList != null) {
                    if(tagTable[i].tag1 == 0x00) {  //1 byte Tag
                        tagBuffer = new byte[]{tagTable[i].tag2};
                    }
                    else {  //2 bytes Tag
                        tagBuffer = new byte[]{tagTable[i].tag1, tagTable[i].tag2};
                    }

                    isFound = false;

                    Log.d("Get data from chip data", ByteArrayConverter.ByteArrayToStringHex(tagBuffer, tagBuffer.length));

                    //Search the corresponding tag in chip data
                    for(int j = 0 ; j < chipTLVList.size() ; j++) {
                        //Transaction Amount
                        if((tagBuffer[0] == (byte)0xEF) && (tagBuffer[1] == (byte)0x05)) {
                            transAmountValue = Integer.parseInt(BCDConverter.BCDToString(amountAuth)) +
                                               Integer.parseInt(BCDConverter.BCDToString(amountOther));
                            data = BCDConverter.convertFromString(String.valueOf(transAmountValue));
                            Log.d("Get data from chip data", "Transaction Amount = " + ByteArrayConverter.ByteArrayToStringHex(data, data.length));

                            isFound = true;
                            break;
                        }

                        Log.d("Get data from chip data", "Tag = " + ByteArrayConverter.ByteArrayToStringHex(chipTLVList.get(j).tag, chipTLVList.get(j).tag.length));

                        if(Arrays.equals(chipTLVList.get(j).tag, tagBuffer)) {
                            data = chipTLVList.get(j).value;

                            //Store amount authorized and amount other for calculating transaction amount later
                            if((tagBuffer[0] == (byte)0x9F) && (tagBuffer[1] == (byte)0x02))
                                amountAuth = chipTLVList.get(j).value;
                            else if((tagBuffer[0] == (byte)0x9F) && (tagBuffer[1] == (byte)0x03))
                                amountOther = chipTLVList.get(j).value;

                            Log.d("Get data from chip data", "data 1 = " + ByteArrayConverter.ByteArrayToStringHex(data, data.length));
                            isFound = true;
                            break;
                        }
                    }

                    //Get online data from kernel data
                    if(!isFound) {
                        data = emvController.readKernelData(tagTable[i].tag1, tagTable[i].tag2);
                        Log.d("Get data from chip data", "data 2 = " + ByteArrayConverter.ByteArrayToStringHex(data, data.length));
                    }
                }
                else    //Get online data from kernel data directly
                    data = emvController.readKernelData(tagTable[i].tag1, tagTable[i].tag2);

                if(tagTable[i].handler != null)
                    data = tagTable[i].handler.onProcess(data);
                packet.setPacket(tagTable[i].name, data);
            }
            catch (IOException e){
                Log.d("Online", "(command failed)ConstructPacket: i:" + (""+i) + ("tag name:") + tagTable[i].name);

            }catch ( IllegalArgumentException e){

                Log.d("Online", "(data failed)ConstructPacket: i:" + (""+i) + ("tag name:") + tagTable[i].name);
                Log.d("Online", "(failed data):" + ByteArrayConverter.ByteArrayToStringHex(data, data.length));
            }
        }


//        packet.setPacket(FinancialTransactionRequest.E_FinancialTransactionRequestData.ARQC, new byte[]{cryptogramType});
//        packet.setPacket(FinancialTransactionRequest.E_FinancialTransactionRequestData.TYPE, new byte[]{transactionType});

        return packet.constructPacket();
    }

    private static class PANHandler implements kernelDataHandler{

        @Override
        public byte[] onProcess(byte[] data) {

            byte[] onlineData = new byte[10];

            for(int i =0; i< onlineData.length; i++){
                if(i < data.length)
                    onlineData[i] = data[i];
                else
                    onlineData[i] = (byte) 0xff;
            }
            return onlineData;
        }
    }

    private static class UnpredictedNumberHandler implements kernelDataHandler{

        @Override
        public byte[] onProcess(byte[] data) {
            byte[] onlineData = new byte[6];

            if(isAllZero(data))
                return onlineData;

            onlineData[0] = 4;
            onlineData[1] = 0;
            System.arraycopy(data, 0 ,onlineData,2,data.length);

            return onlineData;
        }
    }


    private static class issuerApplicationDataHandler implements  kernelDataHandler{

        @Override
        public byte[] onProcess(byte[] data) {

            byte[] onlineData = new byte[34];
            if(data == null)
                return onlineData;
            if(data.length == 0)
                return onlineData;

            onlineData[0] = (byte) (data.length & 0x00ff);
            System.arraycopy(data, 0 ,onlineData,2,data.length);

            return onlineData;

        }
    }

    private static class encipherPinLengthHandler implements kernelDataHandler{

        @Override
        public byte[] onProcess(byte[] data) {
            byte[] onlineData = new byte[2];
            if(isAllZero(data) || data.length == 0)
                return onlineData;
            onlineData[0] = (byte) (data.length & 0x00ff);
            return onlineData;
        }
    }

    private static class encipherPinHandler implements kernelDataHandler{
        @Override
        public byte[] onProcess(byte[] data) {
            if(isAllZero(data) || data.length == 0)
                return null;
            return data;
        }
    }

    private static class keySerialNumberLengthHandler implements kernelDataHandler{
        @Override
        public byte[] onProcess(byte[] data){
            byte[] onlineData = new byte[2];
            if(isAllZero(data) || data.length == 0)
                return onlineData;
            onlineData[0] = (byte) (data.length & 0x00ff);
            return onlineData;

        }
    }

    private static class keySerialNumberHandler implements kernelDataHandler{

        @Override
        public byte[] onProcess(byte[] data) {
            if(isAllZero(data) || data.length == 0)
                return null;
            return data;
        }
    }

    private static class issuerScriptResultLenHandler implements kernelDataHandler{

        @Override
        public byte[] onProcess(byte[] data){
            byte[] onlineData = new byte[2];
            if(isAllZero(data) || data.length == 0)
                return onlineData;
            onlineData[0] = (byte) (data.length & 0x00ff);
            onlineData[1] = (byte) ((data.length & 0xff00) >> 8);
            return onlineData;
        }
    }

    private static class issuerScriptResultHandler implements kernelDataHandler{

        @Override
        public byte[] onProcess(byte[] data) {
            if(isAllZero(data) || data.length == 0)
                return null;
            return data;
        }

    }
}
