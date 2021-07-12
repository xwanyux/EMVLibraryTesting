package com.example.emvlibiarytesing.ui.rtc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.emv.EMVController;
import com.example.emvlibiarytesing.ActivityGetControllerCallBack;
import com.example.emvlibiarytesing.R;

import java.io.IOException;
import java.util.Calendar;

public class RtcFragment extends Fragment {

    private RtcViewModel mViewModel;
    private EMVController controller;
    private Context context;

    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.rtc_fragment, container, false);
        mViewModel = new ViewModelProvider(this).get(RtcViewModel.class);

        TextView title = root.findViewById(R.id.textView_title);
        TextView rtcTime = root.findViewById(R.id.textView_rtc_time);
        TextView title2 = root.findViewById(R.id.textView_title2);
        TextView setDateTime = root.findViewById(R.id.textView_set_date_time);
        TextView message = root.findViewById(R.id.textView_message);

        root.findViewById(R.id.rtc_fragment_button_get).setOnClickListener(v -> {
            try {
                rtcTime.setText(mViewModel.getRtcTime());
                title.setText("Current date and time");
                title.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                title.setText(e.getMessage());
                title.setVisibility(View.VISIBLE);
                rtcTime.setText("");
                e.printStackTrace();
            }
        });

        root.findViewById(R.id.rtc_fragment_button_set_date).setOnClickListener(v -> {
            StringBuilder inputDateTime = new StringBuilder();
            StringBuilder selectedDateTime = new StringBuilder();

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    inputDateTime.append(year);
                    selectedDateTime.append(year).append("/");

                    //month is 0 based
                    if((month + 1) < 10) {
                        inputDateTime.append("0").append(month + 1);
                        selectedDateTime.append("0").append(month + 1).append("/");
                    }
                    else {
                        inputDateTime.append(month + 1);
                        selectedDateTime.append(month + 1).append("/");
                    }

                    if(dayOfMonth < 10) {
                        inputDateTime.append("0").append(dayOfMonth);
                        selectedDateTime.append("0").append(dayOfMonth);
                    }
                    else {
                        inputDateTime.append(dayOfMonth);
                        selectedDateTime.append(dayOfMonth);
                    }

                    timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            selectedDateTime.append(" ");

                            if(hourOfDay < 10) {
                                inputDateTime.append("0").append(hourOfDay);
                                selectedDateTime.append("0").append(hourOfDay).append(":");
                            }
                            else {
                                inputDateTime.append(hourOfDay);
                                selectedDateTime.append(hourOfDay).append(":");
                            }

                            if(minute < 10) {
                                inputDateTime.append("0").append(minute);
                                selectedDateTime.append("0").append(minute).append(":");
                            }
                            else {
                                inputDateTime.append(minute);
                                selectedDateTime.append(minute).append(":");
                            }

                            inputDateTime.append("00");
                            selectedDateTime.append("00");

                            title2.setVisibility(View.VISIBLE);
                            setDateTime.setText(selectedDateTime.toString());
                            Log.d("Set RTC Time", inputDateTime.toString());
                            Log.d("Set RTC Time", selectedDateTime.toString());

                            try {
                                mViewModel.setRtcTime(inputDateTime.toString());
                                message.setText("Set RTC success");
                            } catch (IOException e) {
                                message.setText(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }, hour, minute, true);
                    timePickerDialog.show();
                }
            }, year, month, day);
            datePickerDialog.show();
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getActivity() instanceof ActivityGetControllerCallBack) {
            controller = ((ActivityGetControllerCallBack) getActivity()).getController();
            mViewModel.setEmvController(controller);
        }

        context = this.getContext();
    }
}