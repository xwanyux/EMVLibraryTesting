package com.example.emvlibiarytesing.ui.msr;

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
import com.example.emvlibiarytesing.onPinRequireCallBack;
import com.example.emvlibiarytesing.onRequireOnlinePort;
import com.example.emvlibiarytesing.tool.BCDConverter;
import com.example.emvlibiarytesing.tool.ByteArrayConverter;
import com.example.emvlibiarytesing.tool.ConstructFinancialTransactionRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

/**
 *  This MsrViewModel to implement the callBack for MSR EMV Transaction, and send back
 *  the data result back to MsrFragment
 *
 */
public class MsrViewModel extends ViewModel implements EMVCallBack.Basic, EMVCallBack.MagneticStripeReaderCallBack{

    private EMVController emvController;
    private MutableLiveData<String> debugMessage;
    private onPinRequireCallBack callBack;
    private String message;
    private onRequireOnlinePort onlinePortCallBack;
    private boolean haveSuccessFlag;



    public MsrViewModel(){
        debugMessage = new MutableLiveData<>();
    }

    /**
     * key data structure for sending back data to fragment.
     * @return
     */
    public LiveData<String> getDebugMessage() {return debugMessage;}

    /**
     * function for fragment to provide emvController
     * @param controller
     */
    public void setEmvController(EMVController controller){
        emvController = controller;
    }

    /**
     * function for fragment to provide get online port callback
     * @param callBack
     */
    public void setOnlinePortCallBack(onRequireOnlinePort  callBack){
        onlinePortCallBack = callBack;
    }

    /**
     * function for fragment to provide call pin fragment call back
     * @param callBack
     */
    public void setRequirePinCallBack(onPinRequireCallBack callBack){
        this.callBack = callBack;
    }

    /**
     * start MSR emv transaction
     * @param Amount
     * @throws IOException
     */
    public void startTransaction(String Amount) throws IOException {

        if (emvController == null)
            throw new IOException();
        /* setting for pin block usage key and format*/
        emvController.setISOKeyIndexAndPinBlockIsoFormat((byte)0x00, EMVController.EPB_ISO0);
        emvController.start(this, BCDConverter.convertFromString(Amount), null, this, null);
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
     * this is implement EMVCallBack.MagneticStripeReaderCallBack
     * (note) because post value too often will sometime cause the fragment not receive the data.
     * we buffering all the track data at once. And send it in onGetTrack3Data or onFailedTrack3()
     * @param data               track1 data
     */
    @Override
    public void onGetTrack1Data(byte[] data) {
        haveSuccessFlag = true;
        message = "";
        message += "track1:" + ByteArrayConverter.ByteArrayToStringHex(data, data.length) + "\n";

    }

    /**
     * this is implement EMVCallBack.MagneticStripeReaderCallBack
     * (note) because post value too often will sometime cause the fragment not receive the data.
     * we buffering all the track data at once. And send it in onGetTrack3Data or onFailedTrack3()
     */
    @Override
    public void onFailedTrack1() {
        message = "";
        message += "track1: failed\n";
        haveSuccessFlag = false;

    }

    /**
     * this is implement EMVCallBack.MagneticStripeReaderCallBack
     * (note) because post value too often will sometime cause the fragment not receive the data.
     * we buffering all the track data at once. And send it in onGetTrack3Data or onFailedTrack3()
     * @param data               track2 data
     */
    @Override
    public void onGetTrack2Data(byte[] data) {
        message += "track2:" +ByteArrayConverter.ByteArrayToStringHex(data, data.length) + "\n";
        haveSuccessFlag = true;

    }

    /**
     * this is implement EMVCallBack.MagneticStripeReaderCallBack
     * (note) because post value too often will sometime cause the fragment not receive the data.
     * we buffering all the track data at once. And send it in onGetTrack3Data or onFailedTrack3()
     */
    @Override
    public void onFailedTrack2() {
        message += "track2: failed\n";


    }
    /**
     * this is implement EMVCallBack.MagneticStripeReaderCallBack
     * we implement as send back  all track result and start the require pin process
     * @param data               track3 data
     */
    @Override
    public void onGetTrack3Data(byte[] data) {
        haveSuccessFlag = true;
        message += "track3:" +ByteArrayConverter.ByteArrayToStringHex(data, data.length) + "\n";
        debugMessage.postValue(message);
        // need to check whether to start
        callBack.startRequirePinProcess();

    }

    /**
     * this is implement EMVCallBack.MagneticStripeReaderCallBack
     * we implement as send back all track result and if one of track success, start require pin process
     */
    @Override
    public void onFailedTrack3() {
        message += "track3 failed\n";
        debugMessage.postValue(message);
        // need to check whether to start
        if(!haveSuccessFlag)
            callBack.startRequirePinProcess();

    }

    /**
     * TODO
     * You should implement the online processing here.
     *
     * @param data               pin block data
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onGetPinBlockData(byte[] data){
//        SerialDeviceWrapper onlineCom = onlinePortCallBack.getOnlinePort();
//        byte[] finalPacket;
//        byte[] onlinePortBuffer = onlineCom.getBuffer();
        // will be something like that
        // doing some query for the data by
        //emvController.readKernelData(tag1,tag2);
        // send final packet by online port
        //onlineCom.write(finalPacket, finalPacket.length);
        // reset the port then receive the data
        // onlineCom.resetBuffer();
        // analyze the packet (depend on your application)
//        while(true){
//            int currentReceiveLength = onlineCom.getCurrentReceive();
//            if(currentReceiveLength == 10)
//                break;
//        }
        // analyze the result

        boolean successFlag = false;
        byte[] ACKArray = new byte[]{0x06};
        byte[] NAKArray = new byte[]{0x15};
        byte[] responsePacket = new byte[3];
        int lenOfResponsePacket = 0;

        BasicCommand basicCommand = new BasicCommand();

        SerialDeviceWrapper onlineCom = onlinePortCallBack.getOnlinePort();

        try {
            //Construct packet
            byte[] packet = ConstructFinancialTransactionRequest.ConstructPacket(emvController, null);
            Log.d("ONLINE PACKET:", "onGetPinBlockData: " + com.example.emv.tool.ByteArrayConverter.ByteArrayToStringHex(packet, packet.length));

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

            Log.d("Packet Data", com.example.emv.tool.ByteArrayConverter.ByteArrayToStringHex_withNextLine(packetData, 8, packetData.length));

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
                                    Log.d("Receive from host", com.example.emv.tool.ByteArrayConverter.ByteArrayToStringHex(responsePacket, responsePacket.length));
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
                    Log.d("Response MSG", com.example.emv.tool.ByteArrayConverter.ByteArrayToStringHex(responseMsg, responseMsg.length));

                    byte[] ARCArray = Arrays.copyOfRange(responseMsg, 15, 17);
                    String onlineResult = "online result:" + com.example.emv.tool.ByteArrayConverter.ByteArrayToStringAscii(ARCArray, ARCArray.length);
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
        } catch (IOException e) {
            debugMessage.postValue("Error occurred!");
        }
    }
}
