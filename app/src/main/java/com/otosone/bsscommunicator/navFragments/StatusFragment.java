package com.otosone.bsscommunicator.navFragments;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.otosone.bsscommunicator.BluetoothConnectionService;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.databinding.FragmentStatusBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatusFragment extends Fragment {
    private BluetoothConnectionService bluetoothConnectionService;
    private LinearLayout layout1, layout2, layout3, layout4, layout5, layout6, layout7, layout8, layout9, layout10, layout11, layout12, layout13, layout14, layout15, layout16;
    private TextView charger1Tv, charger2Tv, charger3Tv, charger4Tv, charger5Tv, charger6Tv, charger7Tv, charger8Tv, charger9Tv, charger10Tv, charger11Tv, charger12Tv, charger13Tv, charger14Tv, charger15Tv, charger16Tv;
    private TextView isLock1Tv, isLock2Tv, isLock3Tv, isLock4Tv, isLock5Tv, isLock6Tv, isLock7Tv, isLock8Tv, isLock9Tv, isLock10Tv, isLock11Tv, isLock12Tv, isLock13Tv, isLock14Tv, isLock15Tv, isLock16Tv;
    FragmentStatusBinding binding;


    // TODO: Rename and change types and number of parameters
    public static StatusFragment newInstance(String param1, String param2) {
        StatusFragment fragment = new StatusFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_status, container, false);
        View root = binding.getRoot();
        Databind();

        return root;

    }

    private void Databind() {
        layout1 = binding.layout1; layout2 = binding.layout2; layout3 = binding.layout3; layout4 = binding.layout4; layout5 = binding.layout5; layout6 = binding.layout6; layout7 = binding.layout7; layout8 = binding.layout8; layout9 = binding.layout9;
        layout10 = binding.layout10; layout11 = binding.layout11; layout12 = binding.layout12; layout13 = binding.layout13; layout14 = binding.layout14; layout15 = binding.layout15; layout16 = binding.layout16;
        charger1Tv = binding.charger1Tv; charger2Tv = binding.charger2Tv; charger3Tv = binding.charger3Tv; charger4Tv = binding.charger4Tv; charger5Tv = binding.charger5Tv;
        charger6Tv = binding.charger6Tv; charger7Tv = binding.charger7Tv; charger8Tv = binding.charger8Tv; charger9Tv = binding.charger9Tv; charger10Tv = binding.charger10Tv;
        charger11Tv = binding.charger11Tv; charger12Tv = binding.charger12Tv; charger13Tv = binding.charger13Tv; charger14Tv = binding.charger14Tv; charger15Tv = binding.charger15Tv; charger16Tv = binding.charger16Tv;
        isLock1Tv = binding.isLock1Tv; isLock2Tv = binding.isLock2Tv; isLock3Tv = binding.isLock3Tv; isLock4Tv = binding.isLock4Tv; isLock5Tv = binding.isLock5Tv; isLock6Tv = binding.isLock6Tv;
        isLock7Tv = binding.isLock7Tv; isLock8Tv = binding.isLock8Tv; isLock9Tv = binding.isLock9Tv; isLock10Tv = binding.isLock10Tv; isLock11Tv = binding.isLock11Tv; isLock12Tv = binding.isLock12Tv;
        isLock13Tv = binding.isLock13Tv; isLock14Tv = binding.isLock14Tv; isLock15Tv = binding.isLock15Tv; isLock16Tv = binding.isLock16Tv;
    }

    public void setBluetoothConnectionService(BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }
}