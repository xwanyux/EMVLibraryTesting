package com.example.emv.protocol;

import java.security.InvalidParameterException;

public class ICCCommand extends BasicCommand {

    private static final byte CHNL_ICC = 0x20;
    private static final byte CMD_ICC_INIT_KERNEL = 0x01;
    private static final byte CMD_ICC_OPEN_SESSION = 0x02;
    private static final byte CMD_ICC_CLOSE_SESSION = 0x03;
    private static final byte CMD_ICC_DETECT_ICC = 0x04;
    private static final byte CMD_ICC_ENABLE_ICC = 0x05;
    private static final byte CMD_ICC_DISABLE_ICC = 0x06;
    private static final byte CMD_ICC_CREATE_CANDIDATE_LIST = 0x07;
    private static final byte CMD_ICC_SELECT_APP = 0x08;
    private static final byte CMD_ICC_EXEC_1 = 0x09;
    private static final byte CMD_ICC_CVM = 0x0A;
    private static final byte CMD_ICC_EXEC_2 = 0x0B;
    private static final byte CMD_ICC_COMPLETION = 0x0C;
    private static final byte CMD_ICC_SET_PARA = 0x0D;
    private static final byte CMD_ICC_EMVK_GET_DATA_ELEMENT = 0x10;


    private static final byte RC_READY = 0x20;
    private static final byte RC_NOT_READY = 0x21;
    private static final byte RC_ABORTED = 0x22;
    private static final byte RC_GO_ONLINE = 0x23;
    private static final byte RC_CVM_REQUIRE = 0x24;
    private static final byte RC_AUTO_SELECTED = 0x25;


    public static class SelectAppData{

        public byte itemNumber;
        public byte transType;
        public byte[] amtAuth;
        public byte[] amtOther;
        public byte[] tsc;
        public byte[] tid;
    }


    public boolean isReady(byte[] receivePacket){
        return RC_READY == getStatus(receivePacket);
    }
    public boolean isNotReady(byte[] receivePacket){
        return RC_NOT_READY == getStatus(receivePacket);
    }

    public boolean isAborted(byte[] receivePacket){
        return RC_ABORTED == getStatus(receivePacket);
    }

    public boolean isGoOnline(byte[] receivePacket){
        return RC_GO_ONLINE  == getStatus(receivePacket);
    }

    public boolean isCvmRequire(byte[] receivePacket){
        return RC_CVM_REQUIRE == getStatus(receivePacket);
    }

    public boolean isAutoSelected(byte[] receivePacket){
        return RC_AUTO_SELECTED == getStatus(receivePacket);
    }



    public byte[] initKernelPacket(byte kernelConfigID){
        byte[] data = new byte[1];
        data[0] = kernelConfigID;
        return createPacket( CHNL_ICC, CMD_ICC_INIT_KERNEL, data);
    }

    public byte[] openSessionPacket(){ return createPacket(CHNL_ICC, CMD_ICC_OPEN_SESSION, new byte[0]);}

    public byte[] closeSessionPacket() { return createPacket(CHNL_ICC, CMD_ICC_CLOSE_SESSION, new byte[0]);}

    public byte[] detectICCPacket(){ return createPacket(CHNL_ICC, CMD_ICC_DETECT_ICC, new byte[0]);}

    public byte[] enableICCPacket(){ return createPacket(CHNL_ICC, CMD_ICC_ENABLE_ICC, new byte[0]);}

    public byte[] disableICCPacket(){ return createPacket(CHNL_ICC, CMD_ICC_DISABLE_ICC, new byte[0]);}

    public byte[] createCandidateListPacket(){return createPacket(CHNL_ICC, CMD_ICC_CREATE_CANDIDATE_LIST, new byte[0]); }


    public byte[] selectAppPacket(byte itemNum, byte transType, byte[] amtAuth, byte[] amtOther, byte[] tsc, byte[] tid){
        byte[] data = new byte[25];
        data[0] = itemNum;
        data[1] = transType;
        if( (amtAuth.length != 6) || (amtOther.length != 6) || (tsc.length != 3) || (tid.length != 8))
            throw new InvalidParameterException();

        System.arraycopy(amtAuth,0, data, 2, amtAuth.length);
        System.arraycopy(amtOther,0, data, 8, amtOther.length);
        System.arraycopy(tsc,0, data, 14, tsc.length);
        System.arraycopy(tid,0, data, 17, tid.length);

        return createPacket(CHNL_ICC, CMD_ICC_SELECT_APP, data);
    }

    public byte[] selectAppPacket(SelectAppData appData){
        byte[] data = new byte[25];
        data[0] = appData.itemNumber;
        data[1] = appData.transType;
        if( (appData.amtAuth.length != 6) || (appData.amtOther.length != 6)
           || (appData.tsc.length != 3) || (appData.tid.length != 8))
            throw new InvalidParameterException();

        System.arraycopy(appData.amtAuth,0, data, 2, appData.amtAuth.length);
        System.arraycopy(appData.amtOther,0, data, 8, appData.amtOther.length);
        System.arraycopy(appData.tsc,0, data, 14, appData.tsc.length);
        System.arraycopy(appData.tid,0, data, 17, appData.tid.length);

        return createPacket(CHNL_ICC, CMD_ICC_SELECT_APP, data);

    }

    public byte[] exec1Packet(){ return createPacket(CHNL_ICC, CMD_ICC_EXEC_1, new byte[0]);}

    public byte[] cvmPacket(byte cvmCode, byte cvmCondition, byte[] cvmData){

        int cvmDataLength = (cvmData == null)? 0 : cvmData.length;
        byte[] data = new byte[2 + cvmDataLength];
        data[0] = cvmCode;
        data[1] = cvmCondition;
        if(cvmDataLength != 0)
            System.arraycopy(cvmData, 0, data, 2, cvmDataLength);

        return createPacket(CHNL_ICC, CMD_ICC_CVM, data);
    }

    public byte[] exec2Packet(){ return createPacket(CHNL_ICC, CMD_ICC_EXEC_2, new byte[0]);}


    public byte[] completionPacket(byte[] onlineData){
        if(onlineData == null)
            return createPacket(CHNL_ICC, CMD_ICC_COMPLETION, new byte[0]);
        else
            return createPacket(CHNL_ICC, CMD_ICC_COMPLETION, onlineData);
    }

    public byte[] setParameterPacket(byte[] parameter){
        if(parameter == null)
            throw new InvalidParameterException();
        return createPacket(CHNL_ICC, CMD_ICC_SET_PARA, parameter);
    }


    public byte[] getDataElementPacket(byte tag1, byte tag2){
        byte[] data = new byte[2];
        data[0] = tag1;
        data[1] = tag2;
        return createPacket(CHNL_ICC, CMD_ICC_EMVK_GET_DATA_ELEMENT, data);
    }




}
