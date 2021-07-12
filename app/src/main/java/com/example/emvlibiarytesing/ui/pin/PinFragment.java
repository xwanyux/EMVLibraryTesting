package com.example.emvlibiarytesing.ui.pin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.emv.EMVController;
import com.example.emv.EMVKeyboardCallBack;
import com.example.emvlibiarytesing.ActivityGetControllerCallBack;
import com.example.emvlibiarytesing.ActivityPinBlockCallBack;
import com.example.emvlibiarytesing.R;


/**
 *   this PinFragment is to display EMV keyboard in a stand along fragment
 *
 *   this fragment have one parameter
 *   bundle.putBoolean("PinBlock", true); (from the fragment call pinFragment)
 *   if this value is true, the EMV keyboard will compute the pin block when send the permuted pin.
 *   and use callback to send back the pin block result by activity call back function.
 *
 *
 */
public class PinFragment extends Fragment implements  EMVKeyboardCallBack {

    private EMVController controller;
    private EditText password_editText;
    private View root;
    private boolean requirePinBlock;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if(getArguments() != null){
            requirePinBlock = getArguments().getBoolean("PinBlock");
        }
        else
            requirePinBlock = false;


        root  = inflater.inflate(R.layout.pin_fragment, container, false);

        /* this is set for editText don't show android keyboard when click the editText*/
        password_editText = root.findViewById(R.id.pin_fragment_editText_password);
        password_editText.setInputType(InputType.TYPE_NULL);
        password_editText.setTransformationMethod(new PasswordTransformationMethod());
        password_editText.setTextIsSelectable(false);

        return root;
    }



    @Override
    public void onResume(){
        super.onResume();

    }

    /**
     *  You need to closeKeyBoard when you leave this fragment.
     *  or your application will crash.
     */
    @Override
    public void onPause(){
        super.onPause();
        controller.closeKeyBoard();
    }


    /**
     *  This is how we get the emv controller by fragment.
     *  This is also an example how to call the emv keyboard from a fragment
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
        if(getActivity() instanceof ActivityGetControllerCallBack) {
            controller = ((ActivityGetControllerCallBack) getActivity()).getController();
            controller.showEMVKeyBoard(requireActivity(),root,this,requirePinBlock);
        }
    }

    /**
     *  This is implement EMVKeyboardCallBack onInputLengthChange
     *  We implement as display the * in the editText
     * @param numKey      current input key length
     */
    @Override
    public void onInputLengthChange(int numKey) {

        String temp = "";

        for(int i = 0; i < numKey; i++)
            temp += "*";

        String finalTemp = temp;
        requireActivity().runOnUiThread(()->{
            password_editText.setText(finalTemp);
        });

    }

    /**
     *  This is implement EMVKeyboardCallBack onSuccessSetPin()
     *  We implement as just leaving this fragment, if we don't need pin block
     */
    @Override
    public void onSuccessSetPin() {

        if(!requirePinBlock)
            requireActivity().onBackPressed();
    }

    /**
     * This is implement EMVKeyboardCallBack onGetEncipherPinBlockResult()
     * We implement as, get the PinBlockCallBack from the activity, and send the pinBlock data by this callBack.
     * Then leave the fragment.
     * (note: the fragment need the pin block result should set up the pin block callback by activity)
     * @param encipherPinBlock
     */
    @Override
    public void onGetEncipherPinBlockResult(byte[] encipherPinBlock) {

        ((ActivityPinBlockCallBack)requireActivity()).runPinBlockCallBack(encipherPinBlock);
        requireActivity().onBackPressed();
    }

    /**
     *  This is implement EMVKeyboardCallBack onFailed()
     *  We implement as show Toast key board failed. Then leave the fragment
     *
     */
    @Override
    public void onFailed() {

        requireActivity().runOnUiThread(
                ()->{Toast.makeText(getContext(), "key board failed",Toast.LENGTH_SHORT).show();}
        );

        requireActivity().onBackPressed();
    }

    /**
     *  This is implement EMVKeyboardCallBack onDismiss(),
     *  We implement as show toast of keyboard not click send. Then leave the fragment.
     *
     */
    @Override
    public void onDismiss() {

        requireActivity().runOnUiThread(
                ()->{Toast.makeText(getContext(), "not send result",Toast.LENGTH_SHORT).show();}
        );

        requireActivity().onBackPressed();
    }

}
