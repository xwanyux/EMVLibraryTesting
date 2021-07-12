package com.example.emv;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.example.emv.com.SerialPortWrapper;
import com.example.emv.protocol.E_Order_Protocol;
import com.example.emv.protocol.ICCCommand;
import com.example.emv.protocol.PINPADCommand;
import com.example.emv.protocol.SystemCommand;
import com.example.emv.tool.ByteArrayConverter;

import java.io.IOException;

public class EMVController extends E_Order_Protocol{

    private byte[] buffer;
    private SerialPortWrapper com;
    EMVKeyBoardPopWin popWin;
    private int baudRate;
    private EMVThread thread;
    private ICCCommand iccCommand;
    private PINPADCommand pinpadCommand;
    private SystemCommand systemCommand;


    public static final byte EPB_ISO0 = 0x00;
    public static final byte EPB_ISO1 = 0x01;
    public static final byte EPB_ISO2 = 0x02;
    public static final byte EPB_ISO3 = 0x03;
    public static final byte EPB_ISO4 = 0x04;


    public EMVController() throws IOException {
        baudRate = 115200;
        buffer = new byte[2048];
        connected();
        iccCommand = new ICCCommand();
        pinpadCommand = new PINPADCommand();
        systemCommand = new SystemCommand();

    }

    /**
     *  This function is to set pin block parameter (blocking, at most 5 seconds)
     * @param keyIndex        pin index to do generate pin block
     * @param ISOFormat       EPB_IS0 to EPB_ISO4
     * @throws IOException    set parameter failed
     *
     */
    public void setISOKeyIndexAndPinBlockIsoFormat(byte keyIndex, byte ISOFormat) throws IOException {

        byte[] responseData;
        responseData = writeAndWaitResponse(pinpadCommand.setKeyIndexAndPinBlockIsoFormat(keyIndex, ISOFormat), 5000);

        if(!pinpadCommand.isSuccess(responseData))
            throw new IOException("set failed");
    }


    /**
     * This function is to start the transaction (non blocking, notify by callback function)
     * @param basicCallback               must provide (see EMVCallBack interface)
     * @param amount                      must provide (amount should be BCD format at most 6 byte)
     * @param contactLessCallBack         can be null if you don't want (see EMVCallBack interface)
     * @param msrCallBack                 can be null if you don't want (see EMVCallBack interface)
     * @param contactCardCallBack         can be null if you don't want (see EMVCallBack interface)
     * @throws IOException
     */
    public void  start(EMVCallBack.Basic basicCallback, byte[] amount, EMVCallBack.ContactLessCallBack contactLessCallBack,
                      EMVCallBack.MagneticStripeReaderCallBack msrCallBack, EMVCallBack.ContactCardCallBack contactCardCallBack) throws IOException {

        if(thread != null){
            if(!thread.isFinished())
                throw new IOException("old task not finished");
        }

        if(basicCallback == null)
            throw new IOException("basic call back not set");

        if(amount == null)
            throw new IOException("amount not set");
        else if(amount.length != 6){
            throw new IOException("invalid amount");
        }

        EMVThread.resetImportPinState();
        thread = new EMVThread(this, basicCallback, amount);

        thread.setContactCardCallBack(contactCardCallBack);
        thread.setMsrCallBack(msrCallBack);
        thread.setCtlsCallBack(contactLessCallBack);

        thread.start();

    }

    /**
     *  This function is to show keyBoard pop up from the bottom of view (note to call closeKeyBoard when you leave the view)
     * @param context                 fragment, requireActivity()
     * @param root                    view you want to display
     * @param callBack                please refer to EMVKeyBoardCallBack
     * @param requirePinBlock         boolean require pin block or not
     */
    public void showEMVKeyBoard(Context context, View root, EMVKeyboardCallBack callBack, boolean requirePinBlock){

        // this is important!!!!!!
        new Handler(Looper.getMainLooper()).post(() -> {
            popWin = new EMVKeyBoardPopWin(context, this, callBack, requirePinBlock);
            popWin.showAtLocation(root, Gravity.BOTTOM, 0, 0);
        });

//        popWin = new EMVKeyBoardPopWin(context, this);
//        popWin.showAtLocation(root, Gravity.BOTTOM, 0, 0);
    }


