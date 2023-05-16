package com.otosone.bsscommunicator.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.databinding.FragmentStatusBinding;
import com.otosone.bsscommunicator.utils.DataHolder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class StatusFragment extends Fragment {
    private BluetoothConnectionService bluetoothConnectionService;
    private FragmentStatusBinding binding;
    private boolean isBound = false;
    private LinearLayout layout1, layout2, layout3, layout4, layout5, layout6, layout7, layout8, layout9, layout10, layout11, layout12, layout13, layout14, layout15, layout16;
    private TextView charger1Tv, charger2Tv, charger3Tv, charger4Tv, charger5Tv, charger6Tv, charger7Tv, charger8Tv, charger9Tv, charger10Tv, charger11Tv, charger12Tv, charger13Tv, charger14Tv, charger15Tv, charger16Tv;
    private TextView isLock1Tv, isLock2Tv, isLock3Tv, isLock4Tv, isLock5Tv, isLock6Tv, isLock7Tv, isLock8Tv, isLock9Tv, isLock10Tv, isLock11Tv, isLock12Tv, isLock13Tv, isLock14Tv, isLock15Tv, isLock16Tv;
    private TextView socketBssIdTv, statusTempTv, statusFanTv, statusHeaterTv, statusDoorTv, statusHumidityTv, statusMqttTv, statusRestTv, statusLocalTv;
    private ImageView statusRefreshIv;
    public static StatusFragment newInstance(String param1, String param2) {
        StatusFragment fragment = new StatusFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) iBinder;
            bluetoothConnectionService = binder.getService();
            isBound = true;;
            setDefaultStatus();
            Log.d("StationFragment", "Service connected");
            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {
                Log.d("StationFragment", "MessageReceivedListener called");
                getActivity().runOnUiThread(() -> {
                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        // Check if it's a request
                        if(receivedJson.has("request")) {
                            String requestType = receivedJson.getString("request");
                            switch (requestType) {
                                case "BSS_STATUS":
                                    //bssStatus(completeJsonString);
                                    break;
                                case "SOCKET_STATUS":
                                    //socketStatus(completeJsonString);
                                    break;
                                // Add more cases as needed
                            }
                        }

                        // Check if it's a response
                        else if(receivedJson.has("response")) {
                            String responseType = receivedJson.getString("response");
                            String result = receivedJson.getString("result");
                            int errorCode = receivedJson.getInt("error_code");
                            if (result.equals("ok") && errorCode == 0) {
                                switch (responseType) {
                                    case "BSS_STATUS":
                                        //bssStatus(completeJsonString);
                                        break;
                                    case "SOCKET_STATUS":
                                        //socketStatus(completeJsonString);
                                        break;
                                    // Add more cases as needed
                                }
                            }else {
                                // Handle error case here
                                Log.e("StatusFragment", "Received error: result = " + result + ", error_code = " + errorCode);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("StatusFragment", "Error parsing received JSON", e);
                    }
                });
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_status, container, false);
        View root = binding.getRoot();
        Databind();

        statusRefreshIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Extract values from EditText views

                // Create a JSON object for BSS status
                JSONObject bssJson = new JSONObject();
                try {
                    bssJson.put("request", "BSS_STATUS");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Call the sendAsciiMessage method with the string as an argument
                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(bssJson.toString());
                } else {
                    Log.e("StationFragment", "BluetoothConnectionService is not bound");
                    return;
                }

                // Create a JSON object for SOCKET status
                JSONObject socketJson = new JSONObject();
                try {
                    socketJson.put("request", "SOCKET_STATUS");
                    JSONObject dataJson = new JSONObject();
                    for (int i = 0; i < 16; i++) {
                        dataJson.put("index", i);
                        socketJson.put("data", dataJson);
                        String socketString = socketJson.toString();

                        // Call the sendAsciiMessage method with the string as an argument
                        if (isBound && bluetoothConnectionService != null) {
                            bluetoothConnectionService.sendMessage(socketString);
                        } else {
                            Log.e("StationFragment", "BluetoothConnectionService is not bound");
                            return;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe the bssStatus LiveData
        DataHolder.getInstance().getBssStatus().observe(getViewLifecycleOwner(), bssStatus -> {
            if (bssStatus != null) {
                bssStatus(bssStatus);
            }
        });

        // Observe the socketStatusMap LiveData
        DataHolder.getInstance().getSocketStatusMap().observe(getViewLifecycleOwner(), socketStatusMap -> {
            if (socketStatusMap != null) {
                for (Map.Entry<String, JSONObject> entry : socketStatusMap.entrySet()) {
                    socketStatus(entry.getValue());
                }
            }
        });
    }

    private void Databind() {
        layout1 = binding.layout1; layout2 = binding.layout2; layout3 = binding.layout3; layout4 = binding.layout4; layout5 = binding.layout5; layout6 = binding.layout6; layout7 = binding.layout7; layout8 = binding.layout8; layout9 = binding.layout9;
        layout10 = binding.layout10; layout11 = binding.layout11; layout12 = binding.layout12; layout13 = binding.layout13; layout14 = binding.layout14; layout15 = binding.layout15; layout16 = binding.layout16;
        charger1Tv = binding.charger1Tv; charger2Tv = binding.charger2Tv; charger3Tv = binding.charger3Tv; charger4Tv = binding.charger4Tv; charger5Tv = binding.charger5Tv;
        charger6Tv = binding.charger6Tv; charger7Tv = binding.charger7Tv; charger8Tv = binding.charger8Tv; charger9Tv = binding.charger9Tv; charger10Tv = binding.charger10Tv;
        charger11Tv = binding.charger11Tv; charger12Tv = binding.charger12Tv; charger13Tv = binding.charger13Tv; charger14Tv = binding.charger14Tv; charger15Tv = binding.charger15Tv; charger16Tv = binding.charger16Tv;
        isLock1Tv = binding.isLock1Tv; isLock2Tv = binding.isLock2Tv; isLock3Tv = binding.isLock3Tv; isLock4Tv = binding.isLock4Tv; isLock5Tv = binding.isLock5Tv; isLock6Tv = binding.isLock6Tv;
        isLock7Tv = binding.isLock7Tv; isLock8Tv = binding.isLock8Tv; isLock9Tv = binding.isLock9Tv; isLock10Tv = binding.isLock10Tv; isLock11Tv = binding.isLock11Tv; isLock12Tv = binding.isLock12Tv;
        isLock13Tv = binding.isLock13Tv; isLock14Tv = binding.isLock14Tv; isLock15Tv = binding.isLock15Tv; isLock16Tv = binding.isLock16Tv;
        socketBssIdTv = binding.socketBssIdTv; statusTempTv = binding.statusTempTv; statusFanTv = binding.statusFanTv; statusHeaterTv = binding.statusHeaterTv; statusDoorTv = binding.statusDoorTv;
        statusHumidityTv = binding.statusHumidityTv; statusMqttTv = binding.statusMqttTv; statusRestTv = binding.statusRestTv; statusLocalTv = binding.statusLocalTv;
        statusRefreshIv = binding.statusRefreshIv;
    }

    private void setDefaultStatus() {
        // Default values
        String defaultStationId = "N/A";
        int defaultTemperature = 0;
        int defaultHumidity = 0;

        socketBssIdTv.setText(defaultStationId);
        statusTempTv.setText(String.valueOf(defaultTemperature) + "\r°C\r");
        statusHumidityTv.setText(String.valueOf(defaultHumidity) + "\r%\r");

        // Set status text and color
        statusFanTv.setText("FAN\r");
        statusHeaterTv.setText("HEATER\r");
        statusDoorTv.setText("DOOR");
        statusMqttTv.setText("\rMQTT\r");
        statusRestTv.setText("REST\r\r");
        statusLocalTv.setText("LOCAL");

        // Set text color to gray for default status
        statusFanTv.setTextColor(Color.GRAY);
        statusHeaterTv.setTextColor(Color.GRAY);
        statusDoorTv.setTextColor(Color.GRAY);
        statusMqttTv.setTextColor(Color.GRAY);
        statusRestTv.setTextColor(Color.GRAY);
        statusLocalTv.setTextColor(Color.GRAY);
    }

    private void bssStatus(JSONObject bssStatus) {
        try {
            JSONObject dataObject = bssStatus;

            String stationId = dataObject.getString("stationId");
            int fan = dataObject.getInt("fan");
            int heater = dataObject.getInt("heater");

            JSONObject temperatureObject = dataObject.getJSONObject("temperature");
            int topTemperature = temperatureObject.getInt("top");
            int midTemperature = temperatureObject.getInt("mid");
            int bottomTemperature = temperatureObject.getInt("bottom");
            int averageTemperature = (topTemperature + midTemperature + bottomTemperature) / 3;

            int humidity = dataObject.getInt("humidity");
            int door = dataObject.getInt("door");

            JSONObject commObject = dataObject.getJSONObject("comm");
            int mqtt = commObject.getInt("mqtt");
            int rest = commObject.getInt("rest");
            int local = commObject.getInt("local");

            // Update TextViews
            socketBssIdTv.setText(stationId);
            statusTempTv.setText(String.valueOf(averageTemperature) + "\r°C\r");
            statusHumidityTv.setText(String.valueOf(humidity) + "\r%\r");

            // Set status text and color
            statusFanTv.setText("FAN\r");
            statusHeaterTv.setText("HEATER\r");
            statusDoorTv.setText("DOOR");
            statusMqttTv.setText("\rMQTT\r");
            statusRestTv.setText("REST\r\r");
            statusLocalTv.setText("LOCAL");

            // Set text color to green if status is 1
            statusFanTv.setTextColor(fan == 0 ? Color.GRAY : Color.GREEN);
            statusHeaterTv.setTextColor(heater == 0 ? Color.GRAY : Color.GREEN);
            statusDoorTv.setTextColor(door == 0 ? Color.GRAY : Color.GREEN);
            statusMqttTv.setTextColor(mqtt == 0 ? Color.GRAY : Color.GREEN);
            statusRestTv.setTextColor(rest == 0 ? Color.GRAY : Color.GREEN);
            statusLocalTv.setTextColor(local == 0 ? Color.GRAY : Color.GREEN);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




    private void socketStatus(JSONObject socketStatus) {
        try {
            JSONObject dataObject = socketStatus;
            int index = dataObject.getInt("index");

            LinearLayout[] layouts = {layout1, layout2, layout3, layout4, layout5, layout6, layout7, layout8, layout9, layout10, layout11, layout12, layout13, layout14, layout15, layout16};
            TextView[] chargerTextViews = {charger1Tv, charger2Tv, charger3Tv, charger4Tv, charger5Tv, charger6Tv, charger7Tv, charger8Tv, charger9Tv, charger10Tv, charger11Tv, charger12Tv, charger13Tv, charger14Tv, charger15Tv, charger16Tv};
            TextView[] lockTextViews = {isLock1Tv, isLock2Tv, isLock3Tv, isLock4Tv, isLock5Tv, isLock6Tv, isLock7Tv, isLock8Tv, isLock9Tv, isLock10Tv, isLock11Tv, isLock12Tv, isLock13Tv, isLock14Tv, isLock15Tv, isLock16Tv};
            JSONObject chargerObject = dataObject.getJSONObject("charger");
            int charging = chargerObject.getInt("charging");
            int soc = dataObject.getJSONObject("bms").getInt("soc");

            String status = dataObject.getString("status");
            String binaryStatus = Integer.toBinaryString(Integer.parseInt(status, 16));
            char lockBit = binaryStatus.length() >= 4 ? binaryStatus.charAt(binaryStatus.length() - 4) : '0';
            String lockStatus = lockBit == '0' ? "UNLOCK" : "LOCK";

            // Set background color
            switch (charging) {
                case 0:
                    layouts[index].setBackgroundColor(Color.GRAY);
                    break;
                case 1:
                case 2:
                case 3:
                    layouts[index].setBackgroundColor(Color.RED);
                    break;
                case 4:
                    if (soc > 95) {
                        layouts[index].setBackgroundColor(Color.parseColor("#87CEEB")); // Skyblue
                    } else {
                        layouts[index].setBackgroundColor(Color.RED);
                    }
                    break;
                case 6:
                    layouts[index].setBackgroundColor(Color.YELLOW);
                    break;
                default:
                    layouts[index].setBackgroundColor(Color.GRAY);
                    break;
            }

            // Set charger info and lock status
            chargerTextViews[index].setText(String.valueOf(soc));
            lockTextViews[index].setText(lockStatus);

        } catch (JSONException e) {
            e.printStackTrace();
        }
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

