package com.otosone.bsscommunicator.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.otosone.bsscommunicator.utils.DataHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("response") && receivedJson.getString("response").equals("CTRL_BMS")) {
                            String result = receivedJson.getString("result");
                            int errorCode = receivedJson.getInt("error_code");

                            if (result.equals("ok") && errorCode == 0) {
                                JSONObject responseJson = new JSONObject();
                                responseJson.put("response", "CTRL_BMS");
                                responseJson.put("result", "ok");
                                responseJson.put("error_code", 0);
                                // Handle success case here
                            } else {
                                // Handle error case here
                                Log.e("BMSFragment", "Received error: result = " + result + ", error_code = " + errorCode);
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("BMSFragment", "Error parsing received JSON", e);
                    }
                });

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

        bmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject json = new JSONObject();
                try {
                    json.put("request", "CTRL_BMS");

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

                }

                if (isBound && bluetoothConnectionService != null) {
                    String jsonString = json.toString();
                    bluetoothConnectionService.sendMessage(jsonString);

                } else {
                    Toast.makeText(getActivity(), "Not connected to a device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize bmsItems here
        bmsItems = new ArrayList<>();
        for (int i = 0; i <= 15; i++) {
            String id = String.format("%02d", i);
            BMSItem item = new BMSItem(false, id, 0, 0);
            bmsItems.add(item);
        }

        // Set adapter to the listView
        bmsAdapter = new BMSAdapter(requireContext(), bmsItems);
        ListView listView = binding.bmsListView;
        listView.setAdapter(bmsAdapter);

        Map<String, JSONObject> binaryStatusMap = DataHolder.getInstance().getSocketStatusMap().getValue();
        if (binaryStatusMap != null) {
            Log.d("statusinfo", binaryStatusMap.toString());
            for (Map.Entry<String, JSONObject> entry : binaryStatusMap.entrySet()) {
                String socketId = entry.getKey();
                JSONObject socketData = entry.getValue();
                try {
                    JSONObject bms = socketData.getJSONObject("bms"); // Here's the change
                    int soc = bms.getInt("soc");

                    // Assume you want to set soc to your BMSItem's value
                    for (BMSItem item : bmsItems) {
                        if (item.getId().equals(String.format("%02d", Integer.parseInt(socketId)))) {
                            item.setValue(soc);  // Note: you may need to modify this depending on the type of the 'value' in your BMSItem class
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // Notify the adapter about the change in bmsItems
            bmsAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        bindBluetoothConnectionService();

        bmsItems = new ArrayList<>();
        for (int i = 0; i <= 15; i++) {
            String id = String.format("%02d", i);
            BMSItem item = new BMSItem(false, id, 0, 0);
            bmsItems.add(item);
        }

        // Set adapter to the listView
        bmsAdapter = new BMSAdapter(requireContext(), bmsItems);
        ListView listView = binding.bmsListView;
        listView.setAdapter(bmsAdapter);

        Map<String, JSONObject> binaryStatusMap = DataHolder.getInstance().getSocketStatusMap().getValue();
        if (binaryStatusMap != null) {
            Log.d("statusinfo", binaryStatusMap.toString());
            for (Map.Entry<String, JSONObject> entry : binaryStatusMap.entrySet()) {
                String socketId = entry.getKey();
                JSONObject socketData = entry.getValue();
                try {
                    JSONObject bms = socketData.getJSONObject("bms"); // Here's the change
                    int soc = bms.getInt("soc");

                    // Assume you want to set soc to your BMSItem's value
                    for (BMSItem item : bmsItems) {
                        if (item.getId().equals(String.format("%02d", Integer.parseInt(socketId)))) {
                            item.setValue(soc);  // Note: you may need to modify this depending on the type of the 'value' in your BMSItem class
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // Notify the adapter about the change in bmsItems
            bmsAdapter.notifyDataSetChanged();
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