    /**
     *  This function need to be call when you leave current fragment or activity
     */
    /*need to close when you leave current fragment or activity!!!!*/
    public void closeKeyBoard(){
        if(popWin != null)
            popWin.dismiss();
    }


    /**
     * This function is to connect to secure processor com port, you should call it when your activity resume
     * @throws IOException connected failed
     */
    public void connected() throws IOException {

        if(com == null) {
            com = new SerialPortWrapper(buffer, baudRate);
            setCom(com);
            setBufferData(buffer);
        }
    }

    /**
     * This function is to disconnect to secure processor com port, you should call it when your activity onStop
     * (if you are not disconnected , other application use the same port will not able to get data)
     * @throws IOException disconnect failed
     */
    public void disconnected() throws IOException {
        if(com != null){
            com.close();
            com = null;

        }
        if(thread != null){
            thread.interrupt();
            thread = null;
        }
    }


    // for now just ignore any parameter setting
    // we will have lot of things need to be set, e.g like AID, Public key, EMV level2 parameter
    /*this command will block*/
    public byte[] readKernelData(byte Tag) throws IOException {
        return readKernelData((byte) 0x00, Tag);
    }

    public byte[] readKernelData(byte firstTag, byte secondTag) throws IOException {
        byte[] responseData;
         responseData = writeAndWaitResponse(iccCommand.getDataElementPacket(firstTag,secondTag),5000);
         if(iccCommand.isSuccess(responseData))
             return iccCommand.getData(responseData);
         throw new IOException();
    }

    /*not testing yet*/
    public byte[] getKeyMode() throws IOException{
        byte[] responseData;
        responseData = writeAndWaitResponse(pinpadCommand.getPinMode(), 5000);
        if(pinpadCommand.isSuccess(responseData))
            return pinpadCommand.getData(responseData);
        throw new IOException();
    }

    /* not testing yet*/
    public byte[] getMacByCurrentKeyMode(byte macMode, byte keyIndex, byte[] initialVector, byte[] data) throws IOException {
        byte[] responseData;
        responseData = writeAndWaitResponse(pinpadCommand.getMacByCurrentKeyMode(macMode, keyIndex, initialVector, data), 5000);
        if(pinpadCommand.isSuccess(responseData))
            return pinpadCommand.getData(responseData);
        throw new IOException();
    }

    public byte[] getRtcTime() throws IOException {
        byte[] responseData;
//        responseData = writeAndWaitResponse(systemCommand.getDateTimePacket(), 5000);
//        if(systemCommand.isSuccess(responseData))
//            return systemCommand.getData(responseData);
//        throw new IOException("Get RTC failed");

        try {
            responseData = writeAndWaitResponse(systemCommand.getDateTimePacket(), 5000);
            if(systemCommand.isSuccess(responseData))
                return systemCommand.getData(responseData);
            throw new IOException("Get RTC failed");
        } catch (IOException e) {
            throw new IOException("Get RTC time out");
        }
    }

    public void setRtcTime(byte[] rtcTime) throws IOException {
        byte[] responseData;
//        responseData = writeAndWaitResponse(systemCommand.setDateTimePacket(rtcTime), 5000);
//        if(!systemCommand.isSuccess(responseData))
//            throw new IOException("Set RTC failed");

        try {
            responseData = writeAndWaitResponse(systemCommand.setDateTimePacket(rtcTime), 5000);
            if(!systemCommand.isSuccess(responseData))
                throw new IOException("Set RTC failed");
        } catch (IOException e) {
            throw new IOException("Set RTC time out");
        }
    }
}
