package com.example.emvlibiarytesing.ui.contactless;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comwrapper.SerialDeviceWrapper;
import com.example.emv.EMVCallBack;
import com.example.emv.EMVController;
import com.example.emv.EMVThread;
import com.example.emv.protocol.BasicCommand;
import com.example.emv.protocol.CtlsData;
import com.example.emv.protocol.ICCCommand;
import com.example.emv.tool.ByteArrayConverter;
import com.example.emv.tool.TLVProcessing;
import com.example.emvlibiarytesing.onPinRequireCallBack;
import com.example.emvlibiarytesing.onRequireOnlinePort;
import com.example.emvlibiarytesing.tool.BCDConverter;
import com.example.emvlibiarytesing.tool.ConstructFinancialTransactionRequest;
import com.example.emvlibiarytesing.ui.icc.IccViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class ContactLessViewModel extends ViewModel implements EMVCallBack.Basic, EMVCallBack.ContactLessCallBack {

    private EMVController emvController;
    private MutableLiveData<String> debugMessage;
    private onPinRequireCallBack callBack;
    private onRequireOnlinePort onlinePortCallBack;
    private ArrayList<TLVProcessing.Tag> chipTLVList;

    public ContactLessViewModel(){

        debugMessage = new MutableLiveData<>();

    }

    /**
     * key data structure for sending back data to fragment.
     * @return
     */
    public LiveData<String> getDebugMessage() {return debugMessage;}


    /**
     * function for fragment to provide get online port callback
     * @param callBack
     */
    public void setOnlinePortCallBack(onRequireOnlinePort  callBack){
        onlinePortCallBack = callBack;
    }
    /**
     * function for fragment to provide emvController
     * @param controller
     */
    public void setEmvController(EMVController controller){
        emvController = controller;
    }

    /**
     * function for fragment to provide call pin fragment call back
     * @param callBack
     */
    public void setRequirePinCallBack(onPinRequireCallBack callBack){
        this.callBack = callBack;
    }

    /**
     * start CTLS emv transaction
     * @param Amount
     * @throws IOException
     */
    public void startTransaction(String Amount) throws IOException {

        if (emvController == null)
            throw new IOException();
        /* setting for pin block usage key and format*/
        emvController.setISOKeyIndexAndPinBlockIsoFormat((byte)0x00, EMVController.EPB_ISO0);
        emvController.start(this, BCDConverter.convertFromString(Amount), this, null, null);
    }


    /**
     * this is implement EMVCallBack.Basic.onTimeOut()
     * we implement as send "time out!!" value back to Fragment
     */
    @Override
    public void onTimeOut() {
        debugMessage.postValue("time out!!");
    }

    /**
     * this is implement EMVCallBack.Basic.onTransactionType
     * we implement as send transaction type back to Fragment
     * @param type      0x01(ICC), 0x02(CTLS), 0x03(MSR)
     */
    @Override
    public void onTransactionType(byte type) {
        if(type == EMVThread.CTLS)
            debugMessage.postValue("type: CTLS");
        else if(type == EMVThread.ICC)
            debugMessage.postValue("type: ICC");
        else if(type == EMVThread.MSR)
            debugMessage.postValue("type: MSR");
    }

    /**
     * this is implement EMVCallBack.Basic.onFailed
     * we implement as send failed command back to Fragment
     * @param FailCommand                 e-order protocol command name
     */
    @Override
    public void onFailed(String FailCommand) {
        debugMessage.postValue("Failed Command:" + FailCommand);
    }

    /**
     * this is implement EMVCallBack.ContactLessCallBack
     * we implement as send back data back to fragment
     * @param data               parse ctls data
     */
    @Override
    public void onSuccess(CtlsData data) {
        String temp = "";
        if(data.isDateTimeExist()){
            temp += "DateTime:\n" + ByteArrayConverter.ByteArrayToStringHex(data.getDataTime(), data.getDataTime().length) + "\n";
            //debugMessage.postValue();
        }
        //Log.d("CTLS", "dateTime:" + ByteArrayConverter.ByteArrayToStringHex_withNextLine(data.getDataTime(),10, data.getDataTime().length));
        if(data.isTrack1DataExist()) {
            temp += "track1Data:\n" + ByteArrayConverter.ByteArrayToStringAscii(data.getTrack1Data(), data.getTrack1Data().length) + "\n";
            //debugMessage.postValue( );
        }
        //Log.d("CTLS","track1Data:" + ByteArrayConverter.ByteArrayToStringHex_withNextLine(data.getTrack1Data(), 10 , data.getTrack1Data().length));
        if(data.isTrack2DataExist()) {
            temp += "track2Data:\n" + ByteArrayConverter.ByteArrayToStringHex(data.getTrack2Data(), data.getTrack2Data().length) + "\n";
            //debugMessage.postValue();
        }
        //Log.d("CTLS", "track2Data:" + ByteArrayConverter.ByteArrayToStringHex_withNextLine(data.getTrack2Data(), 10 ,data.getTrack2Data().length));
        if(data.isChipDataExist()) {
            temp += "chip data:\n" + ByteArrayConverter.ByteArrayToStringHex(data.getChipData(), data.getChipData().length);
            //debugMessage.postValue();
        }
        //Log.d("CTLS", "chip data:" + ByteArrayConverter.ByteArrayToStringHex_withNextLine(data.getChipData(), 10, data.getChipData().length));
        debugMessage.postValue(temp);

        TLVProcessing tlvProcessing = new TLVProcessing();
        chipTLVList = tlvProcessing.parseTLVList(data.getChipData());

        callBack.startRequirePinProcess();
    }

    /**
     * TODO
     * You should implement the online processing here.
     * @param data               pin block data
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onGetPinBlockData(byte[] data) {
        boolean successFlag = false;
        byte[] ACKArray = new byte[]{0x06};
        byte[] NAKArray = new byte[]{0x15};
        byte[] responsePacket = new byte[3];
        int lenOfResponsePacket = 0;

        BasicCommand basicCommand = new BasicCommand();

        SerialDeviceWrapper onlineCom = onlinePortCallBack.getOnlinePort();

        try {
            //Construct packet
            byte[] packet = ConstructFinancialTransactionRequest.ConstructPacket(emvController, chipTLVList);
            Log.d("ONLINE PACKET:", "onGetPinBlockData: " + ByteArrayConverter.ByteArrayToStringHex(packet, packet.length));

            byte[] packetData = new byte[3 + packet.length];   //SOH(1) + LEN(1) + MSG(n) + LRC(1)
            packetData[0] = (byte)0x01;
            packetData[1] = (byte)packet.length;
            for(int i = 0 ; i < packet.length ; i++) {
                packetData[2 + i] = packet[i];
            }

            byte lrc = 0;
            for(int i = 1 ; i < (packetData.length - 2) ; i++) {
                lrc = (byte)(lrc ^ packetData[i]);
            }

            packetData[2 + packet.length] = lrc;

            Log.d("Packet Data", ByteArrayConverter.ByteArrayToStringHex_withNextLine(packetData, 8, packetData.length));

            if(onlineCom != null) {
                byte[] onlinePortBuffer = onlineCom.getBuffer();

                //Send packet to host
                onlineCom.write(packetData, 5000);

                long currentTime = Calendar.getInstance().getTimeInMillis();

                //Receive packet from host
                onlineCom.resetBuffer();
                while(true) {
                    //Receive at least 4 bytes => ACK(1) + SOH(1) + LEN(1) + LRC(1)
                    if(onlineCom.getCurrentReceive() >= 4) {
                        //Receive ACK
                        if(onlinePortBuffer[0] == 0x06) {
                            lenOfResponsePacket = onlinePortBuffer[2];

                            if(onlineCom.getCurrentReceive() == (4 + lenOfResponsePacket)) {
                                //Exclude ACK to parse packet
                                responsePacket = Arrays.copyOfRange(onlinePortBuffer, 1, onlineCom.getCurrentReceive());

                                if(basicCommand.isValidAuxDLLPacket(responsePacket)) {
                                    //Send ACK to host
                                    onlineCom.write(ACKArray, 1);
                                    Log.d("Receive from host", ByteArrayConverter.ByteArrayToStringHex(responsePacket, responsePacket.length));
                                    successFlag = true;
                                    break;
                                }
                            }
                        }
                    }

                    //Receiving timeout occurs after 5 seconds
                    if((Calendar.getInstance().getTimeInMillis() - currentTime) > 5000) {
                        Log.d("Receive from host", "Timeout");
                        break;
                    }
                }

                if(successFlag) {
                    /**
                     * Host should response the following data
                     * Poll or Final Flag
                     * Terminal Identifier, an8
                     * Transaction Date, n6
                     * Transaction Time, n6
                     * Authorization Response Code, an2
                     * Authorization Code, an6, 2L-V
                     * Issuer Authentication Data, b16, 2L-V
                     * Issuer Script Template, var, 2L-V
                     */
                    byte[] responseMsg = basicCommand.getAuxDLLMsg(responsePacket);
                    Log.d("Response MSG", ByteArrayConverter.ByteArrayToStringHex(responseMsg, responseMsg.length));

                    byte[] ARCArray = Arrays.copyOfRange(responseMsg, 15, 17);
                    String onlineResult = "online result:" + ByteArrayConverter.ByteArrayToStringAscii(ARCArray, ARCArray.length);
                    if((ARCArray[0] == 0x30) && (ARCArray[1] == 0x30)) {
                        debugMessage.postValue(onlineResult + "\n" + "Approved");
                    }
                    else {
                        debugMessage.postValue(onlineResult + "\n" + "Declined");
                    }
                }
                else {
                    //Send NAK to host
                    onlineCom.write(NAKArray, 1);
                }
            }


            /*
            byte[] packetData = new byte[4 + packet.length];   //SOH(1) + LEN(2) + PAYLOAD(n) + LRC(1)
            packetData[0] = (byte)0x01;
            packetData[1] = (byte)(packet.length & 0x00FF);
            packetData[2] = (byte)((packet.length & 0xFF00) >> 8);
            for(int i = 0 ; i < packet.length ; i++) {
                packetData[3 + i] = packet[i];
            }

            byte lrc = 0;
            for(int i = 1 ; i < (packetData.length - 2) ; i++) {
                lrc = (byte)(lrc ^ packetData[i]);
            }

            packetData[3 + packet.length] = lrc;
            Log.d("Packet Data", ByteArrayConverter.ByteArrayToStringHex(packetData, packetData.length));
            Log.d("Packet Data (split)", ByteArrayConverter.ByteArrayToStringHex_withNextLine(packetData, 8, packetData.length));

            //Send packet to host
            if(onlineCom != null) {
                byte[] onlinePortBuffer = onlineCom.getBuffer();
                onlineCom.write(packetData, 5000);

                onlineCom.resetBuffer();
                while(true){
                    if(onlineCom.getCurrentReceive() == 48){
                        byte response[] = new byte[] {0x06};
                        onlineCom.write(response, 1);
                        break;
                    }
                }
                debugMessage.postValue(ByteArrayConverter.ByteArrayToStringAscii(onlinePortBuffer, 48));
            }
            */
        } catch (IOException e) {
            debugMessage.postValue("Error occurred!");
        }



        /**
                // ==== [Request Data] ====
                SOH: 01
                LEN: CC 00
                Message Type: 00
                Terminal Identifier: 31 32 33 34 35 36 37 38
                Transaction Date: 21 06 24
                Transaction Time: 11 43 38
                ARQC: 80
                Transaction Type (Good and Service): 00
                Amount, Authorized: 00 00 00 00 00 01
                Amount, Other: 00 00 00 00 00 00
                Transaction Amount: 00 00 00 00 00 01
                Terminal Currency Code: 09 78
                Application PAN: 41 82 30 87 03 42 21 20 FF FF
                Application PAN Sequence Number: 00
                Application Interchange Profile (b2): 00 00
                Application Transaction Counter (b2) - ATC: 00 00
                Terminal Verification Results: 00 00 00 00 00
                Terminal Country Code: 00 56
                Unpredictable Number: 00 00 00 00 00 00
                Issuer Application Data - IAD: 00000000000000000000000000000000000000000000000000000000000000000000
                Cryptogram Information Data (b1) - CID: 00
                Application Cryptogram (b8): 00 00 00 00 00 00 00 00
                POS Entry Mode (n2): 80
                Issuer Script Results for Reversal if present (2L-V[5*n]):
                Enciphered PIN Data (RFU):
                TSI (2 bytes):
                KSN (if available):

                00000000000000000200112210004182308703422120D221
                1201145583080600000000000001080033313030393536370300E0F8
                C80100220600082200000001020000010F0031323334303030303030
                303030303002003030000000000000
                LRC: 66

                // ==== [Response Data] ====
                ACK: 06
                SOH: 01
                LEN: 2B 00
                00
                Terminal Identifier: 31 32 33 34 35 36 37 38
                Transaction Date: 00 00 00
                Transaction Time: 00 00 00
                Authorization Response Code: 30 30
                Authorization Code (2L-V): 06 00 31 32 33 34 35 36
                Issuer Authentication Data (2L-V): 00 00
                issuer script template (2L-V): 00 00 0000000000000000000000000000
                LRC: 22
         */
        /*
        if(onlineCom != null) {
            byte[] payload = new byte[1024];
            int payloadLen = 0;
            int payloadIndex = 0;

            //Message Type (02XX)
            byte msgType = (byte)0x00;
            final int MESSAGE_TYPE_LEN = 1;
            payload[payloadIndex] = msgType;
            payloadIndex += MESSAGE_TYPE_LEN;
            payloadLen += MESSAGE_TYPE_LEN;


            //Terminal Identifier
            byte[] tid = new byte[] {0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38};
            final int TID_LEN = 8;
            for(int i = payloadIndex, j = 0 ; j < TID_LEN ; i++, j++) {
                payload[i] = tid[j];    //0 0 -> 7 7
            }
            payloadIndex += TID_LEN;    //8
            payloadLen += TID_LEN;  //8

            //Transaction Date
            byte[] transDate = new byte[] {0x21, 0x06, 0x24};
            final int DATE_LEN = 3;
            for(int i = payloadIndex, j = 0 ; j < DATE_LEN ; i++, j++) {
                payload[i] = transDate[j];    //8 0 -> 10 2
            }
            payloadIndex += DATE_LEN;    //11
            payloadLen += DATE_LEN;  //11

            //Transaction Time
            byte[] transTime = new byte[] {0x11, 0x43, 0x38};
            final int TIME_LEN = 3;
            for(int i = payloadIndex, j = 0 ; j < TIME_LEN ; i++, j++) {
                payload[i] = transTime[j];    //11 0 -> 13 2
            }
            payloadIndex += TIME_LEN;    //14
            payloadLen += TIME_LEN;  //14

            //ARQC
            byte arqc = (byte)0x80;
            final int ARQC_LEN = 1;
            payload[payloadIndex] = arqc;
            payloadIndex += ARQC_LEN;    //15
            payloadLen += ARQC_LEN;  //15

            //Transaction Type
            byte transType = (byte)0x00;    //Good and Service
            final int TRANS_TYPE_LEN = 1;
            payload[payloadIndex] = transType;
            payloadIndex += TRANS_TYPE_LEN;    //16
            payloadLen += TRANS_TYPE_LEN;  //16

            //Amount, Authorized
            byte[] amtAuthor = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
            final int AMT_AUTHOR_LEN = 6;
            for(int i = payloadIndex, j = 0 ; j < AMT_AUTHOR_LEN ; i++, j++) {
                payload[i] = amtAuthor[j];    //16 0 -> 21 5
            }
            payloadIndex += AMT_AUTHOR_LEN;    //22
            payloadLen += AMT_AUTHOR_LEN;  //22

            //Amount, Other
            byte[] amtOther = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            final int AMT_OTHER_LEN = 6;
            for(int i = payloadIndex, j = 0 ; j < AMT_OTHER_LEN ; i++, j++) {
                payload[i] = amtOther[j];    //22 0 -> 27 5
            }
            payloadIndex += AMT_OTHER_LEN;    //28
            payloadLen += AMT_OTHER_LEN;  //28

            //Transaction Amount
            byte[] transAmt = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
            final int TRANS_AMT_LEN = 6;
            for(int i = payloadIndex, j = 0 ; j < TRANS_AMT_LEN ; i++, j++) {
                payload[i] = transAmt[j];    //28 0 -> 33 5
            }
            payloadIndex += TRANS_AMT_LEN;    //34
            payloadLen += TRANS_AMT_LEN;  //34

            //Terminal Currency Code
            byte[] terminalCurrencyCode = new byte[] {0x09, 0x78};
            final int TERMINAL_CURRENCY_CODE_LEN = 2;
            for(int i = payloadIndex, j = 0 ; j < TERMINAL_CURRENCY_CODE_LEN ; i++, j++) {
                payload[i] = terminalCurrencyCode[j];    //34 0 -> 35 1
            }
            payloadIndex += TERMINAL_CURRENCY_CODE_LEN;    //36
            payloadLen += TERMINAL_CURRENCY_CODE_LEN;  //36

            //Application PAN
            byte[] pan = new byte[] {0x41, (byte) 0x82, 0x30, (byte) 0x87, 0x03, 0x42, 0x21, 0x20, (byte) 0xFF, (byte) 0xFF};
            final int PAN_LEN = 10;
            for(int i = payloadIndex, j = 0 ; j < PAN_LEN ; i++, j++) {
                payload[i] = pan[j];    //36 0 -> 45 9
            }
            payloadIndex += PAN_LEN;    //46
            payloadLen += PAN_LEN;  //46

            //Application PAN Sequence Number
            byte panSeqNumber = (byte)0x00;
            final int PAN_SEQ_NUM_LEN = 1;
            payload[payloadIndex] = panSeqNumber;
            payloadIndex += PAN_SEQ_NUM_LEN;    //47
            payloadLen += PAN_SEQ_NUM_LEN;  //47

            //Application Interchange Profile (b2)
            byte[] aip = new byte[] {0x00, 0x00};
            final int AIP_LEN = 2;
            for(int i = payloadIndex, j = 0 ; j < AIP_LEN ; i++, j++) {
                payload[i] = aip[j];    //47 0 -> 48 1
            }
            payloadIndex += AIP_LEN;    //49
            payloadLen += AIP_LEN;  //49

            //Application Transaction Counter (b2) - ATC
            byte[] atc = new byte[] {0x00, 0x00};
            final int ATC_LEN = 2;
            for(int i = payloadIndex, j = 0 ; j < ATC_LEN ; i++, j++) {
                payload[i] = atc[j];    //49 0 -> 50 1
            }
            payloadIndex += ATC_LEN;    //51
            payloadLen += ATC_LEN;  //51

            //Terminal Verification Results
            byte[] tvr = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00};
            final int TVR_LEN = 5;
            for(int i = payloadIndex, j = 0 ; j < TVR_LEN ; i++, j++) {
                payload[i] = tvr[j];    //51 0 -> 55 4
            }
            payloadIndex += TVR_LEN;    //56
            payloadLen += TVR_LEN;  //56

            //Terminal Country Code
            byte[] terminalCountryCode = new byte[] {0x00, 0x56};
            final int TERMINAL_COUNTRY_CODE_LEN = 2;
            for(int i = payloadIndex, j = 0 ; j < TERMINAL_COUNTRY_CODE_LEN ; i++, j++) {
                payload[i] = terminalCountryCode[j];    //56 0 -> 57 1
            }
            payloadIndex += TERMINAL_COUNTRY_CODE_LEN;    //58
            payloadLen += TERMINAL_COUNTRY_CODE_LEN;  //58

            //Unpredictable Number (2L-V)
            byte[] un = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            final int UN_LEN = 6;
            for(int i = payloadIndex, j = 0 ; j < UN_LEN ; i++, j++) {
                payload[i] = un[j];    //58 0 -> 63 5
            }
            payloadIndex += UN_LEN;    //64
            payloadLen += UN_LEN;  //64

            //Issuer Application Data - IAD (2L-V)
            byte[] iad = new byte[] {0x00, 0x00,
                                     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            final int IAD_LEN = 34;
            for(int i = payloadIndex, j = 0 ; j < IAD_LEN ; i++, j++) {
                payload[i] = iad[j];    //64 0 -> 97 33
            }
            payloadIndex += IAD_LEN;    //98
            payloadLen += IAD_LEN;  //98

            //Cryptogram Information Data (b1) - CID: 00
            byte cid = (byte)0x00;
            final int CID_LEN = 1;
            payload[payloadIndex] = cid;
            payloadIndex += CID_LEN;    //99
            payloadLen += CID_LEN;  //99

            //Application Cryptogram (b8)
            byte[] appCrypto = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            final int APP_CRYPTO_LEN = 8;
            for(int i = payloadIndex, j = 0 ; j < APP_CRYPTO_LEN ; i++, j++) {
                payload[i] = appCrypto[j];    //99 0 -> 106 7
            }
            payloadIndex += APP_CRYPTO_LEN;    //107
            payloadLen += APP_CRYPTO_LEN;  //107

            //POS Entry Mode (n2)
            byte posEntry = (byte)0x80;
            final int POS_ENTRY_LEN = 1;
            payload[payloadIndex] = posEntry;
            payloadIndex += POS_ENTRY_LEN;    //108
            payloadLen += POS_ENTRY_LEN;  //108

            //The following data
            //Issuer Script Results for Reversal if present (2L-V[5*n])
            //Enciphered PIN Data (RFU)
            //TSI (2 bytes)
            //KSN (if available)
            byte[] otherData = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                           0x02, 0x00, 0x11, 0x22, 0x10, 0x00, 0x41, (byte)0x82,
                                           0x30, (byte)0x87, 0x03, 0x42, 0x21, 0x20, (byte)0xD2, 0x21,
                                           0x12, 0x01, 0x14, 0x55, (byte)0x83, 0x08, 0x06, 0x00,
                                           0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x08, 0x00,
                                           0x33, 0x31, 0x30, 0x30, 0x39, 0x35, 0x36, 0x37,
                                           0x03, 0x00, (byte)0xE0, (byte)0xF8, (byte)0xC8, 0x01, 0x00, 0x22,
                                           0x06, 0x00, 0x08, 0x22, 0x00, 0x00, 0x00, 0x01,
                                           0x02, 0x00, 0x00, 0x01, 0x0F, 0x00, 0x31, 0x32,
                                           0x33, 0x34, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
                                           0x30, 0x30, 0x30, 0x30, 0x30, 0x02, 0x00, 0x30,
                                           0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            final int OTHER_DATA_LEN = 95;
            for(int i = payloadIndex, j = 0 ; j < OTHER_DATA_LEN ; i++, j++) {
                payload[i] = otherData[j];    //108 0 -> 202 94
            }
            payloadIndex += OTHER_DATA_LEN;    //203
            payloadLen += OTHER_DATA_LEN;  //203


            byte[] packetData = new byte[4 + payloadLen];   //SOH(1) + LEN(2) + PAYLOAD(n) + LRC(1)
            packetData[0] = (byte)0x01;
            packetData[1] = (byte)(payloadLen & 0x00FF);
            packetData[2] = (byte)((payloadLen & 0xFF00) >> 8);
            for(int i = 0 ; i < payloadLen ; i++) {
                packetData[3 + i] = payload[i];
            }

            byte lrc = 0;
            for(int i = 1 ; i < (packetData.length - 2) ; i++) {
                lrc = (byte)(lrc ^ packetData[i]);
            }

            packetData[3 + payloadLen] = lrc;
            Log.d("Packet Data", ByteArrayConverter.ByteArrayToStringHex(packetData, packetData.length));
            Log.d("Packet Data (split)", ByteArrayConverter.ByteArrayToStringHex_withNextLine(packetData, 8, packetData.length));

            //Send packet to host
            byte[] onlinePortBuffer = onlineCom.getBuffer();
            try {
                onlineCom.write(packetData, 5000);

                onlineCom.resetBuffer();
                while(true){
                    if(onlineCom.getCurrentReceive() == 48){
                        byte response[] = new byte[] {0x06};
                        onlineCom.write(response, 1);
                        break;
                    }
                }
                debugMessage.postValue(ByteArrayConverter.ByteArrayToStringAscii(onlinePortBuffer, 48));
            } catch (IOException e) {
                e.printStackTrace();
                debugMessage.postValue("Error occurred!");
            }
        }
        */

        /*
        if(onlineCom != null){
            byte[] onlinePortBuffer = onlineCom.getBuffer();
//            byte[] sendArray = "hello World!!".getBytes();
            String testPacket = "01CC00003132333435363738210624114338800000000000" +
                                "000100000000000000000000000109784182308703422120FFFF0000" +
                                "00000000000000000056000000000000000000000000000000000000" +
                                "00000000000000000000000000000000000000000000000000000000" +
                                "0000008000000000000000000200112210004182308703422120D221" +
                                "1201145583080600000000000001080033313030393536370300E0F8" +
                                "C80100220600082200000001020000010F0031323334303030303030" +
                                "30303030300200303000000000000066";
            byte[] sendArray;
            int idx = 0;
            int sendLen = 0;

            try {
                while(sendLen != testPacket.length()) {
                    //length = 130 => 0~129 => 0~127, 128~129
                    if((testPacket.length() - sendLen) >= 128) {
                        sendArray = testPacket.substring(idx, idx + 128).getBytes();
                        idx += 128;
                        sendLen += 128;
                    }
                    else {
                        sendArray = testPacket.substring(idx, (testPacket.length() - 1)).getBytes();
                        idx = testPacket.length() - 1;
                        sendLen += testPacket.length() - sendLen;
                    }

                    Log.d("go online packet", ByteArrayConverter.ByteArrayToStringHex(sendArray, sendArray.length));
                    onlineCom.write(sendArray, 1);
                }

                onlineCom.resetBuffer();
                while(true){
                    if(onlineCom.getCurrentReceive() == 10){
                        break;
                    }
                }
                debugMessage.postValue(ByteArrayConverter.ByteArrayToStringAscii(onlinePortBuffer, 10));

            }catch (IOException e){

            }


        }
        else
            debugMessage.postValue("no online port");
        */
    }
}
