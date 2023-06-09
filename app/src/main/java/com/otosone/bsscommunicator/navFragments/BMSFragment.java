package com.otosone.bsscommunicator.navFragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.otosone.bsscommunicator.adapter.ChargingAdapter;
import com.otosone.bsscommunicator.listItem.BMSItem;
import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.adapter.BMSAdapter;
import com.otosone.bsscommunicator.databinding.FragmentBmsBinding;
import com.otosone.bsscommunicator.listItem.ChargingItem;
import com.otosone.bsscommunicator.listItem.DoorItem;
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
    private CheckBox bms_checkbox;
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
                            Log.d("231441", "221");
                            String result = receivedJson.getString("result");
                            int errorCode = receivedJson.getInt("error_code");

                            if (result.equals("ok") && errorCode == 0) {
                                Log.d("231441", "222");
                                // Show a notification here when the success case is met
                                showNotification("Success!");
                            } else {
                                Log.e("BMSFragment", "Received error: result = " + result + ", error_code = " + errorCode);
                                // Show a notification here when the error case is met
                                showNotification("Error: result = " + result + ", error_code = " + errorCode);
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

    private void showNotification(String message) {
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "channel_id")
                .setSmallIcon(R.drawable.rounded_button)
                .setContentTitle("Notification from App")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bms, container, false);
        bmsBtn = binding.bmsBtn;
        bms_checkbox = binding.bmsCheckbox;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bms_checkbox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        }
        bms_checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (BMSItem item : bmsItems) {
                item.setChecked(isChecked);
            }
            // Notify the adapter about the change in doorItems
            bmsAdapter.notifyDataSetChanged();
        });

        bmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject json = new JSONObject();
                try {
                    json.put("request", "CTRL_BMS");

                    JSONObject data = new JSONObject(); // Create a nested JSON object for data
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
                    data.put("count", checkedCount);
                    data.put("bmsList", bmsJsonArray);

                    json.put("data", data);
                } catch (JSONException e) {
                    e.printStackTrace();
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