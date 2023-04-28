package com.otosone.bsscommunicator.navFragments;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.otosone.bsscommunicator.BMSItem;
import com.otosone.bsscommunicator.BluetoothConnectionService;
import com.otosone.bsscommunicator.DoorItem;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.adapter.BMSAdapter;
import com.otosone.bsscommunicator.adapter.DoorAdapter;
import com.otosone.bsscommunicator.databinding.FragmentBmsBinding;
import com.otosone.bsscommunicator.databinding.FragmentDoorBinding;

import java.util.ArrayList;
import java.util.List;


public class BMSFragment extends Fragment {

    FragmentBmsBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bms, container, false);

        List<BMSItem> bmsItems = new ArrayList<>();
        bmsItems.add(new BMSItem(false, "01", 0));
        bmsItems.add(new BMSItem(true, "02", 0));
        bmsItems.add(new BMSItem(false, "03", 0));
        bmsItems.add(new BMSItem(true, "04", 0));
        bmsItems.add(new BMSItem(false, "05", 0));
        bmsItems.add(new BMSItem(true, "06", 0));
        // Add more items as needed

        // Initialize the DoorItemAdapter
        BMSAdapter bmsAdapter = new BMSAdapter(requireContext(), bmsItems);

        // Bind the DoorItemAdapter to the ListView
        ListView listView = binding.bmsListView;
        listView.setAdapter(bmsAdapter);


        return binding.getRoot();
    }

    public void setBluetoothConnectionService(BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }
}