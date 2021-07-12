package com.example.emv;

public interface EMVKeyboardCallBack {



    /**
     *  This call back is call when input of key change
     * @param numKey      current input key length
     */
    void onInputLengthChange(int numKey);

    /**
     *  This call back is call when send is click and the result is success
     */
    void onSuccessSetPin();

    /**
     * This call back is call only when you set the require pin block parameter is set.
     * This call back also need to first
     * @param encipherPinBlock
     */
    void onGetEncipherPinBlockResult(byte[] encipherPinBlock);

    /**
     *  onFailed failed can happened in three case
     *  (1) generate random keyboard failed
     *  (2) send the encode pin password failed
     *  (3) get the pin block failed
     */
    void onFailed();
    /**
     *  onDismiss only happened when the keyboard timeOut
     *  if any failed occur, this call back will not be called
     */
    void onDismiss();


//    void onPasswordTooShort();
//    void onPasswordTooLong();


}
