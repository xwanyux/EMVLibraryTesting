package com.example.emv.com;

import android.util.Log;

import com.example.emv.tool.ByteArrayConverter;

import java.io.IOException;
import java.util.Calendar;


/**
 *  This a abstract class for other class to implement the protocol.
 *  The abstract method isValidPacket (the method is tell when is the receive success)
 *  The abstract method getData (the method is for extracting the data for receiving packet)
 *
 *  After this two abstract method is set, this class become
 *  if you provide a SerialDeviceWrapper and a data buffer,
 *  you can use receiveWithTimeOut and writeAndWaitResponse
 *  (the data you receive is packet per unit)
 */
abstract public class SerialDeviceWithProtocol {


    protected SerialDeviceWrapper com;
    protected byte[] bufferData;


    protected void setCom(SerialDeviceWrapper com){
        this.com = com;
    }

    // for to set Com first
    protected void setBufferData(byte[] bufferData){
        this.bufferData = bufferData;
        if(com != null)
            com.setBuffer(bufferData);
    }

    abstract protected boolean isValidPacket(byte[] packet, int length);

    abstract protected byte[] getData(byte[] packet, int length);

    // use some approximate method
    public byte[] receiveWithTimeOut(int waitTime) throws IOException{
        if(com == null)
            throw new IOException("com not initialize");
        if(bufferData == null)
            throw new IOException("buffer not set");

        connectBufferToCom();

        long currentTime =  Calendar.getInstance().getTimeInMillis();;
        boolean successFlag = false;
        while (true){
            if(isValidPacket(bufferData, com.getCurrentReceive())){
                successFlag = true;
                break;
            }
            if((Calendar.getInstance().getTimeInMillis() - currentTime) > waitTime)
                break;
        }
        //Log.d("receive","(in the com port)" + "com buffer:" + bufferData.hashCode());
        //Log.d("receive","(in the com port)" + "com hash:" + com.hashCode());
        Log.d("receive", "(in the com port) "+ ByteArrayConverter.ByteArrayToStringHex(bufferData, com.getCurrentReceive()));

        if(successFlag)
            return getData(bufferData, com.getCurrentReceive());

        throw new IOException("time out");
    }


    public byte[] writeAndWaitResponse(byte[] data, int waitTime) throws IOException {
        if(com == null)
            throw new IOException("com not initialize");
        if(bufferData == null)
            throw new IOException("buffer not set");
        connectBufferToCom();
        com.resetBuffer();
        com.write(data, 10); // this just put a small number
        return receiveWithTimeOut(waitTime);

    }


    private void connectBufferToCom(){
        if(com != null && bufferData != null )
            com.setBuffer(bufferData);
    }




}
