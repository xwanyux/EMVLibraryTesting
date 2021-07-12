package com.example.emvlibiarytesing.ui.icc;

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
import com.example.emv.protocol.ICCCommand;
import com.example.emv.tool.ByteArrayConverter;
import com.example.emvlibiarytesing.onPinRequireCallBack;
import com.example.emvlibiarytesing.onRequireOnlinePort;
import com.example.emvlibiarytesing.tool.BCDConverter;
import com.example.emvlibiarytesing.tool.ConstructFinancialTransactionRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

public class IccViewModel extends ViewModel implements EMVCallBack.Basic, EMVCallBack.ContactCardCallBack {


    @Override
    public void onTimeOut() {
        debugMessage.postValue("time out!!");
    }

    @Override
    public void onTransactionType(byte type) {
        if(type == EMVThread.CTLS)
            debugMessage.postValue("type: CTLS");
        else if(type == EMVThread.ICC)
            debugMessage.postValue("type: ICC");
        else if(type == EMVThread.MSR)
            debugMessage.postValue("type: MSR");
    }


    @Override
    public void onFailed(String FailCommand) {

        debugMessage.postValue("Failed Command:" + FailCommand);
    }

    @Override
    public void onGetCandidateList(byte[] candidateList) {

        Log.d("EMV", "onGetCandidateList: ");
    }

    @Override
    public ICCCommand.SelectAppData onGetSelectAppData() {
        return data;
    }

    @Override
    public void onCVMRequire(byte cvmCode, byte cvmCondition) {
        /* the api should have the ability to tell the user require pin or not*/
        /* now, it's not clear*/
        Log.d("EMV", "onCVMRequire: cvmCode:" + ByteArrayConverter.ByteArrayToStringHex(new byte[]{cvmCode}, 1));
        Log.d("EMV", "onCVMRequire: cvmCondiction:" + ByteArrayConverter.ByteArrayToStringHex(new byte[]{cvmCondition}, 1));
        Log.d("EMV", "onCVMRequire: ");


    }

    @Override
    public void onPinRequire() {
        callBack.startRequirePinProcess();
    }

