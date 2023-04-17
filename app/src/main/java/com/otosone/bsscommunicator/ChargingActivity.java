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
import com.otosone.bsscommunicator.databinding.ActivityChargingBinding;

import java.util.ArrayList;
import java.util.List;

public class ChargingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ListView listView;
    private ChargingAdapter adapter;
    private List<ChargingItem> itemList;
    private ActivityChargingBinding binding;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    ImageView chargingMenuIv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Databinding();

        navigationView.setNavigationItemSelectedListener(this);

        chargingMenuIv.setOnClickListener(v -> openNavigationDrawer());

        itemList = new ArrayList<>();

        adapter = new ChargingAdapter(this, itemList);
        itemList.add(new ChargingItem(false, "", "1"));
        itemList.add(new ChargingItem(false, "", "2"));
        itemList.add(new ChargingItem(false, "", "3"));
        itemList.add(new ChargingItem(false, "", "4"));
        itemList.add(new ChargingItem(false, "", "5"));
        itemList.add(new ChargingItem(false, "", "6"));
        itemList.add(new ChargingItem(false, "", "7"));
        itemList.add(new ChargingItem(false, "", "8"));
        itemList.add(new ChargingItem(false, "", "9"));
        itemList.add(new ChargingItem(false, "", "10"));
        itemList.add(new ChargingItem(false, "", "11"));
        itemList.add(new ChargingItem(false, "", "12"));
        itemList.add(new ChargingItem(false, "", "13"));
        itemList.add(new ChargingItem(false, "", "14"));
        itemList.add(new ChargingItem(false, "", "15"));
        itemList.add(new ChargingItem(false, "", "16"));

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_charging);
        chargingMenuIv = binding.chargingMenuIv;
        listView = binding.chargingListView;
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
    }
}