package com.otosone.bsscommunicator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.google.android.material.navigation.NavigationView;
import com.otosone.bsscommunicator.databinding.ActivityMainBinding;
import com.otosone.bsscommunicator.navFragments.ChargerFragment;
import com.otosone.bsscommunicator.navFragments.ChargingFragment;
import com.otosone.bsscommunicator.navFragments.DoorFragment;
import com.otosone.bsscommunicator.navFragments.FanAndHeaterFragment;
import com.otosone.bsscommunicator.navFragments.ResetFragment;
import com.otosone.bsscommunicator.navFragments.StationFragment;
import com.otosone.bsscommunicator.navFragments.StatusFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ImageButton navigationMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        navigationMenuButton = binding.navigationMenuButton;

        navigationView.setNavigationItemSelectedListener(this);

        navigationMenuButton.setOnClickListener(v -> openNavigationDrawer());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        switch (item.getItemId()) {
            case R.id.nav_status:
                selectedFragment = new StatusFragment();
                break;
            case R.id.nav_reset:
                selectedFragment = new ResetFragment();
                break;
            case R.id.nav_charging:
                selectedFragment = new ChargingFragment();
                break;
            case R.id.nav_door:
                selectedFragment = new DoorFragment();
                break;
            case R.id.nav_station:
                selectedFragment = new StationFragment();
                break;
            case R.id.nav_fan_and_heater:
                selectedFragment = new FanAndHeaterFragment();
                break;
            case R.id.nav_charger:
                selectedFragment = new ChargerFragment();
                break;
            default:
                return false;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_layout, selectedFragment)
                .commit();

        drawer.closeDrawers();
        return true;
    }

    protected void openNavigationDrawer() {
        if (drawer != null) {
            drawer.openDrawer(GravityCompat.START);
        }
    }
}

