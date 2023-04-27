package com.otosone.bsscommunicator.navFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.otosone.bsscommunicator.BluetoothConnectionService;
import com.otosone.bsscommunicator.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BMSFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BMSFragment extends Fragment {

    private BluetoothConnectionService bluetoothConnectionService;

    public BMSFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BMSFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BMSFragment newInstance(String param1, String param2) {
        BMSFragment fragment = new BMSFragment();
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
        return inflater.inflate(R.layout.fragment_bms, container, false);
    }

    public void setBluetoothConnectionService(BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }
}