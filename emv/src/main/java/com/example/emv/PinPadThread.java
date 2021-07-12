package com.example.emv;

import android.util.Log;

import com.example.emv.protocol.E_Order_Protocol;
import com.example.emv.protocol.ICCCommand;
import com.example.emv.protocol.PINPADCommand;
import com.example.emv.tool.ByteArrayConverter;

import java.io.IOException;

public class PinPadThread extends Thread {


    public interface PinPadThreadCallBack {

        void onFailed(String command);

        void onGetEncodePermutation(byte[] data);

        void onGetEncipherPinBlock(byte[] data);

        void onSuccessSetPin();

    }

    final private byte GET_PERMUTATION = 0x01;
    final private byte WRITE_PERMUTED_PIN = 0x02;
    final private byte GET_ENCRYPTED_PIN_BLOCK = 0x03;


    private E_Order_Protocol protocol;
    private String currentCommand;
    //private ICCCommand iccCommand;
    private PINPADCommand pinPadCommand;
    private byte[] modulus;
    private byte[] exponent;
    private byte[] permuted_pin;
    private PinPadThreadCallBack pinPadThreadCallBack;
    private byte flag;

    public PinPadThread(byte[] modulus, byte[] exponent, PinPadThreadCallBack callBack, E_Order_Protocol protocol) {
        this.modulus = modulus;
        this.exponent = exponent;
        //iccCommand = new ICCCommand();
        pinPadCommand = new PINPADCommand();
        pinPadThreadCallBack = callBack;
        flag = GET_PERMUTATION;
        currentCommand = "get permutation";
        this.protocol = protocol;
    }

    public PinPadThread(byte[] permutedPin, PinPadThreadCallBack callBack, E_Order_Protocol protocol) {
        flag = WRITE_PERMUTED_PIN;
        permuted_pin = permutedPin;
        //iccCommand = new ICCCommand();
        pinPadCommand = new PINPADCommand();
        pinPadThreadCallBack = callBack;
        currentCommand = "write permuted pin";
        this.protocol = protocol;
    }

    public PinPadThread(PinPadThreadCallBack callBack, E_Order_Protocol protocol) {
        flag = GET_ENCRYPTED_PIN_BLOCK;
        //iccCommand = new ICCCommand();
        pinPadCommand = new PINPADCommand();
        this.protocol = protocol;
        this.pinPadThreadCallBack = callBack;
        currentCommand = "get encrypted pin block";
    }


    @Override
    public void run() {


        byte[] responsePacket = new byte[0];
        try {
            if (pinPadThreadCallBack == null || protocol == null)
                throw new IOException();
            if (flag == GET_PERMUTATION)
                responsePacket = getPermutation(modulus, exponent);
            else if (flag == WRITE_PERMUTED_PIN)
                responsePacket = writePermutedPin(permuted_pin);
            else if (flag == GET_ENCRYPTED_PIN_BLOCK)
                responsePacket = getEncryptedPinBlock();

            if (isInterrupted())
                throw new IOException();

            if (pinPadCommand.isSuccess(responsePacket)) {
                // cvmCallBack.onGetEncodePermutation(iccCommand.getData(responsePacket));
                if (flag == GET_PERMUTATION) {
                    pinPadThreadCallBack.onGetEncodePermutation(pinPadCommand.getData(responsePacket));
                }

                /*very ugly way to do it, for I can't find a simple enough way to do it*/
                else if (flag == WRITE_PERMUTED_PIN) {
                    EMVThread.setImportPinStateSuccess();
                    pinPadThreadCallBack.onSuccessSetPin();
                } else if (flag == GET_ENCRYPTED_PIN_BLOCK) {
                    // EMV thread should never use this
                    pinPadThreadCallBack.onGetEncipherPinBlock(pinPadCommand.getData(responsePacket));
                }
            } else {
                pinPadThreadCallBack.onFailed(currentCommand);
                EMVThread.setImportPinStateFailed();
            }
        } catch (IOException e) {
            pinPadThreadCallBack.onFailed(currentCommand);
        }

    }

    private byte[] getPermutation(byte[] modulus, byte[] exponent) throws IOException {
        currentCommand = "get permutation";
        return protocol.writeAndWaitResponse(pinPadCommand.getPermutationPacket(modulus, exponent), 5000);
    }

    private byte[] writePermutedPin(byte[] permutedPin) throws IOException {
        currentCommand = "write permuted pin";
        return protocol.writeAndWaitResponse(pinPadCommand.writePermutedPinPacket(permutedPin), 5000);
    }

    private byte[] getEncryptedPinBlock() throws IOException {
        currentCommand = "get encrypted pin block";
        return protocol.writeAndWaitResponse(pinPadCommand.getEncryptedPinBlockPacket(), 5000);

    }
}
