package com.otosone.bsscommunicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.otosone.bsscommunicator.adapter.NavigationDrawerAdapter;
import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.bluetooth.BluetoothStateReceiver;
import com.otosone.bsscommunicator.bluetooth.ConnectionFailedListener;
import com.otosone.bsscommunicator.databinding.ActivityMainBinding;
import com.otosone.bsscommunicator.listItem.NavigationDrawerItem;
import com.otosone.bsscommunicator.navFragments.BMSFragment;
import com.otosone.bsscommunicator.navFragments.ChargerFragment;
import com.otosone.bsscommunicator.navFragments.DoorFragment;
import com.otosone.bsscommunicator.navFragments.FanFragment;
import com.otosone.bsscommunicator.navFragments.HeaterFragment;
import com.otosone.bsscommunicator.navFragments.ResetFragment;
import com.otosone.bsscommunicator.navFragments.ScanFragment;
import com.otosone.bsscommunicator.navFragments.StationFragment;
import com.otosone.bsscommunicator.navFragments.StatusFragment;
import com.otosone.bsscommunicator.utils.DataHolder;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ConnectionFailedListener {

    private ActivityMainBinding binding;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ImageButton navigationMenuButton;
    private Button mainBtn;
    private boolean isBound = false;
    private BluetoothConnectionService bluetoothConnectionService;
    private ServiceConnection serviceConnection;
    private TextView versionNameTextView;
    private String apkVersion;
    private ListView listView;
    private NavigationDrawerAdapter adapter;

    NavigationDrawerItem[] drawerItems = new NavigationDrawerItem[]{
            new NavigationDrawerItem.NavigationHeader("OTOS BSS APP", true),
            new NavigationDrawerItem.NavigationDivider(), // New divider
            new NavigationDrawerItem.NavigationItem("Bluetooth Scan", R.drawable.bookmark),
            new NavigationDrawerItem.NavigationItem("Socket Status", R.drawable.bookmark),
            new NavigationDrawerItem.NavigationDivider(), // New divider
            new NavigationDrawerItem.NavigationHeader("Control", false),
            new NavigationDrawerItem.NavigationItem("Station reset", R.drawable.bookmark),
            new NavigationDrawerItem.NavigationItem("Socket door", R.drawable.bookmark),
            new NavigationDrawerItem.NavigationItem("Charger", R.drawable.bookmark),
            new NavigationDrawerItem.NavigationItem("BMS", R.drawable.bookmark),
            new NavigationDrawerItem.NavigationDivider(), // New divider
            new NavigationDrawerItem.NavigationHeader("Setting", false),
            new NavigationDrawerItem.NavigationItem("Station", R.drawable.bookmark),
            new NavigationDrawerItem.NavigationItem("Fan", R.drawable.bookmark),
            new NavigationDrawerItem.NavigationItem("Heater", R.drawable.bookmark),
            new NavigationDrawerItem.NavigationItem("Charger", R.drawable.bookmark),
    };

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
        listView = binding.listView;

        adapter = new NavigationDrawerAdapter(this, drawerItems);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        DataHolder dataHolder = DataHolder.getInstance();
        dataHolder.getInfo().observe(this, new Observer<JSONObject>() {
            @Override
            public void onChanged(JSONObject info) {
                try {
                    String apkVersion = info.getString("apkVersion");
                    versionNameTextView.setText("V" + apkVersion);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        BluetoothStateReceiver bluetoothStateReceiver = new BluetoothStateReceiver(this);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);

        navigationMenuButton.setOnClickListener(v -> {

            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
            openNavigationDrawer();
        });

        View footerView = getLayoutInflater().inflate(R.layout.footer, null);
        versionNameTextView = footerView.findViewById(R.id.version_name);

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
                            apkVersion = dataObject.getString("apkVersion");
                            // Update versionNameTextView directly here
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    versionNameTextView.setText("V" + apkVersion);
                                }
                            });

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
                ScanFragment scanFragment = new ScanFragment();
                scanFragment.setBluetoothConnectionService(bluetoothConnectionService);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, scanFragment)
                        .commit();
            }
        };
        mainBtn.setVisibility(View.GONE);
        StatusFragment statusFragment = new StatusFragment();
        statusFragment.setBluetoothConnectionService(bluetoothConnectionService);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, statusFragment)
                .commit();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavigationDrawerItem selectedItem = adapter.getItem(position);
                if(selectedItem.isHeader()){
                    // Header was clicked, we can choose to do nothing
                    // Or you can handle it accordingly.
                } else {
                    // NavigationItem was clicked, proceed as usual
                    NavigationDrawerItem.NavigationItem navigationItem = (NavigationDrawerItem.NavigationItem) selectedItem;
                    Fragment selectedFragment = null;

                    switch (navigationItem.getTitle()) {
                        case "Bluetooth Scan":
                            // Disconnect bluetooth before scanning again
                            if (bluetoothConnectionService.isConnected()) {
                                bluetoothConnectionService.disconnect();
                            }
                            selectedFragment = new ScanFragment();
                            ((ScanFragment) selectedFragment).setBluetoothConnectionService(bluetoothConnectionService);
                            break;
                        case "Socket Status":
                            selectedFragment = new StatusFragment();
                            break;
                        case "Station reset":
                            selectedFragment = new ResetFragment();
                            break;
                        case "Socket door":
                            selectedFragment = new DoorFragment();
                            break;
                        case "Charger":
                            selectedFragment = new ChargerFragment();
                            break;
                        case "BMS":
                            selectedFragment = new BMSFragment();
                            break;
                        case "Station":
                            selectedFragment = new StationFragment();
                            break;
                        case "Fan":
                            selectedFragment = new FanFragment();
                            break;
                        case "Heater":
                            selectedFragment = new HeaterFragment();
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid item title " + navigationItem.getTitle());
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                    }

                    drawer.closeDrawer(GravityCompat.START); // Close the drawer
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothConnectionService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        if (apkVersion != null) {
            versionNameTextView.setText("V" + apkVersion);
        }
    }

    public void switchToScanFragment() {
        ScanFragment scanFragment = new ScanFragment();
        scanFragment.setBluetoothConnectionService(bluetoothConnectionService);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, scanFragment)
                .commit();
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