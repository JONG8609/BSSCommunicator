package com.otosone.bsscommunicator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import com.google.android.material.navigation.NavigationView;
import com.otosone.bsscommunicator.databinding.ActivityMainBinding;
import com.otosone.bsscommunicator.navFragments.BMSFragment;
import com.otosone.bsscommunicator.navFragments.ChargerFragment;
import com.otosone.bsscommunicator.navFragments.ChargingFragment;
import com.otosone.bsscommunicator.navFragments.DoorFragment;
import com.otosone.bsscommunicator.navFragments.FanAndHeaterFragment;
import com.otosone.bsscommunicator.navFragments.ResetFragment;
import com.otosone.bsscommunicator.navFragments.StationFragment;
import com.otosone.bsscommunicator.navFragments.StatusFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ExpandableListAdapter expandableListAdapter;
    private ExpandableListView expandableListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ImageButton navigationMenuButton;
    private Button mainBtn;
    private boolean isBound = false;
    private BluetoothConnectionService bluetoothConnectionService;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        navigationMenuButton = binding.navigationMenuButton;
        mainBtn = binding.mainBtn;
        navigationMenuButton.setOnClickListener(v -> openNavigationDrawer());

        expandableListView = navigationView.findViewById(R.id.expandableListView);

        prepareListData();

        expandableListAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Fragment selectedFragment = null;

            switch (groupPosition) {
                case 0:
                    if (childPosition == 0) {
                        selectedFragment = new StatusFragment();
                    }
                    break;
                case 1:
                    if (childPosition == 0) {
                        selectedFragment = new StatusFragment();
                    }
                    break;
                case 2:
                    switch (childPosition) {
                        case 0:
                            selectedFragment = new ResetFragment();
                            break;
                        case 1:
                            selectedFragment = new ChargingFragment();
                            break;
                        case 2:
                            selectedFragment = new DoorFragment();
                            break;
                        case 3:
                            selectedFragment = new BMSFragment();
                            break;
                    }
                    break;
                case 3:
                    switch (childPosition) {
                        case 0:
                            selectedFragment = new StationFragment();
                            mainBtn.setVisibility(View.GONE);
                            break;
                        case 1:
                            selectedFragment = new FanAndHeaterFragment();
                            break;
                        case 2:
                            selectedFragment =
                                    new ChargerFragment();
                            break;
                    }
                    break;
                case 4:
                    if (childPosition == 0) {
                        selectedFragment = new StatusFragment();
                    }
                    break;
                case 5:
                    if (childPosition == 0) {
                        selectedFragment = new StatusFragment();
                    }
                    break;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                drawer.closeDrawers();
            }
            return false;
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) service;
                bluetoothConnectionService = binder.getService();
                bluetoothConnectionService.setMessageReceivedListener(message -> {
                    Log.d("ReceivedMessage", message);
                });
                isBound = true;
                updateUI();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                isBound = false;
                bluetoothConnectionService = null;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothConnectionService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        listDataHeader.add("Scan");
        listDataHeader.add("Monitoring");
        listDataHeader.add("Control");
        listDataHeader.add("Preferences");
        listDataHeader.add("Management");
        listDataHeader.add("Update");

        List<String> scanMenu = new ArrayList<>();
        scanMenu.add("Scan");

        List<String> monitoringMenu = new ArrayList<>();
        monitoringMenu.add("Socket Status");

        List<String> controlMenu = new ArrayList<>();
        controlMenu.add("BSS Reset");
        controlMenu.add("Charging");
        controlMenu.add("Door");
        controlMenu.add("BMS");

        List<String> preferencesMenu = new ArrayList<>();
        preferencesMenu.add("Station");
        preferencesMenu.add("FAN&Heater");
        preferencesMenu.add("Charger");

        List<String> managementMenu = new ArrayList<>();

        List<String> updateMenu = new ArrayList<>();

        listDataChild.put(listDataHeader.get(0), scanMenu);
        listDataChild.put(listDataHeader.get(1), monitoringMenu);
        listDataChild.put(listDataHeader.get(2), controlMenu);
        listDataChild.put(listDataHeader.get(3), preferencesMenu);
        listDataChild.put(listDataHeader.get(4), managementMenu);
        listDataChild.put(listDataHeader.get(5), updateMenu);
    }

    protected void openNavigationDrawer() {
        if (drawer != null) {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    private void updateUI() {
        if (bluetoothConnectionService != null && bluetoothConnectionService.isConnected()) {
            // Show StationFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new StationFragment())
                    .commit();
        } else {
            // Show a message or a UI component to indicate that the device is not connected
        }
    }
}