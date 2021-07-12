package com.example.emv.tool;

import java.io.UnsupportedEncodingException;

public class ByteArrayConverter {

    static public String ByteArrayToStringAscii(byte[] array, int size){

        try{
            return new String(array, "UTF-8").substring(0,size);
        }
        catch (UnsupportedEncodingException e){
            return "";
        }
    }


    static public String ByteArrayToStringHex(byte[] array, int size) {

        char[] convert_array = new char[size * 3];
        byte temp_byte;
        int temp_unsigned_byte, temp;
        for (int i = 0; i < size; i++) {

            temp_byte = array[i];
            // convert signed byte to it's unsigned value
            temp_unsigned_byte = (temp_byte >= 0x00)?(int) temp_byte : (256 + temp_byte);

            convert_array[3*i + 2] = ' ';
            temp = temp_unsigned_byte/16;
            //Log.d("DEBUG", (i +"") + (temp+""));
            convert_array[3*i] = (char)((temp < 10)? temp + '0' : temp - 10 + 'A');
            temp = temp_unsigned_byte%16;
            convert_array[3*i+1] = (char)((temp < 10)? temp + '0' : temp - 10 + 'A');

        }
        return new String(convert_array);

    }


    static public String ByteArrayToStringAscii_withNextLine(byte[] array, int split_number, int size){

            String ascii = ByteArrayToStringAscii(array, size);

            return  StringSplitWithSeparator(ascii, split_number, "\n");

    }

    static public String ByteArrayToStringHex_withNextLine(byte[] array, int split_number, int size){

        String hex_string = ByteArrayToStringHex(array, size);

        return StringSplitWithSeparator(hex_string, 3 *split_number, "\n");

    }

    private static String StringSplitWithSeparator(String string, int split_number, String separator){

        String string_with_separator = "";
        String temp;

        for(int i = 0; i < string.length()/split_number + 1; i++){

            if(split_number * (i+1) <= string.length())
                temp = string.substring(split_number*i, split_number * (i+1));
            else
                temp = string.substring(split_number * i);

            string_with_separator += temp + separator;

        }

        return  string_with_separator;
    }

}
