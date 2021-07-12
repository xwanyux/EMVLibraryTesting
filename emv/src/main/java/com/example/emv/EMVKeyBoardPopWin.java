package com.example.emv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.emv.tool.ByteArrayConverter;
import com.example.emv.tool.RSAThread;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class EMVKeyBoardPopWin extends PopupWindow implements RSAThread.RSACallBack, PinPadThread.PinPadThreadCallBack {


    private Context mContext;

    private View view;

    private Button btn_take_enter;
    private Button btn_clear;
    private Button[] buttonArray;
    private TextView textView_time_left;
    private TextView textView_state;
    private int count_time_left;
    private EMVController controller;
    private RSAThread rsaThread;
    private PinPadThread pinPadThread;
    private byte[] modulus;
    private byte[] publicKeyExponent;
    private PrivateKey privateKey;
    private String permutation;
    private byte[] randomNumber;
    private String pinPassword;
    private EMVKeyboardCallBack keyBoardCallBack;
    private int minimum_pin_length;
    private int maximum_pin_length;
    private boolean failedFlag;
    private boolean requirePinBlockFlag;


    public EMVKeyBoardPopWin(Context mContext, EMVController controller, EMVKeyboardCallBack callBack, boolean requirePinBlock) {

        failedFlag = false;
        requirePinBlockFlag = requirePinBlock;
//        minimum_pin_length = 4;
//        maximum_pin_length = 12;

        this.controller = controller;
        this.view = LayoutInflater.from(mContext).inflate(R.layout.secure_keyboard, null);
        keyBoardCallBack = callBack;

        pinPassword = "";

        buttonArray = new Button[10];

        buttonArray[0] = view.findViewById(R.id.secure_keyboard_button_position0);
        buttonArray[1] = view.findViewById(R.id.secure_keyboard_button_position1);
        buttonArray[2] = view.findViewById(R.id.secure_keyboard_button_position2);
        buttonArray[3] = view.findViewById(R.id.secure_keyboard_button_position3);
        buttonArray[4] = view.findViewById(R.id.secure_keyboard_button_position4);
        buttonArray[5] = view.findViewById(R.id.secure_keyboard_button_position5);
        buttonArray[6] = view.findViewById(R.id.secure_keyboard_button_position6);
        buttonArray[7] = view.findViewById(R.id.secure_keyboard_button_position7);
        buttonArray[8] = view.findViewById(R.id.secure_keyboard_button_position8);
        buttonArray[9] = view.findViewById(R.id.secure_keyboard_button_position9);

        textView_time_left = view.findViewById(R.id.secure_keyboard_textview_time_left);
        textView_state = view.findViewById(R.id.secure_keyboard_textview_error_state);

        textView_time_left.setText("non started");

        for(int i = 0; i < 10; i++){
            buttonArray[i].setText("???");
            buttonArray[i].setActivated(false);
        }

        btn_take_enter =  view.findViewById(R.id.secure_keyboard_button_enter);
//        btn_pick_photo = (Button) view.findViewById(R.id.btn_pick_photo);
//        btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        // 取消按钮


        btn_take_enter.setActivated(false);

        btn_clear = view.findViewById(R.id.secure_keyboard_button_delete);
        btn_clear.setOnClickListener(v->{
            if(pinPassword.length() != 0) {
                pinPassword = "";
                keyBoardCallBack.onInputLengthChange(pinPassword.length());
            }
        });



//        // 设置按钮监听
//        btn_pick_photo.setOnClickListener(itemsOnClick);
//        btn_take_photo.setOnClickListener(itemsOnClick);

        // 设置外部可点击
        //this.setOutsideTouchable(true);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
//        this.view.setOnTouchListener(new View.OnTouchListener() {
//
//            public boolean onTouch(View v, MotionEvent event) {
//
//                int height = view.findViewById(R.id.pop_layout).getTop();
//
//                int y = (int) event.getY();
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (y < height) {
//                        dismiss();
//                    }
//                }
//                return true;
//            }
//        });


        /* 设置弹出窗口特征 */
        // 设置视图
        this.setContentView(this.view);
        // 设置弹出窗体的宽和高
        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setOutsideTouchable(false);

        // 设置弹出窗体可点击
        this.setFocusable(false);

         //实例化一个ColorDrawable颜色为半透明
//        ColorDrawable dw = new ColorDrawable(0xb0000000);
//        // 设置弹出窗体的背景
//        this.setBackgroundDrawable(dw);

        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.take_photo_anim);


        rsaThread = new RSAThread(this);
        rsaThread.start();
    }


    private byte[] generateEncodeMessage(String password){


        byte[] data = new byte[randomNumber.length];

        for(int i = 0; i < data.length; i++)
            data[i] = (byte)( randomNumber[i] ^ 0xFF);
        int i,j;
        data[0] = (byte)(password.length() ^ randomNumber[0]);
        for(i = 0; i<password.length();i++){
            for(j = 0; j < permutation.length(); j++){
                if(password.charAt(i) == permutation.charAt(j)){
                    Log.d("PIN", "position"+(""+j));
                    break;
                }
            }
            data[i+1] = (byte)( (j + 0x30) ^ randomNumber[i+1]);
        }

        Log.d("PIN", ByteArrayConverter.ByteArrayToStringHex(data,data.length));
        return data;
    }


    private boolean checkValidPermutation(String permutation){

        if(permutation.length() != 10)
            return false;

        String temp = "0123456789";
        for(int i = 0; i < temp.length(); i++){
            if(!permutation.contains(temp.substring(i,i+1)))
                return false;
        }
        return true;
    }


    private void onSetKeyBoardPermutation(String permutation){

        if(checkValidPermutation(permutation)) {
            btn_take_enter.setActivated(true);
            btn_take_enter.setOnClickListener(v -> {
                // 销毁弹出框
                // decode the password
                byte[] permuted_data;
                permuted_data = generateEncodeMessage(pinPassword);
                pinPadThread = new PinPadThread(permuted_data, this, controller);
                pinPadThread.start();
                dismiss();
            });
            for (int i = 0; i < 10; i++) {
                buttonArray[i].setText(permutation.substring(i, i + 1));
                buttonArray[i].setActivated(true);
                int temp = i;
                buttonArray[i].setOnClickListener(v -> {
                    pinPassword += (permutation.substring(temp, temp + 1));
                    keyBoardCallBack.onInputLengthChange(pinPassword.length());
//                    password_editText.setText(pinPassword);
                });
            }
            setCountDownTimer("Success", 30);
        }
        else
            onFailed();

    }

    @Override
    public void onGetEncodePermutation(byte[] data) {
        //Log.d("PIN", "onGetEncodePermutation: ");
        try {
            Log.d("PIN", "onGetEncodePermutation: (original data len):" + (""+data.length));
            Log.d("PIN", "onGetEncodePermutation:(original data) " + ByteArrayConverter.ByteArrayToStringHex(data, data.length));
            @SuppressLint("GetInstance") Cipher rsaCipher = Cipher.getInstance("RSA/ECB/NoPadding", "BC");
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decryptedBytes = rsaCipher.doFinal(data);
            Log.d("PIN", "onGetEncodePermutation:(decode data) " + ByteArrayConverter.ByteArrayToStringHex(decryptedBytes, decryptedBytes.length));

            permutation = new String(Arrays.copyOfRange(decryptedBytes, 0, 10),"UTF-8");
            onSetKeyBoardPermutation(permutation);
            Log.d("PIN", "onGetEncodePermutation:(ASCII) " + permutation);

            randomNumber = Arrays.copyOfRange(decryptedBytes, 10, decryptedBytes.length);

            Log.d("PIN", "onGetEncodePermutation:(random number): " + ByteArrayConverter.ByteArrayToStringHex(randomNumber, randomNumber.length));

            //keyBoardNumber.postValue(new String(decryptedBytes, "UTF-8").substring(0,decryptedBytes.length));

        }catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onGetEncipherPinBlock(byte[] data) {
        keyBoardCallBack.onGetEncipherPinBlockResult(data);
    }

    @Override
    public void onSuccessSetPin() {
        keyBoardCallBack.onSuccessSetPin();
        if(requirePinBlockFlag){
            // start a new thread
            pinPadThread = new PinPadThread(this, controller);
            pinPadThread.start();
        }
    }


    private void setCountDownTimer(String stateMessage, int Time){
        textView_state.setText(stateMessage);
        count_time_left = Time;
        textView_time_left.setText((count_time_left + "") + "s");

        /*if you are not doing this, will cause error*/
        new Handler(Looper.getMainLooper()).post(() -> {
            new CountDownTimer(count_time_left * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    count_time_left--;
                    try {
                        if(textView_time_left != null)
                            textView_time_left.setText((count_time_left + "") + "(s)");

                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        cancel();
                    }
                }

                @Override
                public void onFinish() {
                    /*just for safety*/
                    try {
                        if(!failedFlag)
                            keyBoardCallBack.onDismiss();
                        dismiss();
                    } catch (IllegalStateException ignored) {

                    }
                }
            }.start();
        });
    }




    @Override
    public void onFailed(String command) {
        Log.d("KEY BOARD", "onFailed: ");
        if(command.equals("get permutation"))
            setCountDownTimer("Failed", 5);
        EMVThread.setImportPinStateFailed();
        keyBoardCallBack.onFailed();
        failedFlag = true;

    }


    @Override
    public void onFailed() {
        Log.d("KEY BOARD", "onFailed: ");
        setCountDownTimer("Failed", 5);
        EMVThread.setImportPinStateFailed();
        keyBoardCallBack.onFailed();
        failedFlag = true;
    }

    @Override
    public void onGetKeyPair(KeyPair keyPair) {
        privateKey = keyPair.getPrivate();
        modulus = ((RSAPublicKey)keyPair.getPublic()).getModulus().toByteArray();
        if (modulus[0] == 0) {
            byte[] tmp = new byte[modulus.length - 1];
            System.arraycopy(modulus, 1, tmp, 0, tmp.length);
            modulus = tmp;
        }
        publicKeyExponent = ((RSAPublicKey)keyPair.getPublic()).getPublicExponent().toByteArray();

        pinPadThread = new PinPadThread(modulus, publicKeyExponent, this, controller);

        pinPadThread.start();

    }
}
