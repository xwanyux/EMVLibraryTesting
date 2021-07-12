package com.example.emv;

import android.util.Log;

import com.example.emv.protocol.CTLSCommand;
import com.example.emv.protocol.CtlsData;
import com.example.emv.protocol.E_Order_Protocol;
import com.example.emv.protocol.ICCCommand;
import com.example.emv.protocol.MSRCommand;
import com.example.emv.tool.ByteArrayConverter;

import java.io.IOException;
import java.util.Arrays;

public class EMVThread extends Thread {


//  public class StartThread {

    public static final byte ICC = 0x01;
    public static final byte CTLS = 0x02;
    public static final byte MSR = 0x03;


    public static final int NOT_IMPORT = 0;
    public static final int IMPORT_SUCCESS = 1;
    public static final int IMPORT_FAILED = 2;
    static private int pinImportState;

    private E_Order_Protocol protocol;
    private ICCCommand iccCommand;
    private CTLSCommand ctlsCommand;
    private MSRCommand msrCommand;
    private String currentCommand;
    private byte[] amount;
    private CtlsData ctlsData;
    private Boolean track1Flag;
    private Boolean track2Flag;
    private Boolean track3Flag;
    private EMVCallBack.Basic basicCallBack;
    private EMVCallBack.ContactCardCallBack contactCardCallBack;
    private EMVCallBack.MagneticStripeReaderCallBack msrCallBack;
    private EMVCallBack.ContactLessCallBack ctlsCallBack;
    private byte cvmCode;
    private byte cvmCondition;
    private byte[] onlineData;
    private boolean isFinished;

//    private byte keyIndex;
//    private byte pinBlockIsoFormat;
//    private boolean flagKeyIndexPinBlockIsoFormat;

    private final byte CVR_PLAINTEXT_PIN = 0x01;
    private final byte CVR_ENCIPHERED_PIN_ONLINE = 0x02;
    private final byte CVR_PLAINTEXT_PIN_AND_SIGN = 0x03;
    private final byte CVR_ENCIPHERED_PIN = 0x04;
    private final byte CVR_ENCIPHERED_PIN_AND_SIGN = 0x05;

    private byte[] requirePinCVMArray = new byte[] {CVR_PLAINTEXT_PIN, CVR_ENCIPHERED_PIN_ONLINE, CVR_PLAINTEXT_PIN_AND_SIGN,  CVR_ENCIPHERED_PIN,
            CVR_ENCIPHERED_PIN_AND_SIGN};


    private static class ErrorCommand extends Exception{

    }

    private static class RequireCVM extends  Exception{

    }

    private boolean requirePinImport(byte cvmCode){

        for (byte b : requirePinCVMArray) {
            if (cvmCode == b)
                return true;
        }
        return false;
    }

    static public void setImportPinStateSuccess(){
        pinImportState = IMPORT_SUCCESS;
    }

    static public void setImportPinStateFailed(){
        pinImportState = IMPORT_FAILED;
    }

    static public void resetImportPinState(){
        pinImportState = NOT_IMPORT;
    }

    public boolean isFinished(){
        return isFinished;
    }


    public void setContactCardCallBack(EMVCallBack.ContactCardCallBack contactCardCallBack) {
        this.contactCardCallBack = contactCardCallBack;
    }

    public void  setMsrCallBack(EMVCallBack.MagneticStripeReaderCallBack msrCallBack){
        this.msrCallBack = msrCallBack;
    }

    public void setCtlsCallBack(EMVCallBack.ContactLessCallBack ctlsCallBack){
        this.ctlsCallBack = ctlsCallBack;
    }

    public EMVThread(E_Order_Protocol protocol, EMVCallBack.Basic callback, byte[] amount){
        pinImportState = NOT_IMPORT;
        this.protocol = protocol;
        iccCommand = new ICCCommand();
        ctlsCommand = new CTLSCommand();
        msrCommand = new MSRCommand();
        //amount = new byte[6];
        this.amount = amount;
        basicCallBack = callback;
       // flagKeyIndexPinBlockIsoFormat = false;
    }

//    public void setKeyIndexAndPinBlockIsoFormat(byte keyIndex, byte ISOFormat){
//        this.keyIndex = keyIndex;
//        this.pinBlockIsoFormat = ISOFormat;
//        flagKeyIndexPinBlockIsoFormat = true;
//    }

