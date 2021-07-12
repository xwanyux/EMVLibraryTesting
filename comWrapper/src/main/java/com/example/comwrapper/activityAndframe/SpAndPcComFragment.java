package com.example.comwrapper.activityAndframe;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public abstract class SpAndPcComFragment extends Fragment {

    private ActivitySPComCallBack spCallBackListener;
    private ActivityPCComCallBack pcCallBackListener;


    protected ActivitySPComCallBack getSpCallBackListener(){return spCallBackListener;}
    protected ActivityPCComCallBack getPcCallBackListener(){return pcCallBackListener;}

    protected abstract int getSPBaudRate();
    protected abstract int getPCBaudRate();


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //getActivity() is fully created in onActivityCreated and instanceOf differentiate it between different Activities
        if (getActivity() instanceof ActivitySPComCallBack)
            spCallBackListener = (ActivitySPComCallBack) getActivity();

        if (getActivity() instanceof ActivityPCComCallBack)
            pcCallBackListener = (ActivityPCComCallBack) getActivity();


        if(spCallBackListener != null) {
            try {
                spCallBackListener.connectSPCom(getSPBaudRate());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(pcCallBackListener != null) {
            try {
                pcCallBackListener.connectPCCom(getPCBaudRate());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}


