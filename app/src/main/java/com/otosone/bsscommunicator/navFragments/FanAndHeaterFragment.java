package com.otosone.bsscommunicator.navFragments;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.otosone.bsscommunicator.BluetoothConnectionService;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.databinding.FragmentFanAndHeaterBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FanAndHeaterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FanAndHeaterFragment extends Fragment {

    FragmentFanAndHeaterBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;

    public FanAndHeaterFragment() {
        // Required empty public constructor
    }


    public static FanAndHeaterFragment newInstance(String param1, String param2) {
        FanAndHeaterFragment fragment = new FanAndHeaterFragment();
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_fan_and_heater, container, false);
        return binding.getRoot();
    }

    public void setBluetoothConnectionService(BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }
}