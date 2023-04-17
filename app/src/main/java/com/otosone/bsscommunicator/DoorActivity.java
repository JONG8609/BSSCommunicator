package com.otosone.bsscommunicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.material.navigation.NavigationView;
import com.otosone.bsscommunicator.adapter.ChargingAdapter;
import com.otosone.bsscommunicator.adapter.DoorAdapter;
import com.otosone.bsscommunicator.databinding.ActivityChargingBinding;
import com.otosone.bsscommunicator.databinding.ActivityDoorBinding;

import java.util.ArrayList;
import java.util.List;

public class DoorActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ListView listView;
    private DoorAdapter adapter;
    private List<DoorItem> itemList;
    private ActivityDoorBinding binding;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    ImageView doorMenuIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Databinding();

        navigationView.setNavigationItemSelectedListener(this);

        doorMenuIv.setOnClickListener(v -> openNavigationDrawer());

        itemList = new ArrayList<>();

        adapter = new DoorAdapter(this, itemList);
        itemList.add(new DoorItem(false, "1", ""));
        itemList.add(new DoorItem(false, "2", ""));
        itemList.add(new DoorItem(false, "3", ""));
        itemList.add(new DoorItem(true, "4", ""));
        itemList.add(new DoorItem(false, "5", ""));
        itemList.add(new DoorItem(false, "6", ""));
        itemList.add(new DoorItem(false, "7", ""));
        itemList.add(new DoorItem(true, "8", ""));
        itemList.add(new DoorItem(false, "9", ""));
        itemList.add(new DoorItem(false, "10", ""));
        itemList.add(new DoorItem(false, "11", ""));
        itemList.add(new DoorItem(true, "12", ""));
        itemList.add(new DoorItem(false, "13", ""));
        itemList.add(new DoorItem(false, "14", ""));
        itemList.add(new DoorItem(false, "15", ""));
        itemList.add(new DoorItem(true, "16", ""));
        listView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d("Drawer", "Navigation item selected");
        Intent intent;

        switch (item.getItemId()) {
            case R.id.nav_status:
                intent = new Intent(this, StatusActivity.class);
                break;
            case R.id.nav_reset:
                intent = new Intent(this, ResetActivity.class);
                break;
            case R.id.nav_charging:
                intent = new Intent(this, ChargingActivity.class);
                break;
            case R.id.nav_door:
                intent = new Intent(this, DoorActivity.class);
                break;
            case R.id.nav_station:
                intent = new Intent(this, StationActivity.class);
                break;
            case R.id.nav_fan_and_heater:
                intent = new Intent(this, FanAndHeaterActivity.class);
                break;
            case R.id.nav_charger:
                intent = new Intent(this, ChargerActivity.class);
                break;
            default:
                return false;
        }

        startActivity(intent);
        finish();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void openNavigationDrawer() {
        Log.d("Drawer", "Opening navigation drawer");
        if (drawer != null) {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    private void Databinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_door);
        listView = binding.doorListView;
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        doorMenuIv = binding.doorMenuIv;
    }
}