    /* the overall structure would be something like that*/
    /* still very messy, wait for another day to do it*/
    @Override
    public void run(){

        try {
            byte[] response;
//            if(flagKeyIndexPinBlockIsoFormat){
//                response = setKeyIndexAndIsoFormatToSp(this.keyIndex, this.pinBlockIsoFormat);
//                if(!iccCommand.isSuccess(response))
//                    throw new ErrorCommand();
//            }

            response = initKernel();
            if(!iccCommand.isSuccess(response))
                throw new ErrorCommand();
            response = openSession();
            if(!iccCommand.isSuccess(response))
                throw new ErrorCommand();
            response = MSROpen();
            if(!iccCommand.isSuccess(response))
                throw new ErrorCommand();

            response = readyForSale(amount);
            if(isInterrupted())
                throw new ErrorCommand();

            ctlsData = ctlsCommand.parseCtlsData(ctlsCommand.getData(response));
            if(ctlsData.isSuccess()){
                basicCallBack.onTransactionType(CTLS);
                if(ctlsCallBack != null)
                    ctlsCallBack.onSuccess(ctlsData);
                Log.d("CTLS", "ctls success!!!");
            }
            else if(ctlsData.isICCInsert()){
                Log.d("CTLS", "ICC insert!!!");
                basicCallBack.onTransactionType(ICC);
                if(contactCardCallBack != null)
                    processingICC();
            }
            else if(ctlsData.isSwipe()){
                Log.d("CTLS", "MSR SWIPE!!!");
                basicCallBack.onTransactionType(MSR);
                if(msrCallBack != null)
                    processingMSR();
            }
            else if(ctlsData.isTimeOut()){
                basicCallBack.onTimeOut();
                Log.d("CTLS", "Time Out!!!");
            }
            else{
                basicCallBack.onFailed(currentCommand);
                Log.d("CTLS","ERROR UNKNOWN!!");
            }

            MSRClose();
            closeSession();
        }catch (IOException | ErrorCommand | InterruptedException e){
            Log.d("CTS", "run: failed " + currentCommand);
            basicCallBack.onFailed(currentCommand);
        }catch ( RequireCVM e){

        }


        isFinished = true;

    }



    private void processingMSR() throws IOException, ErrorCommand {
        byte[] response;
        response = MSRStatus();

        track1Flag = msrCommand.isDataOkTrack1(response);
        track2Flag = msrCommand.isDataOkTrack2(response);
        track3Flag = msrCommand.isDataOKTrack3(response);

        response = MSRRead();

        if(!msrCommand.isSuccess(response) || isInterrupted())
            throw new ErrorCommand();


        MSRProcessData(msrCommand.getData(response));

        MSRClose();
    }


    private void processingICC() throws ErrorCommand, IOException, RequireCVM, InterruptedException {
        byte[] responsePacket;
        responsePacket = enableIcc();
        if(iccCommand.isFailure(responsePacket) || isInterrupted()) {

            throw new ErrorCommand();
        }

        responsePacket = createCandidateList();
        if(iccCommand.isReady(responsePacket) && !isInterrupted()){
            // on get candidate list
            // call back 2
            contactCardCallBack.onGetCandidateList(iccCommand.getData(responsePacket));
        }

        if(iccCommand.isNotReady(responsePacket) || iccCommand.isFailure(responsePacket) || isInterrupted()){
            // is not ready and failures, both as failure
            throw new ErrorCommand();
        }

        // call back 3 , should get the data
        ICCCommand.SelectAppData data = contactCardCallBack.onGetSelectAppData();
        responsePacket = selectApplication(data);
        if(iccCommand.isNotReady(responsePacket) || iccCommand.isFailure(responsePacket) || isInterrupted()){
            throw new ErrorCommand();
        }


        byte[] receiveData;
        responsePacket = exec1();
        if(iccCommand.isCvmRequire(responsePacket) && !isInterrupted()){
            // call back 4
            receiveData = iccCommand.getData(responsePacket);
            cvmCode = receiveData[0];
            cvmCondition = receiveData[1];
            contactCardCallBack.onCVMRequire(cvmCode, cvmCondition);
            //throw new RequireCVM();

            if(requirePinImport(cvmCode)){
                 contactCardCallBack.onPinRequire();
                // wait for pin to be set at most 30 secs
                int counts;
                counts = 0;
                while(!isInterrupted()){

                    if(pinImportState != NOT_IMPORT)
                        break;

                    sleep(500);
                    counts += 1;
                    if(counts == 60)
                        break;
                }
                currentCommand = "get pins failed";
                if(pinImportState != IMPORT_SUCCESS)
                    throw new ErrorCommand();

                Log.d("EMV", "processingICC: require pin\n");
            }
            else
                Log.d("EMV", "processingICC: don't require pin ");



        }
        else if(iccCommand.isSuccess(responsePacket) && !isInterrupted()){
            // seem like nothing to do
//             call back 5 (send method code and condiction code)
//            receiveData = iccCommand.getData(responsePacket);
//            cvmCode = receiveData[0];
//            cvmCondition = receiveData[1];
//            emvCallBack.onReceiveCVMCodeAndCVMCondition(cvmCode, cvmCondition);

        }

        else if(iccCommand.isFailure(responsePacket) || isInterrupted()) {
            throw new ErrorCommand();
        }


        // when got here, mean no cvm require , don't need any cvm data
        responsePacket = cardholderVerification(cvmCode,cvmCondition,new byte[0]);
        if(iccCommand.isFailure(responsePacket) || isInterrupted()) {
            throw new ErrorCommand();
        }

        responsePacket = exec2();
        if(iccCommand.isSuccess(responsePacket) && !isInterrupted()){
            // on offline result call back, call back7
            // I think these thing should mean the EMV is finished , don't need to run the further procedure
            // for now still just keep going anyway
            contactCardCallBack.onReceiveOfflineResult(iccCommand.getData(responsePacket));
        }
        else if(iccCommand.isGoOnline(responsePacket) && !isInterrupted()){
            // go online call back 6
            onlineData = contactCardCallBack.onGoOnline();
        }
        else {
            throw new ErrorCommand();
        }


        responsePacket = completion(onlineData);

        if(iccCommand.isFailure(responsePacket)){
            // on data call back;
        }

        if(iccCommand.isSuccess(responsePacket)){
            // data response
            contactCardCallBack.onReceiveOnlineResult(iccCommand.getData(responsePacket));
        }
        else{
            throw new ErrorCommand();
        }


    }

