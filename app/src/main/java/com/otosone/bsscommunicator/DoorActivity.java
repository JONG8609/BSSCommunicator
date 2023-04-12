package com.otosone.bsscommunicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.ListView;

import com.otosone.bsscommunicator.adapter.ChargingAdapter;
import com.otosone.bsscommunicator.adapter.DoorAdapter;
import com.otosone.bsscommunicator.databinding.ActivityChargingBinding;
import com.otosone.bsscommunicator.databinding.ActivityDoorBinding;

import java.util.ArrayList;
import java.util.List;

public class DoorActivity extends AppCompatActivity {

    private ListView listView;
    private DoorAdapter adapter;
    private List<DoorItem> itemList;
    private ActivityDoorBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Databinding();

        itemList = new ArrayList<>();

        adapter = new DoorAdapter(this, itemList);
        itemList.add(new DoorItem(false, "111", "222"));
        itemList.add(new DoorItem(false, "222", "222"));
        itemList.add(new DoorItem(false, "333", "222"));
        itemList.add(new DoorItem(true, "444", "222"));
        itemList.add(new DoorItem(false, "555", "222"));
        listView.setAdapter(adapter);
    }

    private void Databinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_door);
        listView = binding.doorListView;
    }
}