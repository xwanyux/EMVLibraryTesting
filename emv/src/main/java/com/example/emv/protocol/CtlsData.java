package com.example.emv.protocol;

public class CtlsData {


    private final byte RC_SUCCESS = (byte) 0x00;
    private final byte RC_DATA = (byte)  0x01;
    private final byte RC_INSERT = (byte) 0xe1;
    private final byte RC_SWIPE = (byte) 0xe2;
    private final byte RC_TIMEOUT = (byte) 0xf2;

    private byte responseCode;
    private byte schemeID;
    private byte[] track1;
    private byte[] track2;
    private byte[] chipData;
    private byte[] dataTime;


    public void setDataTime(byte[] data){
        dataTime = data;
    }
    public void setTrack1Data(byte[] data){
        track1 = data;
    }

    public void setTrack2Data(byte[] data){
        track2 = data;
    }

    public void setSchemeID(byte ID){
        schemeID = ID;
    }

    public void setResponseCode(byte code){
        responseCode = code;
    }

    public void setChipData(byte[] data){
        chipData = data;
    }

    public byte getSchemeID(){
        return schemeID;
    }

    public byte[] getTrack1Data(){
        return track1;
    }

    public byte[] getTrack2Data(){
        return track2;
    }

    public byte[] getChipData(){
        return chipData;
    }

    public byte[] getDataTime(){
        return dataTime;
    }



    public boolean isICCInsert(){
        return responseCode == RC_INSERT;
    }
    public boolean isSwipe(){
        return responseCode == RC_SWIPE;
    }

    public boolean isSuccess(){
        return (responseCode == RC_SUCCESS) || (responseCode == RC_DATA);
    }

    public boolean isTimeOut() {return responseCode == RC_TIMEOUT;}

    public boolean isTrack1DataExist(){
        return track1 != null;
    }

    public boolean isTrack2DataExist(){
        return track2 != null;
    }

    public boolean isChipDataExist(){
        return chipData != null;
    }

    public boolean isDateTimeExist(){
        return dataTime != null;
    }


}
