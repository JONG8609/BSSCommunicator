package com.otosone.bsscommunicator.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.otosone.bsscommunicator.adapter.ChargingAdapter;
import com.otosone.bsscommunicator.listItem.BMSItem;
import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.adapter.BMSAdapter;
import com.otosone.bsscommunicator.databinding.FragmentBmsBinding;
import com.otosone.bsscommunicator.listItem.ChargingItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class BMSFragment extends Fragment {

    FragmentBmsBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private BMSAdapter bmsAdapter;
    private List<BMSItem> bmsItems;
    private Button bmsBtn;
    public static BMSFragment newInstance() {
        return new BMSFragment();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) iBinder;
            bluetoothConnectionService = binder.getService();
            isBound = true;

            Log.d("BMSFragment", "Service connected");

            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {
                Log.d("BMSFragment", "MessageReceivedListener called");
                getActivity().runOnUiThread(() -> {

                    Toast.makeText(getActivity(), "Received JSON: " + completeJsonString, Toast.LENGTH_LONG).show();
                    Log.d("BMSFragment", "Complete JSON: " + completeJsonString);

                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("type") && receivedJson.getString("type").equals("CTRL_BMS")) {
                            // Update your BMS items here
                        }

                    } catch (JSONException e) {
                        Log.e("BMSFragment", "Error parsing received JSON", e);
                    }
                });

                Log.d("BMSFragment", "Complete JSON: " + completeJsonString);
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothConnectionService = null;
            isBound = false;
            Log.d("BMSFragment", "Service disconnected");
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bms, container, false);
        bmsBtn = binding.bmsBtn;

        // Initialize bmsItems
        bmsItems = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            bmsItems.add(new BMSItem(false, i, 0, 0));
        }

        bmsAdapter = new BMSAdapter(requireContext(), bmsItems);

        ListView listView = binding.bmsListView;
        listView.setAdapter(bmsAdapter);

        bmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject json = new JSONObject();
                try {
                    json.put("type", "CTRL_BMS");

                    JSONArray bmsJsonArray = new JSONArray();
                    int checkedCount = 0;
                    for (BMSItem bmsItem : bmsItems) {
                        if (bmsItem.isChecked()) {
                            JSONObject bmsJsonObject = new JSONObject();
                            bmsJsonObject.put("id", bmsItem.getId());
                            bmsJsonObject.put("cmd", bmsItem.getCmd());
                            bmsJsonObject.put("value", bmsItem.getValue());
                            bmsJsonArray.put(bmsJsonObject);
                            checkedCount++;
                        }
                    }
                    json.put("count", checkedCount);
                    json.put("bmsList", bmsJsonArray);
                } catch (JSONException e) {
                    Log.e("BMSFragment", "Error creating JSON object", e);
                }

                if (isBound && bluetoothConnectionService != null) {
                    String jsonString = json.toString();
                    bluetoothConnectionService.sendMessage(jsonString);
                    Toast.makeText(getActivity(), "Sent JSON: " + jsonString, Toast.LENGTH_LONG).show();
                    Log.d("BMSFragment", "Sent JSON: " + jsonString);
                } else {
                    Toast.makeText(getActivity(), "Not connected to a device", Toast.LENGTH_SHORT).show();
                }
            }
        });




        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindBluetoothConnectionService();
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindBluetoothConnectionService();
    }
    public void setBluetoothConnectionService(BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }
    private ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();
    private void bindBluetoothConnectionService() {
        Intent intent = new Intent(getContext(), BluetoothConnectionService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindBluetoothConnectionService() {
        if (isBound) {
            getActivity().unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}