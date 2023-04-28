package com.otosone.bsscommunicator.navFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.otosone.bsscommunicator.BluetoothConnectionService;
import com.otosone.bsscommunicator.ChargingItem;
import com.otosone.bsscommunicator.adapter.ChargingAdapter;
import com.otosone.bsscommunicator.databinding.FragmentChargingBinding;

import java.util.ArrayList;
import java.util.List;

public class ChargingFragment extends Fragment {

    FragmentChargingBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;

    public static ChargingFragment newInstance() {
        ChargingFragment fragment = new ChargingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChargingBinding.inflate(inflater, container, false);

        List<ChargingItem> chargingItems = new ArrayList<>();
        // Add your ChargingItem instances to the list, for example:
        chargingItems.add(new ChargingItem(false, "01", "START"));
        chargingItems.add(new ChargingItem(true, "02", "STOP"));

        ChargingAdapter chargingAdapter = new ChargingAdapter(requireContext(), chargingItems);

        ListView listView = binding.chargingListView;
        listView.setAdapter(chargingAdapter);

        return binding.getRoot();
    }

    public void setBluetoothConnectionService(BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }
}
