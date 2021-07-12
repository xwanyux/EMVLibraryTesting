package com.example.comwrapper.activityAndframe;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public abstract class SpComOnlyFragment extends Fragment {

    private ActivitySPComCallBack spCallBackListener;


    protected ActivitySPComCallBack getSpCallBackListener(){
        return spCallBackListener;
    }

    protected abstract int getSPBaudRate();


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof ActivitySPComCallBack)
            spCallBackListener = (ActivitySPComCallBack) getActivity();

        if(spCallBackListener != null) {
            try {
                spCallBackListener.connectSPCom(getSPBaudRate());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
