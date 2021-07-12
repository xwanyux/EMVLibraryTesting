package com.example.comwrapper;


import java.io.IOException;

/*This is a wrapper use for direct write and event driven read
*
* user can provide two kind of buffer to receive data
*
* 1. byte[] normal buffer
*
* 2. ring buffer (I think maybe , implement ring buffer enough)
*
*
* */
public abstract class SerialDeviceWrapper {

    static final int USE_RING_BUFFER = 1;
    static final int USE_NORMAL_BUFFER = 0;

    protected int currentReceive;
    protected byte[] buffer;
    protected RingBuffer ringBuffer;
    protected int useRingBufferFlag;
    protected int baudRate;


    public void setBuffer(RingBuffer ringBuffer){
        this.ringBuffer = ringBuffer;
        this.useRingBufferFlag = USE_RING_BUFFER;
        resetBuffer();
    }

    public RingBuffer getRingBuffer() {
        return this.ringBuffer;
    }

    public void setBuffer(byte[] buffer){
        this.buffer = buffer;
        this.useRingBufferFlag = USE_NORMAL_BUFFER;
        resetBuffer();
    }

    public byte[] getBuffer(){
        return this.buffer;
    }

    public int getCurrentReceive(){
        if(this.useRingBufferFlag == USE_NORMAL_BUFFER)
            return currentReceive;
        else if(this.useRingBufferFlag == USE_RING_BUFFER)
            return ringBuffer.getCurrentReceive();
        return 0;
    }

    public int getBaudRate(){return  baudRate;}

    public void resetBuffer(){
        if(this.useRingBufferFlag == USE_NORMAL_BUFFER)
            currentReceive = 0;
        else if(this.useRingBufferFlag == USE_RING_BUFFER)
            ringBuffer.reset();
    }

    public int getCurrentUseBufferFlag(){
        return useRingBufferFlag;
    }

    abstract public void write(final byte[] src, final int timeout) throws IOException;

    abstract public void close() throws IOException;

}
