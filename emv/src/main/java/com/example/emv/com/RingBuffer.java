package com.example.emv.com;

public class RingBuffer {

    static class NoDataAvailableException extends Exception{

    }

    static class BufferFullException extends Exception{

    }

    private byte[] buffer;
    private int writeIdx;
    private int readIdx;
    private int bufferSize;

    RingBuffer(int size){
        buffer = new byte[size];
        writeIdx = 0;
        readIdx = 0;
        bufferSize = size;
        for(int i = 0; i < bufferSize; i++)
            buffer[i] = (byte) 0xff;

    }


    public int getCurrentReceive(){
        if(writeIdx < readIdx)
            return 255 - readIdx + writeIdx;
        else
            return  writeIdx - readIdx;
    }

    public byte read() throws NoDataAvailableException {
        byte return_byte = 0;
        if (readIdx == writeIdx)
            throw new RingBuffer.NoDataAvailableException();
        else {
            synchronized (this) {
                return_byte = buffer[readIdx];
                buffer[readIdx] = (byte) 0xff;
                readIdx = (readIdx + 1) % bufferSize;
            }
        }
        return return_byte;


    }


    public void write(byte data) throws BufferFullException {
        //display("write");
        int temp_idx;
        temp_idx = (writeIdx + 1) % bufferSize;
        if (temp_idx != readIdx) {
            synchronized (this) {
                buffer[writeIdx] = data;
                writeIdx = temp_idx;
            }
        } else
            throw new RingBuffer.BufferFullException();

    }

    public void reset(){
        synchronized (this) {
            writeIdx = 0;
            readIdx = bufferSize - 1;
        }
    }









}
