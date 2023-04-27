package com.otosone.bsscommunicator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.otosone.bsscommunicator.databinding.ActivityMainBinding;
import com.otosone.bsscommunicator.navFragments.BMSFragment;
import com.otosone.bsscommunicator.navFragments.ChargerFragment;
import com.otosone.bsscommunicator.navFragments.ChargingFragment;
import com.otosone.bsscommunicator.navFragments.DoorFragment;
import com.otosone.bsscommunicator.navFragments.FanAndHeaterFragment;
import com.otosone.bsscommunicator.navFragments.ResetFragment;
import com.otosone.bsscommunicator.navFragments.ScanFragment;
import com.otosone.bsscommunicator.navFragments.StationFragment;
import com.otosone.bsscommunicator.navFragments.StatusFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConnectionFailedListener{

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


        BluetoothStateReceiver bluetoothStateReceiver = new BluetoothStateReceiver(this);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);


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
                        ScanFragment scanFragment = new ScanFragment();
                        scanFragment.setBluetoothConnectionService(bluetoothConnectionService);
                        selectedFragment = scanFragment;
                    }
                    // Other cases here...
                    break;
                case 1:
                    if (childPosition == 0) {
                        StatusFragment statusFragment = new StatusFragment();
                        statusFragment.setBluetoothConnectionService(bluetoothConnectionService);
                        selectedFragment = new StatusFragment();
                        mainBtn.setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    switch (childPosition) {
                        case 0:
                            ResetFragment resetFragment = new ResetFragment();
                            resetFragment.setBluetoothConnectionService(bluetoothConnectionService);
                            selectedFragment = new ResetFragment();
                            mainBtn.setVisibility(View.GONE);
                            break;
                        case 1:
                            ChargingFragment chargingFragment = new ChargingFragment();
                            chargingFragment.setBluetoothConnectionService(bluetoothConnectionService);
                            selectedFragment = new ChargingFragment();
                            mainBtn.setVisibility(View.GONE);
                            break;
                        case 2:
                            DoorFragment doorFragment = new DoorFragment();
                            doorFragment.setBluetoothConnectionService(bluetoothConnectionService);
                            selectedFragment = new DoorFragment();
                            mainBtn.setVisibility(View.GONE);
                            break;
                        case 3:
                            BMSFragment bmsFragment = new BMSFragment();
                            bmsFragment.setBluetoothConnectionService(bluetoothConnectionService);
                            selectedFragment = new BMSFragment();
                            mainBtn.setVisibility(View.GONE);
                            break;
                    }
                    break;
                case 3:
                    switch (childPosition) {
                        case 0:
                            StationFragment stationFragment = new StationFragment();
                            stationFragment.setBluetoothConnectionService(bluetoothConnectionService);
                            selectedFragment = stationFragment;
                            mainBtn.setVisibility(View.GONE);
                            break;
                        case 1:
                            FanAndHeaterFragment fanAndHeaterFragment = new FanAndHeaterFragment();
                            fanAndHeaterFragment.setBluetoothConnectionService(bluetoothConnectionService);
                            selectedFragment = new FanAndHeaterFragment();
                            mainBtn.setVisibility(View.GONE);
                            break;
                        case 2:
                            ChargerFragment chargerFragment = new ChargerFragment();
                            chargerFragment.setBluetoothConnectionService(bluetoothConnectionService);
                            selectedFragment = new ChargerFragment();
                            mainBtn.setVisibility(View.GONE);
                            break;
                    }
                    break;
                case 4:
                    if (childPosition == 0) {
                        //selectedFragment = new StatusFragment();
                    }
                    break;
                case 5:
                    if (childPosition == 0) {
                        //selectedFragment = new StatusFragment();
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
                });
                isBound = true;

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

    public void switchToScanFragment() {
        ScanFragment scanFragment = new ScanFragment();
        scanFragment.setBluetoothConnectionService(bluetoothConnectionService);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, scanFragment)
                .commit();
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

    public void setBluetoothConnectionService(BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }

    @Override
    public void onConnectionFailed() {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, "Connection failed. Switching to ScanFragment", Toast.LENGTH_LONG).show();
            ScanFragment scanFragment = new ScanFragment();
            scanFragment.setBluetoothConnectionService(bluetoothConnectionService);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, scanFragment)
                    .commit();
        });
    }
}