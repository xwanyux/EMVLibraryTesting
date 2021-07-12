package com.example.emvlibiarytesing;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ShareViewModel extends ViewModel {


    /*will be use to send encrypted pin block for msr or contactLess*/
    public MutableLiveData<byte[]> item = new MutableLiveData<>();

    public byte[] getItem(){
        return item.getValue();
    }

    public void postItem(byte[] data){
        item.postValue(data);
        //item.setValue(data);
    }

}
