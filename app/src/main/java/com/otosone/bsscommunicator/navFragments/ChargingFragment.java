package com.otosone.bsscommunicator.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.listItem.ChargingItem;
import com.otosone.bsscommunicator.adapter.ChargingAdapter;
import com.otosone.bsscommunicator.databinding.FragmentChargingBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ChargingFragment extends Fragment {

    FragmentChargingBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;

    private ChargingAdapter chargingAdapter;
    private List<ChargingItem> chargingItems;
    private Button chargingBtn;
    public static ChargingFragment newInstance() {
        ChargingFragment fragment = new ChargingFragment();
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
                Log.d("StationFragment", "MessageReceivedListener called");
                getActivity().runOnUiThread(() -> {

                    Toast.makeText(getActivity(), "Received JSON: " + completeJsonString, Toast.LENGTH_LONG).show();
                    Log.d("ChargingFragment", "Complete JSON: " + completeJsonString);

                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("type") && receivedJson.getString("type").equals("CTRL_CHG")) {
                            int count = receivedJson.getInt("count");
                            JSONArray chgList = receivedJson.getJSONArray("chgList");

                            List<ChargingItem> chargingItems = new ArrayList<>();

                            for (int i = 0; i < 16; i++) {
                                boolean found = false;

                                for (int j = 0; j < count; j++) {
                                    JSONObject chgObj = chgList.getJSONObject(j);
                                    int id = chgObj.getInt("id");
                                    int charge = chgObj.getInt("charge");

                                    if (id == i + 1) {
                                        ChargingItem item = new ChargingItem(false, String.format("%02d", i + 1), charge == 1 ? "START" : "STOP");
                                        chargingItems.add(item);
                                        found = true;
                                        break;
                                    }
                                }

                                if (!found) {
                                    ChargingItem item = new ChargingItem(false, String.format("%02d", i + 1), "STOP");
                                    chargingItems.add(item);
                                }
                            }

                            ChargingAdapter chargingAdapter = new ChargingAdapter(requireContext(), chargingItems);
                            binding.chargingListView.setAdapter(chargingAdapter);
                        }

                    } catch (JSONException e) {
                        Log.e("ChargingFragment", "Error parsing received JSON", e);
                    }
                });

                Log.d("ChargingFragment", "Complete JSON: " + completeJsonString);

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
        chargingItems = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            chargingItems.add(new ChargingItem(false, String.format("%02d", i), "STOP"));
        }

        chargingAdapter = new ChargingAdapter(requireContext(), chargingItems);

        ListView listView = binding.chargingListView;
        listView.setAdapter(chargingAdapter);

        chargingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a JSON object
                JSONObject json = new JSONObject();
                try {
                    json.put("type", "CTRL_CHG");
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
                    json.put("count", checkedCount);
                    json.put("chgList", chgList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String jsonString = json.toString();
                Log.d("UTF=8", jsonString);

                // Call the sendAsciiMessage method with the string as an argument
                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(jsonString);
                    Log.d("json11", jsonString);
                } else {
                    Log.e("ChargingFragment", "BluetoothConnectionService is not bound");
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