    private void MSRProcessData(byte[] data){
        int cursor = 0;
        int len;
        byte[] trackData;
        if(track1Flag){
            len = data[cursor];
            cursor+= 1;
            trackData = Arrays.copyOfRange(data, cursor, cursor + len);
            msrCallBack.onGetTrack1Data(trackData);
            cursor += len;
        }else
            msrCallBack.onFailedTrack1();


        if(track2Flag){
            len = data[cursor];
            cursor+= 1;
            trackData = Arrays.copyOfRange(data, cursor, cursor + len);
            msrCallBack.onGetTrack2Data(trackData);
            cursor += len;
        }else
            msrCallBack.onFailedTrack2();



        if(track3Flag){
            len = data[cursor];
            cursor+= 1;
            trackData = Arrays.copyOfRange(data, cursor, cursor + len);
            msrCallBack.onGetTrack3Data(trackData);
            cursor += len;
        }else
            msrCallBack.onFailedTrack3();

    }



    private byte[] readyForSale(byte[] data) throws IOException {
        currentCommand = "Ready for sale";
        return protocol.writeAndWaitResponse(ctlsCommand.readyForSalePacket(data), 5000);
    }


    private byte[] initKernel() throws IOException {

        currentCommand = "init kernel";
        byte[] packet;
        packet = iccCommand.initKernelPacket((byte)0x00);
        return protocol.writeAndWaitResponse(packet, 5000);
    }

    private byte[] openSession() throws IOException {
        currentCommand = "open session";
        return protocol.writeAndWaitResponse(iccCommand.openSessionPacket(), 5000);
    }

    private byte[] closeSession()  throws IOException {
        currentCommand = "close session";
        return protocol.writeAndWaitResponse(iccCommand.closeSessionPacket(), 5000);
    }

    private byte[] detectIcc() throws IOException {
        currentCommand = "detect ICC";
        return protocol.writeAndWaitResponse(iccCommand.detectICCPacket(), 5000);
    }

    private byte[] enableIcc() throws IOException{
        currentCommand = "enable ICC";
        return protocol.writeAndWaitResponse(iccCommand.enableICCPacket(), 5000);
    }

    private byte[] disableIcc() throws IOException {
        currentCommand = "disable ICC";
        return protocol.writeAndWaitResponse(iccCommand.disableICCPacket(), 5000);
    }


    private byte[] createCandidateList() throws IOException {
        currentCommand = "create candidate list";
        return protocol.writeAndWaitResponse(iccCommand.createCandidateListPacket(), 5000);
    }

    private byte[] selectApplication(ICCCommand.SelectAppData data) throws IOException {
        currentCommand = "select application";
        return protocol.writeAndWaitResponse(iccCommand.selectAppPacket(data), 5000);
    }

    private byte[] exec1() throws IOException{
        currentCommand = "exec1";
        return protocol.writeAndWaitResponse(iccCommand.exec1Packet(), 5000);
    }

    private byte[] cardholderVerification(byte cvmCode, byte cvmCondition, byte[] cvmData) throws IOException {
        currentCommand = "cvm";
        return  protocol.writeAndWaitResponse(iccCommand.cvmPacket(cvmCode, cvmCondition, cvmData), 5000);
    }

    private byte[] exec2() throws IOException{
        currentCommand = "exec2";
        return protocol.writeAndWaitResponse(iccCommand.exec2Packet(), 5000);
    }

    private byte[] completion(byte[] onlineData) throws IOException {
        currentCommand = "completion";
        return protocol.writeAndWaitResponse(iccCommand.completionPacket(onlineData), 5000);
    }


    private byte[] MSROpen() throws IOException {
        currentCommand = "MSR open";
        // open all
        return protocol.writeAndWaitResponse(msrCommand.openPacket((byte) 0x07), 5000);
    }

    private byte[] MSRClose() throws IOException {
        currentCommand = "MSR close";
        return protocol.writeAndWaitResponse(msrCommand.closePacket(), 5000);
    }

    private byte[] MSRStatus() throws IOException{
        currentCommand = "MSR status";
        return protocol.writeAndWaitResponse(msrCommand.statusPacket((byte) 0x01), 5000);
    }

    private byte[] MSRRead() throws IOException{
        currentCommand = "MSR Read";
        return protocol.writeAndWaitResponse(msrCommand.readPacket((byte) 0x00, (byte) 0x07), 5000);
    }

    //}
}
