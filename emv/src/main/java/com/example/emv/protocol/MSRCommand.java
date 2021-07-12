package com.example.emv.protocol;

public class MSRCommand extends BasicCommand{

    private static final byte CHNL_MSR = 0x0D;
    private static final byte CMD_MSR_OPEN = 0x01;
    private static final byte CMD_MSR_CLOSE = 0x02;
    private static final byte CMD_MSR_STATUS = 0x03;
    private static final byte CMD_MSR_READ = 0x04;

    private static final byte NO_DATA = 0x00;
    private static final byte DATA_OK = 0x01;
    private static final byte DATA_ERROR = 0x02;


    private boolean isDataOK(byte[] data, int track){

        if(data.length != 4)
            return false;

        return DATA_OK == data[track];
    }


    public boolean isDataOkTrack1(byte[] data){
        return isDataOK(data, 1);
    }

    public boolean isDataOkTrack2(byte[] data){
        return isDataOK(data, 2);
    }

    public boolean isDataOKTrack3(byte[] data){
        return isDataOK(data, 3);
    }



    public byte[] openPacket(byte trackNumber){
        byte[] data = new byte[2];
        data[0] = 0x01;
        data[1] = trackNumber;
        return createPacket(CHNL_MSR, CMD_MSR_OPEN, data);
    }

    public byte[] closePacket(){
        return createPacket(CHNL_MSR, CMD_MSR_CLOSE, new byte[0]);
    }


    public byte[] statusPacket(byte keepStatusByte){
        byte[] data = new byte[1];
        data[0] = keepStatusByte;
        return createPacket(CHNL_MSR, CMD_MSR_STATUS, data);
    }

    public byte[] readPacket(byte keepStatusByte, byte trackNumber){
        byte[] data = new byte[2];
        data[0] = keepStatusByte;
        data[1] = trackNumber;
        return createPacket(CHNL_MSR, CMD_MSR_READ, data);
    }


}
