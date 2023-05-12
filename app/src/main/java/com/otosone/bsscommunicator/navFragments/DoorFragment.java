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
import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.listItem.ChargingItem;
import com.otosone.bsscommunicator.listItem.DoorItem;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.adapter.DoorAdapter;
import com.otosone.bsscommunicator.databinding.FragmentDoorBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class DoorFragment extends Fragment {

    FragmentDoorBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private DoorAdapter doorAdapter;
    private List<DoorItem> doorItems;
    private Button doorBtn;
    public static DoorFragment newInstance(String param1, String param2) {
        DoorFragment fragment = new DoorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) iBinder;
            bluetoothConnectionService = binder.getService();
            isBound = true;

            Log.d("StationFragment", "Service connected");

            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {
                Log.d("DoorFragment", "MessageReceivedListener called");
                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("response") && receivedJson.getString("response").equals("CTRL_LOCK")) {
                            String result = receivedJson.getString("result");
                            int errorCode = receivedJson.getInt("error_code");

                            if (result.equals("ok") && errorCode == 0) {
                                JSONObject responseJson = new JSONObject();
                                responseJson.put("response", "CTRL_LOCK");
                                responseJson.put("result", "ok");
                                responseJson.put("error_code", 0);
                                // Handle success case here
                            } else {
                                // Handle error case here
                                Log.e("DoorFragment", "Received error: result = " + result + ", error_code = " + errorCode);
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("DoorFragment", "Error parsing received JSON", e);
                    }
                });

                Log.d("DoorFragment", "Complete JSON: " + completeJsonString);

            });


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothConnectionService = null;
            isBound = false;
            Log.d("StationFragment", "Service disconnected");
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

        // Create a list of DoorItems
        doorBtn = binding.doorBtn;
        doorItems = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            doorItems.add(new DoorItem(false, String.format("%02d", i), "UNLOCK"));
        }

        doorAdapter = new DoorAdapter(requireContext(), doorItems);

        ListView listView = binding.doorListView;
        listView.setAdapter(doorAdapter);


        //프로토콜 변경 해야됨
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
                            doorObj.put("lock", item.getDoorStatus().equals("UNLOCK") ? 1 : 0);
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
                Log.d("UTF-8", jsonString);

                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(jsonString);
                } else {
                    Log.e("DoorFragment", "BluetoothConnectionService is not bound");
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