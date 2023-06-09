package com.otosone.bsscommunicator.navFragments;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.otosone.bsscommunicator.utils.HexToBinUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class StatusFragment extends Fragment {
    private BluetoothConnectionService bluetoothConnectionService;
    private FragmentStatusBinding binding;
    private boolean isBound = false;
    private LinearLayout layout1, layout2, layout3, layout4, layout5, layout6, layout7, layout8, layout9, layout10, layout11, layout12, layout13, layout14, layout15, layout16;
    private TextView charger1Tv, charger2Tv, charger3Tv, charger4Tv, charger5Tv, charger6Tv, charger7Tv, charger8Tv, charger9Tv, charger10Tv, charger11Tv, charger12Tv, charger13Tv, charger14Tv, charger15Tv, charger16Tv;
    private TextView isLock1Tv, isLock2Tv, isLock3Tv, isLock4Tv, isLock5Tv, isLock6Tv, isLock7Tv, isLock8Tv, isLock9Tv, isLock10Tv, isLock11Tv, isLock12Tv, isLock13Tv, isLock14Tv, isLock15Tv, isLock16Tv;
    private TextView socketBssIdTv, statusTempTv, statusFanTv, statusHeaterTv, statusDoorTv, statusHumidityTv, statusMqttTv, statusRestTv, statusLocalTv;
    private ImageButton statusRefreshIv;
    public Queue<JSONObject> requestQueue;
    private int retryCount = 0;
    private static final int MAX_RETRY = 2;
    private Handler retryHandler = new Handler();
    private Runnable retryRunnable;
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
            isBound = true;
            Log.d("StationFragment", "Service connected");
            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {
                Log.d("StationFragment", "MessageReceivedListener called");
                getActivity().runOnUiThread(() -> {
                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);
                        processResponse(receivedJson);
                    } catch (JSONException e) {
                        Log.e("StationFragment", "Error parsing received JSON", e);
                    }
                });
            });

            bluetoothConnectionService.setDeviceConnectedListener(connection -> {
                Log.d("StatusFragment", "DeviceConnectedListener triggered");
                initiateRequests();
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothConnectionService = null;
            isBound = false;
            Log.d("StationFragment", "Service disconnected");
        }
    };


    public void executeRefresh() {
        //rotateImageButton(statusRefreshIv);
        initiateRequests();
    }

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
        setDefaultStatus();

        statusRefreshIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImageButton(statusRefreshIv);
                initiateRequests();
            }
        });
        return root;
    }

    private void rotateImageButton(ImageButton imageButton) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageButton, "rotation", 0f, 360f);
        objectAnimator.setDuration(1000); // Set duration to 1 second
        objectAnimator.start();
    }

    public void initiateRequests() {
        Log.d("StatusFragment", "initiateRequests...");
        requestQueue = new LinkedList<>();

        // Add the requests to the queue
        addRequestToQueue("INFO");
        addRequestToQueue("BSS_STATUS");

        for (int i = 0; i < 16; i++) {
            JSONObject dataJson = new JSONObject();
            try {
                dataJson.put("index", i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            addRequestToQueue("SOCKET_STATUS", dataJson);
        }

        // Send the first request
        sendNextRequest();
    }

    private void addRequestToQueue(String requestType) {
        addRequestToQueue(requestType, null);
    }

    private void addRequestToQueue(String requestType, @Nullable JSONObject data) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("request", requestType);
            if (data != null) {
                requestJson.put("data", data);
            }
            requestQueue.add(requestJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendNextRequest() {
        if (!requestQueue.isEmpty() && isBound && bluetoothConnectionService != null) {
            JSONObject nextRequest = requestQueue.peek();
            bluetoothConnectionService.sendMessage(nextRequest.toString());
            startRetryTimer();
        } else {
            Log.e("StationFragment", "BluetoothConnectionService is not bound or the queue is empty");
        }
    }

    private void startRetryTimer() {
        retryCount = 0;
        retryRunnable = new Runnable() {
            @Override
            public void run() {
                retryCount++;
                if (retryCount > MAX_RETRY) {
                    Log.e("StationFragment", "Maximum retries reached for request: " + requestQueue.peek().toString());
                    requestQueue.poll(); // Remove the failed request from the queue
                    //sendNextRequest(); // Send the next request
                } else {
                    JSONObject currentRequest = requestQueue.peek();
                    bluetoothConnectionService.sendMessage(currentRequest.toString());
                    //retryHandler.postDelayed(this, 1000); // Retry after 1 second
                }
            }
        };
        //retryHandler.postDelayed(retryRunnable, 1000); // Start the retry timer
    }

    private void cancelRetryTimer() {
        retryHandler.removeCallbacks(retryRunnable); // Stop the retry timer
    }

    private void retryCurrentRequest() {
        if (!requestQueue.isEmpty() && isBound && bluetoothConnectionService != null) {
            if (retryCount < MAX_RETRY) {
                new Handler().postDelayed(() -> {
                    JSONObject currentRequest = requestQueue.peek();
                    bluetoothConnectionService.sendMessage(currentRequest.toString());
                    retryCount++;
                },1000);
            } else {
                Log.e("StationFragment", "Maximum retries reached for request: " + requestQueue.peek().toString());
            }
        } else {
            Log.e("StationFragment", "BluetoothConnectionService is not bound or the queue is empty");
        }
    }

    private void processResponse(JSONObject response) {
        try {
            if (response.has("response")) {
                String result = response.getString("result");
                if ("ok".equals(result)) {
                    cancelRetryTimer();
                    requestQueue.poll(); // Remove the handled request from the queue
                    sendNextRequest(); // Send the next request
                } else {
                    retryCurrentRequest();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Observe the allDataReceived LiveData
        DataHolder.getInstance().getAllDataReceived().observe(getViewLifecycleOwner(), allDataReceived -> {
            if (allDataReceived != null && allDataReceived) {
                Log.d("allDataReceived", "All data received. Updating UI.");

                // Get the latest bssStatus and socketStatusMap
                JSONObject bssStatus = DataHolder.getInstance().getBssStatus().getValue();
                JSONObject info = DataHolder.getInstance().getInfo().getValue();
                Map<String, JSONObject> socketStatusMap = DataHolder.getInstance().getSocketStatusMap().getValue();
                if (info != null) {
                    Log.d("info", info.toString());
                }

                if (bssStatus != null && info != null) {
                    try {
                        String apkVersion = info.getString("apkVersion");
                        Log.d("bssinfo", bssStatus.toString());
                        bssStatus(bssStatus, apkVersion);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (socketStatusMap != null) {
                    // Create a copy of the map entries
                    Set<Map.Entry<String, JSONObject>> entriesCopy = new HashSet<>(socketStatusMap.entrySet());
                    Log.d("statusinfo", socketStatusMap.toString());
                    for (Map.Entry<String, JSONObject> entry : entriesCopy) {
                        socketStatus(entry.getValue());
                    }
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        bindBluetoothConnectionService();

        DataHolder.getInstance().getAllDataReceived().observe(getViewLifecycleOwner(), allDataReceived -> {
            if (allDataReceived != null && allDataReceived) {
                Log.d("allDataReceived", "All data received. Updating UI.");

                // Get the latest bssStatus and socketStatusMap
                JSONObject bssStatus = DataHolder.getInstance().getBssStatus().getValue();
                JSONObject info = DataHolder.getInstance().getInfo().getValue();
                Map<String, JSONObject> socketStatusMap = DataHolder.getInstance().getSocketStatusMap().getValue();

                if (info != null) {
                    Log.d("info", info.toString());
                }

                if (bssStatus != null && info != null) {
                    try {
                        String apkVersion = info.getString("apkVersion");
                        Log.d("bssinfo", bssStatus.toString());
                        bssStatus(bssStatus, apkVersion);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (socketStatusMap != null) {
                    // Create a copy of the map entries
                    Set<Map.Entry<String, JSONObject>> entriesCopy = new HashSet<>(socketStatusMap.entrySet());
                    Log.d("statusinfo", socketStatusMap.toString());
                    for (Map.Entry<String, JSONObject> entry : entriesCopy) {
                        socketStatus(entry.getValue());
                    }
                }

                // Reset allDataReceived and other data after updating the UI
                //DataHolder.getInstance().resetData();
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
        statusHumidityTv.setText(String.valueOf(defaultHumidity) + "\r\r%\r");

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

    private void bssStatus(JSONObject bssStatus, String apkVersion) {
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
            socketBssIdTv.setText(stationId + "(" + "V" + apkVersion + ")");
            statusTempTv.setText(String.valueOf(averageTemperature) + "\r°C\r");
            statusHumidityTv.setText(String.valueOf(humidity) + "\r\r%\r");

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
            int soc = Math.round((float)dataObject.getJSONObject("bms").getInt("soc") / 10);

            String status = dataObject.getString("status");
            String binaryStatus = HexToBinUtil.hexToBin(status);
            char lockBit = binaryStatus.length() >= 5 ? binaryStatus.charAt(4) : '0'; // remember indices start from 0
            String lockStatus = lockBit == '0' ? "UNLOCK" : "LOCK";



            // Set background color
            if (binaryStatus.charAt(0) == '0' || binaryStatus.charAt(1) == '0') {
                layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#202124"), 60));
            } else {
                switch (charging) {
                    case 0:
                        layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#BFBEBD"), 60));
                        break;
                    case 1:
                    case 2:
                    case 3:
                        layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#FE423E"), 60));
                        break;
                    case 4:
                        if (soc > 95) {
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#27B6FF"), 60));
                        } else {
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#FE423E"), 60));
                        }
                        break;
                    case 6:
                        char yellowBackground = binaryStatus.charAt(3);
                        if(yellowBackground == '1'){
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#FFC20A"), 60));
                        } else {
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#FFC20A"), 60));
                        }
                        break;
                    default:
                        layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#BFBEBD"), 60));
                        break;
                }
            }

            // Set charger info and lock status
            chargerTextViews[index].setText(String.valueOf(soc));
            lockTextViews[index].setText(lockStatus);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    Drawable getRoundedCornerDrawable(int color, float radius) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(color);
        gradientDrawable.setCornerRadius(radius);
        return gradientDrawable;
    }
    @Override
    public void onPause() {
        super.onPause();
        cancelRetryTimer(); // Stop the retry timer when the fragment is paused
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

