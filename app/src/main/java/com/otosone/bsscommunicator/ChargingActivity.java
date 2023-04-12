package com.otosone.bsscommunicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.ListView;

import com.otosone.bsscommunicator.adapter.ChargingAdapter;
import com.otosone.bsscommunicator.databinding.ActivityChargingBinding;

import java.util.ArrayList;
import java.util.List;

public class ChargingActivity extends AppCompatActivity {

    private ListView listView;
    private ChargingAdapter adapter;
    private List<ChargingItem> itemList;
    private ActivityChargingBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Databinding();

        itemList = new ArrayList<>();

        adapter = new ChargingAdapter(this, itemList);
        itemList.add(new ChargingItem(false, "111", "333"));
        itemList.add(new ChargingItem(false, "222", "333"));
        itemList.add(new ChargingItem(false, "333", "333"));
        itemList.add(new ChargingItem(false, "444", "333"));

        listView.setAdapter(adapter);
    }

    private void Databinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_charging);
        listView = binding.chargingListView;
    }
}