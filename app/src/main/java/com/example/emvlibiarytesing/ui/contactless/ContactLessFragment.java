package com.example.emvlibiarytesing.ui.contactless;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.emvlibiarytesing.tool.ConstructFinancialTransactionRequest;

import java.io.IOException;


/**
 *  ContactLessFragment is a fragment only dealing ctls transaction UI.
 *  All the data processing call back are implement in ContactLessViewModel.
 *  THe MsrFragment also need to provide callback for ContactLessViewModel.
 *  (1) start pin fragment
 *  (2) get the emvController
 *  (3) get the online port
 *
 */
public class ContactLessFragment extends Fragment implements onPinRequireCallBack, onRequireOnlinePort, PinBlockCallBack {

    private ContactLessViewModel mViewModel;
    private EditText amount;
    private EMVController controller;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.contactless_fragment, container, false);
        mViewModel = new ViewModelProvider(this).get(ContactLessViewModel.class);

        final TextView debugTextView = root.findViewById(R.id.contactless_fragment__textView_error_code);

        mViewModel.getDebugMessage().observe(getViewLifecycleOwner(), s -> {
            if(debugTextView.length() > 500)
                debugTextView.setText("");
            debugTextView.append("\n" + s);

        });

        root.findViewById(R.id.contactless_fragment__button_start).setOnClickListener(v -> {
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

        root.findViewById(R.id.contactless_fragment__button_clear).setOnClickListener(v->{
            debugTextView.setText("");
        });


        amount = root.findViewById(R.id.contactless_fragment__editText_amounts);
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
     * this is how fragment get the emvController and also set this controller to ContactLessViewModel.
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
     * This callback for ContactLessViewModel to start pin fragment
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
                navigate(R.id.action_contactlessFragment_to_pinFragment, bundle);
    }

    /**
     * this is implement onRequireOnlinePort.getOnlinePort
     * This callback for ContactLessViewModel to get online port
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onGetPinBlock(byte[] data) {

        requireActivity().runOnUiThread(()->{
                Toast.makeText(getContext(), "get back pin block!!!!",Toast.LENGTH_SHORT).show();
            });

           mViewModel.onGetPinBlockData(data);

    }


}
