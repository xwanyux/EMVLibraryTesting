package com.example.emvlibiarytesing.ui.msr;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comwrapper.SerialDeviceWrapper;
import com.example.emv.EMVController;
import com.example.emvlibiarytesing.ActivityGetControllerCallBack;
import com.example.emvlibiarytesing.ActivityGetOnlinePortCallBack;
import com.example.emvlibiarytesing.ActivityPinBlockCallBack;
import com.example.emvlibiarytesing.PinBlockCallBack;
import com.example.emvlibiarytesing.R;
import com.example.emvlibiarytesing.ShareViewModel;
import com.example.emvlibiarytesing.onPinRequireCallBack;
import com.example.emvlibiarytesing.onRequireOnlinePort;
import com.example.emvlibiarytesing.ui.contactless.ContactLessViewModel;

import java.io.IOException;


/**
 *  MsrFragment is a fragment only dealing msr transaction UI.
 *  All the data processing call back are implement in MsrViewModel.
 *  THe MsrFragment also need to provide callback for MsrViewModel.
 *  (1) start pin fragment
 *  (2) get the emvController
 *  (3) get the online port
 *
 */
public class MsrFragment extends Fragment implements onPinRequireCallBack, onRequireOnlinePort, PinBlockCallBack {

    private MsrViewModel mViewModel;
    private EditText amount;
    private EMVController controller;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.msr_fragment, container, false);

        mViewModel = new ViewModelProvider(this).get(MsrViewModel.class);

        final TextView debugTextView = root.findViewById(R.id.msr_fragment__textView_error_code);

        mViewModel.getDebugMessage().observe(getViewLifecycleOwner(), s -> {
            if(debugTextView.length() > 500)
                debugTextView.setText("");
            //debugTextView.setText(s);
            debugTextView.append("\n" + s);

        });

        root.findViewById(R.id.msr_fragment__button_start).setOnClickListener(v -> {
            if(amount.getText().length() == 0)
                getActivity().runOnUiThread(()-> {
                    Toast.makeText(getContext(), "please input amount!!",Toast.LENGTH_SHORT).show();
                });
            else {
                try {
                    mViewModel.startTransaction(amount.getText().toString());
                    debugTextView.setText("\nTransaction start");
                } catch (IOException e) {
                    debugTextView.setText("\nTransaction Failed");
                }
            }

        });

        root.findViewById(R.id.msr_fragment__button_clear).setOnClickListener(v->{
            debugTextView.setText("");
        });


        amount = root.findViewById(R.id.msr_fragment__editText_amounts);
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 1 && s.toString().startsWith("0")) {
                    s.clear();
                }
            }
        });

        mViewModel.setRequirePinCallBack(this);
        mViewModel.setOnlinePortCallBack(this);

        return root;
    }

    /**
     * this is how fragment get the emvController and also set this controller to msrViewModel
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
        if(getActivity() instanceof ActivityGetControllerCallBack) {
            controller = ((ActivityGetControllerCallBack) getActivity()).getController();
            mViewModel.setEmvController(controller);
        }
    }

    /**
     * this is implement onPinRequireCallBack.startRequirePinProcess()
     * This callback for msrViewModel to start pin fragment
     * We implement as
     * (1)set the PinBlockCallBack by activity (we need pin block perform by emvKeyboard)
     * (2)set the bundle value, key PinBlock , ture (tell emv Fragment to do perform generate pin block)
     * (3)start the pin fragment
     */
    @Override
    public void startRequirePinProcess() {
        Bundle bundle = new Bundle();
        ((ActivityPinBlockCallBack)requireActivity()).setPinBlockCallBack(this);
        bundle.putBoolean("PinBlock", true);
        NavHostFragment.findNavController(this).
                navigate(R.id.action_msrFragment_to_pinFragment, bundle);
    }

    /**
     * this is implement onRequireOnlinePort.getOnlinePort
     * This callback for msrViewModel to get online port
     * we implement as get the online port by activityCallBack
     * @return
     */
    @Override
    public SerialDeviceWrapper getOnlinePort() {
        FragmentActivity activity = getActivity();
        if(activity != null ){
            return ((ActivityGetOnlinePortCallBack)activity).getOnlinePort();
        }
        return null;
    }

    /**
     *  this is implement PinBlockCallBack.onGetPinBlock
     *  This callBack for get back the pin block data
     *  (the mViewModel.onGetPinBlockData should perform send result to online)
     * @param data
     */
    @Override
    public void onGetPinBlock(byte[] data) {
        requireActivity().runOnUiThread(()->{
            Toast.makeText(getContext(), "get back pin block!!!!",Toast.LENGTH_SHORT).show();
        });

        mViewModel.onGetPinBlockData(data);
    }
}
