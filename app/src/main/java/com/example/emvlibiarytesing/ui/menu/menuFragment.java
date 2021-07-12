package com.example.emvlibiarytesing.ui.menu;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.emvlibiarytesing.R;


/**
 *  this is start fragment of activity. It has three button MSR, ICC, CTLS.
 */
public class menuFragment extends Fragment {



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root =  inflater.inflate(R.layout.menu_fragment, container, false);

        root.findViewById(R.id.menu_fragment_button_contactless).setOnClickListener(v->{
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_menuFragment_to_contactlessFragment);
        });

        root.findViewById(R.id.menu_fragment_button_icc).setOnClickListener(v->{
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_menuFragment_to_iccFragment);
        });

        root.findViewById(R.id.menu_fragment_button_msr).setOnClickListener(v->{
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_menuFragment_to_msrFragment);
        });

        root.findViewById(R.id.menu_fragment_button_get_rtc_time).setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_menuFragment_to_rtcFragment);
        });

        return root;
    }



}
