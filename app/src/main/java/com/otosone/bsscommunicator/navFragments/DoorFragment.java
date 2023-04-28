package com.otosone.bsscommunicator.navFragments;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.otosone.bsscommunicator.BluetoothConnectionService;
import com.otosone.bsscommunicator.DoorItem;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.adapter.DoorAdapter;
import com.otosone.bsscommunicator.databinding.FragmentDoorBinding;

import java.util.ArrayList;
import java.util.List;


public class DoorFragment extends Fragment {

    FragmentDoorBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    public static DoorFragment newInstance(String param1, String param2) {
        DoorFragment fragment = new DoorFragment();
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_door, container, false);

        // Create a list of DoorItems
        List<DoorItem> doorItems = new ArrayList<>();
        doorItems.add(new DoorItem(false, "01", "LOCK"));
        doorItems.add(new DoorItem(true, "02", "LOCK"));
        doorItems.add(new DoorItem(false, "03", "LOCK"));
        doorItems.add(new DoorItem(true, "04", "LOCK"));
        doorItems.add(new DoorItem(false, "05", "LOCK"));
        doorItems.add(new DoorItem(true, "06", "LOCK"));
        doorItems.add(new DoorItem(false, "08", "LOCK"));
        doorItems.add(new DoorItem(true, "09", "LOCK"));
        doorItems.add(new DoorItem(false, "10", "LOCK"));
        doorItems.add(new DoorItem(true, "11", "LOCK"));
        doorItems.add(new DoorItem(false, "12", "LOCK"));
        doorItems.add(new DoorItem(true, "13", "LOCK"));
        doorItems.add(new DoorItem(true, "14", "LOCK"));
        doorItems.add(new DoorItem(false, "15", "LOCK"));
        doorItems.add(new DoorItem(true, "16", "LOCK"));
        // Add more items as needed

        // Initialize the DoorItemAdapter
        DoorAdapter doorAdapter = new DoorAdapter(requireContext(), doorItems);

        // Bind the DoorItemAdapter to the ListView
        ListView listView = binding.doorListView;
        listView.setAdapter(doorAdapter);


        return binding.getRoot();
    }

    public void setBluetoothConnectionService(BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }
}