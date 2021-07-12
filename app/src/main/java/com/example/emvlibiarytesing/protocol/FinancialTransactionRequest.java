package com.example.emvlibiarytesing.protocol;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class FinancialTransactionRequest {


    //                "TID",         "DATE",        "TIME",     "ARQC",   "TYPE",
//                        "AMT_AUTH",    "AMT_OTHER",   "AMT",      "CC",     "PAN",
//                        "PAN_SN",      "AIP",         "ATC",      "TVR",    "CNTR_CODE",
//                        "UPD_NBR",     "ISU_AP_DATA", "CID",      "AC",     "PEM",
//                        "LEN_REV_ISR", "REV_ISR",     "LEN_EPIN", "EPIN" ,  "TSI",
//                        "LEN_KSN",     "KSN"
    public enum E_FinancialTransactionRequestData{
        TID,
        DATE,
        TIME,
        ARQC,
        TYPE,
        AMT_AUTH,
        AMT_OTHER,
        AMT,
        CC,
        PAN,
        PAN_SN,
        AIP,
        ATC,
        TVR,
        CNTR_CODE,
        UPD_NBR,
        ISU_AP_DATA,
        CID,
        AC,
        PEM,
        LEN_REV_ISR,
        REV_ISR,
        LEN_EPIN,
        EPIN,
        TSI,
        LEN_KSN,
        KSN
    }


    private byte[] terminalID = new byte[8];
    private byte[] transactionDate = new byte[3];
    private byte[] transactionTime = new byte[3];
    private byte[] authorizationRequestCryptogram = new byte[1];
    private byte[] transactionType = new byte[1];
    private byte[] authorizedAmount = new byte[6];
    private byte[] otherAmount = new byte[6];
    private byte[] transactionAmount = new byte[6];
    private byte[] transactionCurrencyCode = new byte[2];
    private byte[] applicationPAN = new byte[10];
    private byte[] applicationPANSequenceNumber = new byte[1];
    private byte[] applicationInterchangeProfile = new byte[2];
    private byte[] applicationTransactionCounter = new byte[2];
    private byte[] terminalVerificationResult = new byte[5];
    private byte[] terminalCountryCode = new byte[2];
    private byte[] unpredictableNumber = new byte[6];
    private byte[] issuerApplicationData = new byte[34];
    private byte[] cryptogramInformationData = new byte[1];
    private byte[] applicationCryptogram = new byte[8];
    private byte[] POSEntryMode = new byte[1];
    private byte[] lengthOfIssuerScriptResults = new byte[2];
    private byte[] issuerScriptResults;
    private byte[] lengthOfEncipherPinData = new byte[2];
    private byte[] encipherPinData;
    private byte[] transactionStatusInformation = new byte[2];
    private byte[] lengthOfKeySerialNumber = new byte[2];
    private byte[] keySerialNumber;

    private E_FinancialTransactionRequestData[] packetSequence = new E_FinancialTransactionRequestData[] {
            E_FinancialTransactionRequestData.TID,
            E_FinancialTransactionRequestData.DATE,
            E_FinancialTransactionRequestData.TIME,
            E_FinancialTransactionRequestData.ARQC,
            E_FinancialTransactionRequestData.TYPE,
            E_FinancialTransactionRequestData.AMT_AUTH,
            E_FinancialTransactionRequestData.AMT_OTHER,
            E_FinancialTransactionRequestData.AMT,
            E_FinancialTransactionRequestData.CC,
            E_FinancialTransactionRequestData.PAN,
            E_FinancialTransactionRequestData.PAN_SN,
            E_FinancialTransactionRequestData.AIP,
            E_FinancialTransactionRequestData.ATC,
            E_FinancialTransactionRequestData.TVR,
            E_FinancialTransactionRequestData.CNTR_CODE,
            E_FinancialTransactionRequestData.UPD_NBR,
            E_FinancialTransactionRequestData.ISU_AP_DATA,
            E_FinancialTransactionRequestData.CID,
            E_FinancialTransactionRequestData.AC,
            E_FinancialTransactionRequestData.PEM,
            E_FinancialTransactionRequestData.LEN_REV_ISR,
            E_FinancialTransactionRequestData.REV_ISR,
            E_FinancialTransactionRequestData.LEN_EPIN,
            E_FinancialTransactionRequestData.EPIN,
            E_FinancialTransactionRequestData.TSI,
            E_FinancialTransactionRequestData.LEN_KSN,
            E_FinancialTransactionRequestData.KSN

    };

    private HashMap<E_FinancialTransactionRequestData, byte[]> IDByteArrayTable = new HashMap<>();



    public FinancialTransactionRequest(){

        IDByteArrayTable.put( E_FinancialTransactionRequestData.TID, terminalID);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.DATE, transactionDate);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.TIME, transactionTime);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.ARQC, authorizationRequestCryptogram);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.TYPE, transactionType);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.AMT_AUTH, authorizedAmount);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.AMT_OTHER, otherAmount);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.AMT, transactionAmount);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.CC, transactionCurrencyCode);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.PAN, applicationPAN);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.PAN_SN, applicationPANSequenceNumber);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.AIP, applicationInterchangeProfile);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.ATC, applicationTransactionCounter);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.TVR, terminalVerificationResult);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.CNTR_CODE, terminalCountryCode);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.UPD_NBR, unpredictableNumber);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.ISU_AP_DATA, issuerApplicationData);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.CID, cryptogramInformationData);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.AC, applicationCryptogram);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.PEM, POSEntryMode);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.LEN_REV_ISR, lengthOfIssuerScriptResults);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.REV_ISR, issuerScriptResults);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.LEN_EPIN, lengthOfEncipherPinData);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.EPIN, encipherPinData);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.TSI, transactionStatusInformation);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.LEN_KSN, lengthOfKeySerialNumber);
        IDByteArrayTable.put( E_FinancialTransactionRequestData.KSN, keySerialNumber);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setPacket(E_FinancialTransactionRequestData name, byte[] data) {

        byte[] entry = IDByteArrayTable.get(name);

        if(entry != null){
            if(data.length == entry.length)
                IDByteArrayTable.replace(name, data);
            else
                throw new IllegalArgumentException("length wrong");
        }
        else
            IDByteArrayTable.put(name,data);
    }


    public byte[] constructPacket() throws IOException {

        ByteArrayOutputStream byteArrayBuilder = new ByteArrayOutputStream();
        // start byte;
        byteArrayBuilder.write(0x00);

        for( int i = 0; i < packetSequence.length; i++){
            byte[] entry = IDByteArrayTable.get(packetSequence[i]);
            if(entry != null)
                byteArrayBuilder.write(entry);
        }
        // dummy end sequence
        //byteArrayBuilder.write(0x00);
        return byteArrayBuilder.toByteArray();
    }






}
