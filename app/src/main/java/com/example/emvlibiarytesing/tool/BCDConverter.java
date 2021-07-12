package com.example.emvlibiarytesing.tool;

import android.util.Log;

import java.util.Arrays;

public class BCDConverter {

    // fix to 6 byte, this is the spec of BCD use in contact and contactLess
    public static byte[] convertFromString(String amount){

        if(amount.length() > 10)
            throw new IllegalArgumentException();

        byte[] BCD = new byte[6];
        int startIdx = 4;
        String newAmount;
        if(amount.length() % 2 != 0){
            newAmount = "0" + amount;
        }
        else
            newAmount = amount;

        for(int i = newAmount.length() - 1; i>= 0; i = i - 2){
            int tempIdx = i;

            BCD[startIdx] |= (byte) ( newAmount.charAt(tempIdx) - '0');
            BCD[startIdx] |= (byte) (( newAmount.charAt(tempIdx - 1) - '0') * 16);

            startIdx -= 1;

        }

        return BCD;
    }

    public static String BCDToString(byte[] bcd) {
        String AmountStr = "0";
        int startIdx = 0;
        byte[] newAmount;

        if(bcd.length != 6)
            throw new IllegalArgumentException();

        for(int i = 0 ; i < (bcd.length - 1) ; i++) {
            if(bcd[i] != 0x00) {
                startIdx = i;
                break;
            }
        }

        newAmount = Arrays.copyOfRange(bcd, startIdx, bcd.length - 1);
        AmountStr = ByteArrayConverter.ByteArrayToStringHex(newAmount, newAmount.length);
        AmountStr = AmountStr.replace(" ", "");

        if(AmountStr.indexOf('0') == 0)
            AmountStr = AmountStr.substring(1);

        return AmountStr;
    }

    public static void testCase(){
        Log.d("BCD", "testCase(8):"+ByteArrayConverter.ByteArrayToStringHex(convertFromString("8"), 6));
        Log.d("BCD", "testCase(8):" + BCDToString(convertFromString("8")));
        Log.d("BCD", "testCase(90):"+ByteArrayConverter.ByteArrayToStringHex(convertFromString("90"), 6));
        Log.d("BCD", "testCase(90):" + BCDToString(convertFromString("90")));
        Log.d("BCD", "testCase(191):"+ByteArrayConverter.ByteArrayToStringHex(convertFromString("191"), 6));
        Log.d("BCD", "testCase(191):" + BCDToString(convertFromString("191")));
        Log.d("BCD", "testCase(1913):"+ByteArrayConverter.ByteArrayToStringHex(convertFromString("1913"), 6));
        Log.d("BCD", "testCase(1913):" + BCDToString(convertFromString("1913")));
        Log.d("BCD", "testCase(19156):"+ByteArrayConverter.ByteArrayToStringHex(convertFromString("19156"), 6));
        Log.d("BCD", "testCase(19156):" + BCDToString(convertFromString("19156")));
        Log.d("BCD", "testCase(191795):"+ByteArrayConverter.ByteArrayToStringHex(convertFromString("191795"), 6));
        Log.d("BCD", "testCase(191795):" + BCDToString(convertFromString("191795")));
        Log.d("BCD", "testCase(7818941):"+ByteArrayConverter.ByteArrayToStringHex(convertFromString("7818941"), 6));
        Log.d("BCD", "testCase(7818941):" + BCDToString(convertFromString("7818941")));
    }

}
