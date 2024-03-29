package com.otosone.bssmgr.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.otosone.bssmgr.bluetooth.BluetoothConnectionService;
import com.otosone.bssmgr.listItem.DoorItem;
import com.otosone.bssmgr.R;
import com.otosone.bssmgr.adapter.DoorAdapter;
import com.otosone.bssmgr.databinding.FragmentDoorBinding;
import com.otosone.bssmgr.utils.DataHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DoorFragment extends Fragment {

    FragmentDoorBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private DoorAdapter doorAdapter;
    private List<DoorItem> doorItems;
    private Button doorBtn;
    private CheckBox door_checkbox;
    private boolean responseReceived = false;


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) iBinder;
            bluetoothConnectionService = binder.getService();
            isBound = true;



            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {

                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("response") && receivedJson.getString("response").equals("CTRL_LOCK")) {
                            String result = receivedJson.getString("result");
                            int errorCode = receivedJson.getInt("error_code");

                            if (result.equals("ok") && errorCode == 0) {
                                responseReceived = true; // set the flag
                                Toast.makeText(getActivity(),"success", Toast.LENGTH_SHORT).show();
                            } else {
                                responseReceived = true; // set the flag
                                Toast.makeText(getActivity(),"fail", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("DoorFragment", "Error parsing received JSON", e);
                    }
                });



            });

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothConnectionService = null;
            isBound = false;

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_door, container, false);
        doorBtn = binding.doorBtn;
        door_checkbox = binding.doorCheckbox;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            door_checkbox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        }
        door_checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (DoorItem item : doorItems) {
                item.setChecked(isChecked);
            }
            // Notify the adapter about the change in doorItems
            doorAdapter.notifyDataSetChanged();
        });

        doorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject json = new JSONObject();
                try {
                    json.put("request", "CTRL_LOCK");
                    JSONObject dataObj = new JSONObject();

                    JSONArray doorList = new JSONArray();
                    int checkedCount = 0;
                    for (DoorItem item : doorItems) {
                        if (item.isChecked()) {
                            JSONObject doorObj = new JSONObject();
                            doorObj.put("id", Integer.parseInt(item.getId()));
                            doorObj.put("lock", item.getDoorStatus().equals("LOCK") ? 1 : 0);
                            doorList.put(doorObj);
                            checkedCount++;
                        }
                    }
                    dataObj.put("count", checkedCount);
                    dataObj.put("lockList", doorList);
                    json.put("data", dataObj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String jsonString = json.toString();


                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(jsonString);

                    responseReceived = false; // reset the flag

                    // Start a Handler to check for response
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!responseReceived) {
                                Toast.makeText(getActivity(),"fail", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 3000);
                } else {
                    Log.e("DoorFragment", "BluetoothConnectionService is not bound");
                }
                //Toast.makeText(getActivity(),"sampl", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize chargingItems here
        doorItems = new ArrayList<>();
        for (int i = 0; i <= 15; i++) {
            String id = String.format("%02d", i);
            DoorItem item = new DoorItem(false, id, "LOCK");
            doorItems.add(item);
        }

        // Set adapter to the listView
        doorAdapter = new DoorAdapter(requireContext(), doorItems);
        ListView listView = binding.doorListView;
        listView.setAdapter(doorAdapter);

        Map<String, String> binaryStatusMap = DataHolder.getInstance().getBinaryStatusMap().getValue();
        if (binaryStatusMap != null) {

            for (Map.Entry<String, String> entry : binaryStatusMap.entrySet()) {
                String socketId = entry.getKey();
                String binaryStatus = entry.getValue();
                // Update ChargingItem status based on the 7th char in binaryStatus
                if (binaryStatus.length() >= 31) {
                    char chargingStatusChar = binaryStatus.charAt(27);
                    String doorStatus = chargingStatusChar == '0' ? "UNLOCK" : "LOCK";
                    for (DoorItem item : doorItems) {
                        if (item.getId().equals(String.format("%02d", Integer.parseInt(socketId)))) {
                            item.setDoorStatus(doorStatus);
                            break;
                        }
                    }
                }
            }
            // Notify the adapter about the change in chargingItems
            doorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bindBluetoothConnectionService();

        // Initialize chargingItems here
        doorItems = new ArrayList<>();
        for (int i = 0; i <= 15; i++) {
            String id = String.format("%02d", i);
            DoorItem item = new DoorItem(false, id, "LOCK");
            doorItems.add(item);
        }

        // Set adapter to the listView
        doorAdapter = new DoorAdapter(requireContext(), doorItems);
        ListView listView = binding.doorListView;
        listView.setAdapter(doorAdapter);

        Map<String, String> binaryStatusMap = DataHolder.getInstance().getBinaryStatusMap().getValue();
        if (binaryStatusMap != null) {

            for (Map.Entry<String, String> entry : binaryStatusMap.entrySet()) {
                String socketId = entry.getKey();
                String binaryStatus = entry.getValue();
                // Update ChargingItem status based on the 7th char in binaryStatus
                if (binaryStatus.length() > 31) {
                    char chargingStatusChar = binaryStatus.charAt(27);
                    String doorStatus = (chargingStatusChar == '0') ? "UNLOCK" : "LOCK";
                    for (DoorItem item : doorItems) {
                        if (item.getId().equals(String.format("%02d", Integer.parseInt(socketId)))) {
                            item.setDoorStatus(doorStatus);
                            break;
                        }
                    }
                }
            }
            // Notify the adapter about the change in chargingItems
            doorAdapter.notifyDataSetChanged();
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