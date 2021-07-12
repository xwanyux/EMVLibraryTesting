package com.example.emv.com;


import android.util.Log;

import com.example.emv.tool.ByteArrayConverter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class SerialPortWrapper extends SerialDeviceWrapper{

    private SerialPort mSerialPort;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private ReadThread readThread;


    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                if(mSerialPort != null) {
                    int size;
                    try {
                        byte[] buffer = new byte[64];
                        if (mInputStream == null) return;
                        /**
                         *
                         *  (1) first time modify (still have bug)
                         *  modify from mInputStream.read(buffer)
                         *
                         *  to mInputStream.read(buffer, 0 , mInputStream.available())
                         *
                         *
                         *
                         *  the previous one will cause the thread.interrupt() will not kill the thread
                         *  thread will wait for the last receive to end the thread.
                         *
                         *  make non-blocking will solve this problem
                         *
                         *  (2) the problem with (1)
                         *
                         *  mInputStream.available may return the len greater than buffer size
                         *  cause index out of bound error.
                         *
                         *  So, we just need to ignore the blocking , we first check data available
                         *  if available , the read will not blocking anymore
                         *
                         */
                        size = 0;
                        if(mInputStream.available() > 0)
                            size = mInputStream.read(buffer);
                        if (size > 0) {

                            onDataReceived(buffer, size);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
    }


    private void initSerialPort(String pathname, int baudRate) throws IOException {
        mSerialPort = new SerialPort(new File(pathname), baudRate);
        mInputStream = mSerialPort.getInputStream();
        mOutputStream = mSerialPort.getOutputStream();
    }


    public SerialPortWrapper( byte[] buffer,int baudRate) throws IOException {

        initSerialPort("/dev/ttyHSL1", baudRate);
        setBuffer(buffer);
        this.baudRate = baudRate;
        readThread = new ReadThread();
        readThread.start();
    }

    public SerialPortWrapper(RingBuffer buffer, int baudRate) throws IOException {
        initSerialPort("/dev/ttyHSL1", baudRate);
        setBuffer(buffer);
        this.baudRate = baudRate;
        readThread = new ReadThread();
        readThread.start();
    }
    @Override
    public void write(final byte[] src, final int timeout) throws IOException{
        // not use time out variable , just for abstract class implementation
        mOutputStream.write(src);
        mOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        mInputStream.close();
        mOutputStream.close();
        mSerialPort.close();
        readThread.interrupt();
    }

    private void onDataReceived(final byte[] data, final int size) {

        if (useRingBufferFlag == USE_NORMAL_BUFFER) {
            for (int i = 0; i < size; i++) {
                if (currentReceive < buffer.length) {
                    buffer[currentReceive] = data[i];
                    currentReceive += 1;
                }
            }

//            Log.d("receive","(inside api)" + "com buffer" + buffer.hashCode());
           Log.d("receive_debug", "(sp)onDataReceived:" + ByteArrayConverter.ByteArrayToStringHex(data, size));

        } else if (useRingBufferFlag == USE_RING_BUFFER) {
            for (int i = 0; i < size; i++) {
                try {
                    ringBuffer.write(data[i]);
                } catch (RingBuffer.BufferFullException e) {

                }
            }

        }

    }


}

