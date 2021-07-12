package com.example.emvlibiarytesing;

public interface ActivityPinBlockCallBack {



    // use for fragment need to get back pin block
    void setPinBlockCallBack(PinBlockCallBack callBack);

    // use for pin fragment to call this call back function
    void runPinBlockCallBack(byte[] data);



}
