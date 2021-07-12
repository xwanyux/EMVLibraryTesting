package com.example.emv.tool;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class TLVProcessing {
    public static class Tag {
        public byte[] tag;
        public byte[] length;
        public byte[] value;

        public Tag(byte[] tag, byte[] length, byte[] value) {
            this.tag = tag;
            this.length = length;
            this.value = value;
        }
    }

    public static int lenOfT = 0;
    public static int lenOfL = 0;
    public static int lenOfV = 0;

    private boolean getBerLen(byte[] iptDataOfL) {
        int len1 = 0;
        int len2 = 0;
        int index = 0;

        if(iptDataOfL == null) {
            lenOfL = 0;
            lenOfV = 0;
            return false;
        }

        lenOfL = 1;
        len1 = iptDataOfL[index++];

        if((len1 & 0x80) != 0) {    //chained length field?
            switch(len1 & 0x7F) {
                case 0x01:  //1-byte length
                    len1 = iptDataOfL[index++];
                    len2 = 0;
                    lenOfL = 2;
                    break;
                case 0x02:  //2-byte length
                    len2 = iptDataOfL[index++];
                    len1 = iptDataOfL[index++];
                    lenOfL = 3;
                    break;
                default:    //out of spec
                    return false;
            }
        }
        else
            len2 = 0;

        lenOfV = len2 * 256 + len1;
        return true;
    }

    public int getTLVLengthOfT(byte[] iptDataOfT) {
        int countIndex = 0;

        lenOfT = 0;

        if(iptDataOfT == null)
            return lenOfT;

        if(iptDataOfT[0] != 0) {
            for(countIndex = 0 ; countIndex < 256 ; countIndex++) {
                if(countIndex == 0) {
                    if((iptDataOfT[0] & 0x1F) != 0x1F) {
                        //First Byte & No subsequent bytes
                        lenOfT = 1;
                        break;
                    }
                }
                else {
                    if((iptDataOfT[countIndex] & 0x80) == 0x00) {
                        //Last tag byte
                        lenOfT = countIndex + 1;
                        break;
                    }
                }
            }
        }

        return lenOfT;
    }

    public int getTLVLengthOfL(byte[] iptDataOfL) {
        if(getBerLen(iptDataOfL))
            return lenOfL;
        else
            return 0;
    }

    public int getTLVLengthOfV(byte[] iptDataOfL) {
        if(getBerLen(iptDataOfL))
            return lenOfV;
        else
            return 0;
    }

    public TLVProcessing getTLVLength(byte[] iptDataOfT) {
        TLVProcessing tlvProcessing = new TLVProcessing();

        lenOfT = getTLVLengthOfT(iptDataOfT);

        if(!getBerLen(Arrays.copyOfRange(iptDataOfT, lenOfT, iptDataOfT.length))) {
            lenOfL = 0;
            lenOfV = 0;
        }
        return tlvProcessing;
    }

    public ArrayList<Tag> parseTLVList(byte[] iptData) {
        byte[] tagArray;
        byte[] lengthArray;
        byte[] valueArray;
        byte[] tempData;
        int index = 0;
        int parseLen = 0;

        ArrayList<Tag> tlvList = new ArrayList<>();

        if(iptData == null) {
            tlvList.clear();
            return tlvList;
        }

        while(parseLen < iptData.length) {
            //Tag
            tempData = Arrays.copyOfRange(iptData, index, iptData.length);
            getTLVLengthOfT(tempData);
            tagArray = Arrays.copyOfRange(iptData, index, index + lenOfT);
            index += lenOfT;
            parseLen += lenOfT;

            //Length
            tempData = Arrays.copyOfRange(iptData, index, iptData.length);
            getTLVLengthOfL(tempData);
            lengthArray = Arrays.copyOfRange(iptData, index, index + lenOfL);
            index += lenOfL;
            parseLen += lenOfL;

            //Value
            getTLVLengthOfV(tempData);
            valueArray = Arrays.copyOfRange(iptData, index, index + lenOfV);
            index += lenOfV;
            parseLen += lenOfV;

            tlvList.add(new Tag(tagArray, lengthArray, valueArray));
        }

        for(int i = 0 ; i < tlvList.size() ; i++) {
            Log.d("TLV",
                 "Tag: " + ByteArrayConverter.ByteArrayToStringHex(tlvList.get(i).tag, tlvList.get(i).tag.length) +
                      "Length: " + ByteArrayConverter.ByteArrayToStringHex(tlvList.get(i).length, tlvList.get(i).length.length) +
                      "Value: " + ByteArrayConverter.ByteArrayToStringHex(tlvList.get(i).value, tlvList.get(i).value.length));
        }

        return tlvList;
    }
}