    @Override
    public void onReceiveOfflineResult(byte[] data) {
        Log.d("EMV", "onReceiveOfflineResult: ");
        debugMessage.postValue("offline result:" + ByteArrayConverter.ByteArrayToStringAscii(data, data.length));

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public byte[] onGoOnline()  {
        Log.d("EMV", "onGoOnline: ");
        // unable go online
        byte[] localData = new byte[8];
        boolean successFlag = false;
        byte[] ACKArray = new byte[]{0x06};
        byte[] NAKArray = new byte[]{0x15};
        byte[] responsePacket = new byte[3];
        int lenOfResponsePacket = 0;
        int lenOfResponseData = 0;

        BasicCommand basicCommand = new BasicCommand();
        ByteArrayOutputStream returnData = new ByteArrayOutputStream();

        SerialDeviceWrapper onlineCom = onlinePortCallBack.getOnlinePort();
        localData[0] = 0x30;
        localData[1] = 0x30;
        byte[] emvdata;
        try {
            //Construct packet
            byte[] packet = ConstructFinancialTransactionRequest.ConstructPacket(emvController, null);
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

                    /**
                     * The following data are for executing the EMV completion process
                     * Authorization Response Code, an2
                     * Authorization Code, an6, 2L-V
                     * Issuer Authentication Data, b16, 2L-V
                     * Issuer Script Template, var, 2L-V
                     */
                    returnData.write(responseMsg, 15, responsePacket[1] - 15);
                    Log.d("Return Data", ByteArrayConverter.ByteArrayToStringHex(returnData.toByteArray(), returnData.toByteArray().length));
                }
                else {
                    //Send NAK to host
                    onlineCom.write(NAKArray, 1);
                }
            }

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            /*
            emvdata =emvController.readKernelData((byte) 0x9f, (byte) 0x06);
            Log.d("EMV", "onGoOnline:(9f06) " + ByteArrayConverter.ByteArrayToStringHex(emvdata, emvdata.length));
            emvdata = emvController.readKernelData((byte)0x95);
            Log.d("EMV", "onGoOnline:(95) " + ByteArrayConverter.ByteArrayToStringHex(emvdata, emvdata.length));
            byte[] packet = ConstructFinancialTransactionRequest.ConstructPacket(emvController);
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
            Log.d("Packet Data", ByteArrayConverter.ByteArrayToStringHex(packetData, packetData.length));
            Log.d("Packet Data (split)", ByteArrayConverter.ByteArrayToStringHex_withNextLine(packetData, 8, packetData.length));

            if(onlineCom != null) {
                byte[] onlinePortBuffer = onlineCom.getBuffer();

                //Send packet to host
                onlineCom.write(packetData, 5000);

                //Receive packet from host
                onlineCom.resetBuffer();
                while(true) {
                    if(onlineCom.getCurrentReceive() == 47) {   //ACK + response packet
                        //Send ACK to host
                        byte response[] = new byte[] {ACK};
                        onlineCom.write(response, 1);
                        break;
                    }
                }

                //Parse host response data
                lenOfResponseData = onlinePortBuffer[2];
                Log.d("length of response data", String.valueOf(lenOfResponseData));
                byte[] responseData = Arrays.copyOfRange(onlinePortBuffer, 3, 3 + lenOfResponseData);
                Log.d("Response Data", ByteArrayConverter.ByteArrayToStringHex(responseData, responseData.length));

                returnData.write(responseData, 15, lenOfResponseData - 15);
                Log.d("Return Data", ByteArrayConverter.ByteArrayToStringHex(returnData.toByteArray(), returnData.toByteArray().length));
            }
            */
        }catch (IOException e){
            debugMessage.postValue("Error occurred!");
        }

//        if(onlineCom != null){
//            byte[] onlinePortBuffer = onlineCom.getBuffer();
//            byte[] sendArray = "hello World!!".getBytes();
//            try {
//                onlineCom.write(sendArray, 1);
//                onlineCom.resetBuffer();
//                while(true){
//                    if(onlineCom.getCurrentReceive() == 10){
//                        break;
//                    }
//                }
//                debugMessage.postValue(ByteArrayConverter.ByteArrayToStringAscii(onlinePortBuffer, 10));
//
//            }catch (IOException e){
//
//            }
//
//
//        }
//        else
//            debugMessage.postValue("no online port");

//        return localData;
        return returnData.toByteArray();
    }

    @Override
    public void onReceiveOnlineResult(byte[] data) {
        Log.d("EMV", "onReceiveOnlineResult: ");
        String onlineResult = "online result:" + ByteArrayConverter.ByteArrayToStringAscii(data, data.length);
        if((data[0] == 0x30) && (data[1] == 0x30)) {
            debugMessage.postValue(onlineResult + "\n" + "Approved");
        }
        else {
            debugMessage.postValue(onlineResult + "\n" + "Declined");
        }
    }




    private EMVController emvController;
    private MutableLiveData<String> debugMessage;
    private ICCCommand.SelectAppData data;
    private onPinRequireCallBack callBack;
    private onRequireOnlinePort onlinePortCallBack;



    public IccViewModel() {

        debugMessage = new MutableLiveData<>();
        data = new ICCCommand.SelectAppData();
        //data.amtAuth = new byte[6];
        data.amtOther = new byte[6];
        data.tsc = new byte[3];
        data.tid = new byte[8];


    }

    public void setOnlinePortCallBack(onRequireOnlinePort  callBack){
        onlinePortCallBack = callBack;
    }

    public LiveData<String> getDebugMessage() {return debugMessage;}

    public void setRequirePinCallBack(onPinRequireCallBack callBack){
        this.callBack = callBack;
    }

    public void setEmvController(EMVController controller){
        emvController = controller;
    }


    public void startTransaction(String Amount) throws IOException {

        if (emvController == null)
            throw new IOException();
        data.amtAuth = BCDConverter.convertFromString(Amount);
        emvController.setISOKeyIndexAndPinBlockIsoFormat((byte)0x00, EMVController.EPB_ISO0);
        emvController.start(this, BCDConverter.convertFromString(Amount), null, null, this);
    }


}
