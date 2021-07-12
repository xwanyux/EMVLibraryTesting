package com.example.emvlibiarytesing.ui.icc;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comwrapper.SerialDeviceWrapper;
import com.example.emv.EMVController;
import com.example.emv.EMVKeyboardCallBack;
import com.example.emvlibiarytesing.ActivityGetControllerCallBack;
import com.example.emvlibiarytesing.ActivityGetOnlinePortCallBack;
import com.example.emvlibiarytesing.PinBlockCallBack;
import com.example.emvlibiarytesing.R;
import com.example.emvlibiarytesing.ShareViewModel;
import com.example.emvlibiarytesing.onPinRequireCallBack;
import com.example.emvlibiarytesing.onRequireOnlinePort;
import com.example.emvlibiarytesing.tool.ByteArrayConverter;

import java.io.IOException;

public class IccFragment extends Fragment implements onPinRequireCallBack, onRequireOnlinePort{



    private IccViewModel mViewModel;

    private EMVController controller;
    private EditText amount;
    private Button send;
    private View root;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        root  = inflater.inflate(R.layout.icc_fragment, container, false);
        mViewModel = new ViewModelProvider(this).get(IccViewModel.class);


        final TextView debugTextView = root.findViewById(R.id.icc_fragment_textView_error_code);

        mViewModel.getDebugMessage().observe(getViewLifecycleOwner(), s -> {
            if(debugTextView.length() > 500)
                debugTextView.setText("");
            //debugTextView.setText(s);
            debugTextView.append("\n" + s);
        });

        root.findViewById(R.id.icc_fragment_button_clear).setOnClickListener(v->{
            debugTextView.setText("");
        });


        send = root.findViewById(R.id.icc_fragment_button_start);
        send.setOnClickListener(v->{
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



        amount = root.findViewById(R.id.icc_fragment_editText_amounts);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
        if(getActivity() instanceof ActivityGetControllerCallBack) {
            controller = ((ActivityGetControllerCallBack) getActivity()).getController();
            mViewModel.setEmvController(controller);
        }
    }




    @Override
    public void startRequirePinProcess() {
        NavHostFragment.findNavController(this).
                navigate(R.id.action_iccFragment_to_pinFragment);
    }

    @Override
    public SerialDeviceWrapper getOnlinePort() {
        FragmentActivity activity = getActivity();
        if(activity != null ){
            return ((ActivityGetOnlinePortCallBack)activity).getOnlinePort();
        }
        return null;
    }
}
