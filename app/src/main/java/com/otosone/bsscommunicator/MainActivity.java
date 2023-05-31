package com.otosone.bsscommunicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.otosone.bsscommunicator.adapter.ExpandableListAdapter;
import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.bluetooth.BluetoothStateReceiver;
import com.otosone.bsscommunicator.bluetooth.ConnectionFailedListener;
import com.otosone.bsscommunicator.databinding.ActivityMainBinding;
import com.otosone.bsscommunicator.navFragments.BMSFragment;
import com.otosone.bsscommunicator.navFragments.ChargerFragment;
import com.otosone.bsscommunicator.navFragments.ChargingFragment;
import com.otosone.bsscommunicator.navFragments.DoorFragment;
import com.otosone.bsscommunicator.navFragments.FanFragment;
import com.otosone.bsscommunicator.navFragments.HeaterFragment;
import com.otosone.bsscommunicator.navFragments.ResetFragment;
import com.otosone.bsscommunicator.navFragments.ScanFragment;
import com.otosone.bsscommunicator.navFragments.StationFragment;
import com.otosone.bsscommunicator.navFragments.StatusFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConnectionFailedListener {

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
    private TextView versionNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        navigationMenuButton = binding.navigationMenuButton;
        mainBtn = binding.mainBtn;

        BluetoothStateReceiver bluetoothStateReceiver = new BluetoothStateReceiver(this);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);



        navigationMenuButton.setOnClickListener(v -> {
            // Add this code to close the soft keyboard if it's open
            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

            openNavigationDrawer();
        });

        expandableListView = navigationView.findViewById(R.id.expandableListView);

        View footerView = getLayoutInflater().inflate(R.layout.footer, null);
        TextView versionNameTextView = footerView.findViewById(R.id.version_name);

        // Set the version name
        SharedPreferences sharedPreferences = getSharedPreferences("apkVersion", Context.MODE_PRIVATE);
        String apkVersion = sharedPreferences.getString("apkVersion", "1.2.7");
        versionNameTextView.setText("V" + apkVersion);

        // Add the footer view to the expandable list view
        expandableListView.addFooterView(footerView);

        prepareListData();

        expandableListAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(expandableListAdapter);



        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {

            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

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
                        navigationView.setVisibility(View.GONE);
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
                            selectedFragment = new StationFragment();
                            mainBtn.setVisibility(View.GONE);
                            break;
                        case 1:
                            FanFragment fanFragment = new FanFragment();
                            fanFragment.setBluetoothConnectionService(bluetoothConnectionService);
                            selectedFragment = new FanFragment();
                            mainBtn.setVisibility(View.GONE);
                            break;
                        case 2:
                            HeaterFragment heaterFragment = new HeaterFragment();
                            heaterFragment.setBluetoothConnectionService(bluetoothConnectionService);
                            selectedFragment = new HeaterFragment();
                            mainBtn.setVisibility(View.GONE);
                            break;
                        case 3:
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

                    try {
                        JSONObject receivedJson = new JSONObject(message);

                        if (receivedJson.has("request") && receivedJson.getString("request").equals("INFO")) {
                            // Extract stationId and apkVersion from the received JSON
                            JSONObject dataObject = receivedJson.getJSONObject("data");
                            String stationId = dataObject.getString("stationId");
                            String apkVersion = dataObject.getString("apkVersion");

                            // Save the extracted values as SharedPreferences
                            SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("your_preferences_name", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("stationId", stationId);
                            editor.putString("apkVersion", apkVersion);
                            editor.apply();

                            // Create a response JSON object
                            JSONObject responseJson = new JSONObject();
                            responseJson.put("response", "INFO");
                            responseJson.put("result", "ok");
                            responseJson.put("error_code", 0);

                            // Send the response to the server
                            bluetoothConnectionService.sendMessage(responseJson.toString());
                        }

                    } catch (JSONException e) {
                        Log.e("StationFragment", "Error parsing received JSON", e);
                    }

                });
                isBound = true;
            }


            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                isBound = false;
                bluetoothConnectionService = null;
            }
        };
        mainBtn.setVisibility(View.GONE);
        StatusFragment statusFragment = new StatusFragment();
        statusFragment.setBluetoothConnectionService(bluetoothConnectionService);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, statusFragment)
                .commit();
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
            bluetoothConnectionService.disconnect();
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
        preferencesMenu.add("Fan");
        preferencesMenu.add("Heater");
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