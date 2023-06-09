package com.otosone.bsscommunicator.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.listItem.ChargingItem;
import com.otosone.bsscommunicator.adapter.ChargingAdapter;
import com.otosone.bsscommunicator.databinding.FragmentChargingBinding;
import com.otosone.bsscommunicator.listItem.DoorItem;
import com.otosone.bsscommunicator.utils.DataHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChargingFragment extends Fragment {

    FragmentChargingBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;

    private ChargingAdapter chargingAdapter;
    private List<ChargingItem> chargingItems;
    private Button chargingBtn;
    private CheckBox charging_checkbox;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) iBinder;
            bluetoothConnectionService = binder.getService();
            isBound = true;

            Log.d("StationFragment", "Service connected");

            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {
                getActivity().runOnUiThread(() -> {

                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("response") && receivedJson.getString("response").equals("CTRL_CHG")) {
                            String result = receivedJson.getString("result");
                            int errorCode = receivedJson.getInt("error_code");

                            if (result.equals("ok") && errorCode == 0) {
                                JSONObject responseJson = new JSONObject();
                                responseJson.put("response", "CTRL_CHG");
                                responseJson.put("result", "ok");
                                responseJson.put("error_code", 0);
                                // Handle success case here
                            } else {
                                // Handle error case here
                                Log.e("StationFragment", "Received error: result = " + result + ", error_code = " + errorCode);
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("ChargingFragment", "Error parsing received JSON", e);
                    }
                });

            });


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothConnectionService = null;
            isBound = false;
            Log.d("ChargingFragment", "Service disconnected");
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChargingBinding.inflate(inflater, container, false);
        chargingBtn = binding.chargingBtn;
        charging_checkbox = binding.chargingCheckbox;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            charging_checkbox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        }
        charging_checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (ChargingItem item : chargingItems) {
                item.setChecked(isChecked);
            }
            // Notify the adapter about the change in doorItems
            chargingAdapter.notifyDataSetChanged();
        });

        chargingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a JSON object
                JSONObject json = new JSONObject();
                try {
                    json.put("request", "CTRL_CHG");

                    JSONObject data = new JSONObject(); // Create a nested JSON object for data
                    JSONArray chgList = new JSONArray();

                    int checkedCount = 0;
                    for (ChargingItem item : chargingItems) {
                        if (item.isChecked()) {
                            JSONObject chgObj = new JSONObject();
                            chgObj.put("id", Integer.parseInt(item.getId()));
                            chgObj.put("charge", item.getCharging().equals("START") ? 1 : 0);
                            chgList.put(chgObj);
                            checkedCount++;
                        }
                    }
                    data.put("count", checkedCount);
                    data.put("chgList", chgList);

                    json.put("data", data);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String jsonString = json.toString();

                // Call the sendAsciiMessage method with the string as an argument
                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(jsonString);
                } else {
                    Log.e("ChargingFragment", "BluetoothConnectionService is not bound");
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize chargingItems here
        chargingItems = new ArrayList<>();
        for (int i = 0; i <= 15; i++) {
            String id = String.format("%02d", i);
            ChargingItem item = new ChargingItem(false, id, "STOP");
            chargingItems.add(item);
        }

        // Set adapter to the listView
        chargingAdapter = new ChargingAdapter(requireContext(), chargingItems);
        ListView listView = binding.chargingListView;
        listView.setAdapter(chargingAdapter);

        Map<String, String> binaryStatusMap = DataHolder.getInstance().getBinaryStatusMap().getValue();
        if (binaryStatusMap != null) {
            Log.d("statusinfo", binaryStatusMap.toString());
            for (Map.Entry<String, String> entry : binaryStatusMap.entrySet()) {
                String socketId = entry.getKey();
                String binaryStatus = entry.getValue();
                // Update ChargingItem status based on the 7th char in binaryStatus
                if (binaryStatus.length() > 6) {
                    char chargingStatusChar = binaryStatus.charAt(6);
                    String chargingStatus = (chargingStatusChar == '1') ? "START" : "STOP";
                    for (ChargingItem item : chargingItems) {
                        if (item.getId().equals(String.format("%02d", Integer.parseInt(socketId)))) {
                            item.setCharging(chargingStatus);
                            break;
                        }
                    }
                }
            }
            // Notify the adapter about the change in chargingItems
            chargingAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bindBluetoothConnectionService();

        // Initialize chargingItems here
        chargingItems = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            String id = String.format("%02d", i);
            ChargingItem item = new ChargingItem(false, id, "STOP");
            chargingItems.add(item);
        }

        // Set adapter to the listView
        chargingAdapter = new ChargingAdapter(requireContext(), chargingItems);
        ListView listView = binding.chargingListView;
        listView.setAdapter(chargingAdapter);

        Map<String, String> binaryStatusMap = DataHolder.getInstance().getBinaryStatusMap().getValue();
        if (binaryStatusMap != null) {
            Log.d("statusinfo", binaryStatusMap.toString());
            for (Map.Entry<String, String> entry : binaryStatusMap.entrySet()) {
                String socketId = entry.getKey();
                String binaryStatus = entry.getValue();
                // Update ChargingItem status based on the 7th char in binaryStatus
                if (binaryStatus.length() > 6) {
                    char chargingStatusChar = binaryStatus.charAt(6);
                    String chargingStatus = (chargingStatusChar == '1') ? "START" : "STOP";
                    for (ChargingItem item : chargingItems) {
                        if (item.getId().equals(String.format("%02d", Integer.parseInt(socketId)))) {
                            item.setCharging(chargingStatus);
                            break;
                        }
                    }
                }
            }
            // Notify the adapter about the change in chargingItems
            chargingAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindBluetoothConnectionService();
    }
    public void setBluetoothConnectionService(BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }
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
