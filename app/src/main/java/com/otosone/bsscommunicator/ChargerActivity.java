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

import com.google.android.material.navigation.NavigationView;
import com.otosone.bsscommunicator.databinding.ActivityChargerBinding;
import com.otosone.bsscommunicator.databinding.ActivityFanAndHeaterBinding;

public class ChargerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ActivityChargerBinding binding;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private ImageView chargerMenuIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBinding();

        navigationView.setNavigationItemSelectedListener(this);


        chargerMenuIv.setOnClickListener(v -> openNavigationDrawer());
    }

    private void DataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_charger);
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        chargerMenuIv = binding.chargerMenuIv;
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
}