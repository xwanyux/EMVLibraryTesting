package com.example.emv;

import com.example.emv.protocol.CtlsData;
import com.example.emv.protocol.ICCCommand;

import java.io.IOException;

public interface EMVCallBack {

    interface Basic{
        /**
         *  when there is no ICC, CTLS, MSR happened within 5 secs.
         *  This call back will be call.
         *  TODO: need to put 5 secs into a parameter (Just change the write and wait time in ReadyForSale Wait Time)
         */
        void onTimeOut();

        /**
         * this call back will first be call when every any of ICC, CTLS, MSR happened.
         *
         * @param type      0x01(ICC), 0x02(CTLS), 0x03(MSR)
         */
        void onTransactionType(byte type);

        /**
         *
         *  this call back will be call when any of command filed. The command is define in e-order protocol.
         * @param FailCommand                 e-order protocol command name
         */
        void onFailed(String FailCommand);
    }



     interface ContactLessCallBack{
         /**
          * this call back will be call when ctls transaction process success
          * @param data               parse ctls data
          */
         void onSuccess(CtlsData data);
    }

    interface MagneticStripeReaderCallBack{

        /**
         * this call back will ba call when track1 data success
         * @param data               track1 data
         */
        void onGetTrack1Data(byte[] data);

        /**
         * this call back will be call when track1 data not success
         */
        void onFailedTrack1();
        /**
         * this call back will ba call when track2 data success
         * @param data               track2 data
         */
        void onGetTrack2Data(byte[] data);

        /**
         * this call back will be call when track2 data not success
         */
        void onFailedTrack2();

        /**
         * this call back will be called when track3 data success
         * @param data
         */
        void onGetTrack3Data(byte[] data);

        /**
         * this call back will be call when track3 not success
         */
        void onFailedTrack3();
    }



    interface ContactCardCallBack{
        /**
         * this is a call back when e-order command CMD_EMCK_CREATE_CANDIDATE_LIST return RC_READY
         * @param candidateList      please refer to e-order protocol
         */
        void onGetCandidateList(byte[] candidateList);

        /**
         * this is a call back function when CMD_EMVK_SELECT_APP need require data
         * @return the transaction require data
         */
        ICCCommand.SelectAppData onGetSelectAppData();
        /**
         * this is a call back function when CMD_EMVK_EXEC_1 return  RC_CVM_REQUIRED
         * @param cvmCode        Method Code
         * @param cvmCondition   Condition code
         */
        void onCVMRequire(byte cvmCode, byte cvmCondition);

        /**
         * this is a call back function, when cvmRequire value indicate requiring pin
         * (note you should call EMVController.showEMVKeyBoard to response this call back)
         */
        void onPinRequire();

        /**
         * This is a call back function when CMD_EMCK_EXEC_2 return RC_SUCCESS
         * @param data      offline result code
         */
        void onReceiveOfflineResult(byte[] data);

        /**
         * This is a call back function when CMD_EMVK_EXEC_2 require RC_GO_ONLINE
         * @return data should be the result send back from the bank
         * (note, the implementation of onGoOnline can only use EMVController.ReadKernelData,
         *  if you call other function e.g getKeyMode(), will cause EMV failed.)
         */
        byte[] onGoOnline();

        /**
         * This is a call back function when CMD_EMVK_COMPLETION
         * @param data      ARC
         */
        void onReceiveOnlineResult(byte[] data);
    }







}
