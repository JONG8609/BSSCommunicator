package com.otosone.bssmgr.navFragments;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.otosone.bssmgr.R;
import com.otosone.bssmgr.bluetooth.BluetoothConnectionService;
import com.otosone.bssmgr.databinding.FragmentStatusBinding;
import com.otosone.bssmgr.utils.DataHolder;
import com.otosone.bssmgr.utils.HexToBinUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private static final int MAX_RETRY = 3;
    private Handler retryHandler = new Handler();
    private Runnable retryRunnable;

    private static final int STATION_ID_LENGTH = 16;
    private static final int CELL_V_LENGTH = 10;
    private static final int CELL_T_LENGTH = 6;
    private static final int CYCLE_LENGTH = 2;
    public static StatusFragment newInstance(String param1, String param2) {
        StatusFragment fragment = new StatusFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        ExecutorService executor = Executors.newCachedThreadPool();

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) iBinder;
            bluetoothConnectionService = binder.getService();
            isBound = true;

            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {

                executor.submit(() -> {
                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);
                        processResponse(receivedJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            });

            // Set the JsonExceptionListener
            bluetoothConnectionService.setOnJSONExceptionListener(() -> {
                // Call retryCurrentRequest when a JSONException is thrown
                retryCurrentRequest();
            });

            bluetoothConnectionService.setDeviceConnectedListener(connection -> {
                initiateRequests();
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_status, container, false);
        View root = binding.getRoot();
        Databind();
        setDefaultStatus();

        statusRefreshIv.setOnClickListener(new View.OnClickListener() {
            private boolean isClickable = true;
            private int remainingSeconds = 0;
            private Handler handler = new Handler(Looper.getMainLooper());
            private Runnable enableButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    isClickable = true;
                    remainingSeconds = 0;
                }
            };
            private Runnable countdownRunnable = new Runnable() {
                @Override
                public void run() {
                    if (remainingSeconds > 0) {
                        remainingSeconds--;
                        if (remainingSeconds <= 10) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "You can click again in " + remainingSeconds + " seconds", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        handler.postDelayed(this, 1000); // 1000 milliseconds = 1 second
                    }
                }
            };

            @Override
            public void onClick(View v) {
                if (isClickable) {
                    isClickable = false;
                    remainingSeconds = 60; // 1 minute = 60 seconds
                    rotateImageButton(statusRefreshIv);
                    initiateRequests();
                    handler.postDelayed(enableButtonRunnable, 0); // 60000 milliseconds = 1 minute
                    handler.postDelayed(countdownRunnable, 1000); // 1000 milliseconds = 1 second
                } else if (remainingSeconds <= 60) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "You can click again in " + remainingSeconds + " seconds.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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


        if (requestQueue.isEmpty()) {

        } else if (!isBound) {

        } else if (bluetoothConnectionService == null) {

        } else {
            if (retryCount < MAX_RETRY) {


                // Use the Looper of the main thread
                Handler handler = new Handler(Looper.getMainLooper());



                handler.postDelayed(() -> {


                    JSONObject currentRequest = requestQueue.peek();
                    bluetoothConnectionService.sendMessage(currentRequest.toString());



                    retryCount++;
                }, 1000);
            } else {
                Log.e("StationFragment", "Maximum retries reached for request: " + requestQueue.peek().toString());
            }
        }
    }
    //Validation process
    private void processResponse(JSONObject response) {
        try {
            // common validation for all types of responses
            if (response.has("response") && response.has("result") && response.has("error_code") && response.has("data")) {
                String result = response.getString("result");
                String responseType = response.getString("response");
                JSONObject data = response.getJSONObject("data");

                // INFO validation
                if (responseType.equals("INFO")) {
                    if (data.has("stationId") && data.has("apkVersion")) {
                        String stationId = data.getString("stationId");
                        String apkVersion = data.getString("apkVersion");

                        // Regular expression to validate apkVersion
                        String regex = "\\d+\\.\\d{2}"; // any number of digits, followed by a dot, followed by exactly two digits

                        // Additional checks for stationId and apkVersion
                        if(stationId != null && stationId.length() == STATION_ID_LENGTH
                                && apkVersion != null && !apkVersion.isEmpty() && apkVersion.matches(regex)) {

                            // process INFO response
                            if (result.equals("ok")) {
                                cancelRetryTimer();
                                requestQueue.poll();
                                sendNextRequest();
                            }
                        }
                    }
                }
                // BSS_STATUS validation
                else if (responseType.equals("BSS_STATUS")
                        && data.has("stationId")
                        && data.has("fan")
                        && data.has("heater")
                        && data.has("temperature")
                        && data.has("humidity")
                        && data.has("door")
                        && data.has("comm")
                        && data.has("emergency")) {

                    // Type checks
                    if(data.getString("stationId").length() == STATION_ID_LENGTH
                            && data.get("fan") instanceof Integer
                            && data.get("heater") instanceof Integer
                            && data.get("temperature") instanceof JSONObject
                            && data.get("humidity") instanceof Integer
                            && data.get("door") instanceof Integer
                            && data.get("comm") instanceof JSONObject
                            && data.get("emergency") instanceof Integer) {

                        JSONObject temperature = data.getJSONObject("temperature");
                        JSONObject comm = data.getJSONObject("comm");

                        // Further nested checks for temperature and comm
                        if(temperature.has("top") && temperature.has("mid") && temperature.has("bottom")
                                && comm.has("mqtt") && comm.has("rest") && comm.has("local")
                                && temperature.get("top") instanceof Integer
                                && temperature.get("mid") instanceof Integer
                                && temperature.get("bottom") instanceof Integer
                                && comm.get("mqtt") instanceof Integer
                                && comm.get("rest") instanceof Integer
                                && comm.get("local") instanceof Integer) {

                            // process BSS_STATUS response
                            if (result.equals("ok")) {
                                cancelRetryTimer();
                                requestQueue.poll();
                                sendNextRequest();
                            }
                        }
                    }
                }
                // SOCKET_STATUS validation
                else if (responseType.equals("SOCKET_STATUS")
                        && data.has("index")
                        && data.has("status")
                        && data.has("cboard")
                        && data.has("charger")
                        && data.has("bms")) {

                    // Type checks
                    if(data.get("index") instanceof Integer
                            && data.get("status") instanceof String
                            && data.get("cboard") instanceof JSONObject
                            && data.get("charger") instanceof JSONObject
                            && data.get("bms") instanceof JSONObject) {

                        JSONObject charger = data.getJSONObject("charger");
                        JSONObject bms = data.getJSONObject("bms");

                        // Further nested checks for charger, and bms
                        if(charger.has("temp") && charger.has("voltage") && charger.has("current") && charger.has("charging")
                                && bms.has("serial") && bms.has("country") && bms.has("factory") && bms.has("soc")
                                && bms.has("pack_v") && bms.has("pack_a") && bms.has("cell_v") && bms.has("cell_t")
                                && bms.has("cycle") && bms.has("alarm")
                                && charger.get("temp") instanceof Integer
                                && charger.get("voltage") instanceof Integer
                                && charger.get("current") instanceof Integer
                                && charger.get("charging") instanceof Integer
                                && bms.get("serial") instanceof String
                                && bms.get("country") instanceof Integer
                                && bms.get("factory") instanceof Integer
                                && bms.get("soc") instanceof Integer
                                && bms.get("pack_v") instanceof Integer
                                && bms.get("pack_a") instanceof Integer
                                && bms.get("cell_v") instanceof JSONArray
                                && bms.get("cell_t") instanceof JSONArray
                                && bms.get("cycle") instanceof JSONArray
                                && bms.get("alarm") instanceof String) {

                            // Check cell_v, cell_t and cycle array length.
                            JSONArray cell_v = bms.getJSONArray("cell_v");
                            JSONArray cell_t = bms.getJSONArray("cell_t");
                            JSONArray cycle = bms.getJSONArray("cycle");

                            if(cell_v.length() == CELL_V_LENGTH && cell_t.length() == CELL_T_LENGTH && cycle.length() == CYCLE_LENGTH) {
                                // process SOCKET_STATUS response
                                if (result.equals("ok")) {
                                    cancelRetryTimer();
                                    requestQueue.poll();
                                    sendNextRequest();
                                }
                            }
                        }
                    }
                }
                else {
                    // If JSON format does not match any expected response format, retry or throw an error.
                    retryCurrentRequest();
                }
            } else {
                Log.d("errr", "errr");
                retryCurrentRequest();
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

                // Get the latest bssStatus and socketStatusMap
                JSONObject bssStatus = DataHolder.getInstance().getBssStatus().getValue();
                JSONObject info = DataHolder.getInstance().getInfo().getValue();
                Map<String, JSONObject> socketStatusMap = DataHolder.getInstance().getSocketStatusMap().getValue();
                if (info != null) {
                }

                if (bssStatus != null && info != null) {
                    try {
                        String apkVersion = info.getString("apkVersion");
                        bssStatus(bssStatus, apkVersion);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (socketStatusMap != null) {
                    // Create a copy of the map entries
                    Set<Map.Entry<String, JSONObject>> entriesCopy = new HashSet<>(socketStatusMap.entrySet());
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

                // Get the latest bssStatus and socketStatusMap
                JSONObject bssStatus = DataHolder.getInstance().getBssStatus().getValue();
                JSONObject info = DataHolder.getInstance().getInfo().getValue();
                Map<String, JSONObject> socketStatusMap = DataHolder.getInstance().getSocketStatusMap().getValue();

                if (info != null) {
                }

                if (bssStatus != null && info != null) {
                    try {
                        String apkVersion = info.getString("apkVersion");
                        bssStatus(bssStatus, apkVersion);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (socketStatusMap != null) {
                    // Create a copy of the map entries
                    Set<Map.Entry<String, JSONObject>> entriesCopy = new HashSet<>(socketStatusMap.entrySet());
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
        layout1 = binding.layout1;layout2 = binding.layout2;layout3 = binding.layout3;layout4 = binding.layout4;layout5 = binding.layout5;layout6 = binding.layout6;layout7 = binding.layout7;layout8 = binding.layout8;
        layout9 = binding.layout9;layout10 = binding.layout10;layout11 = binding.layout11;layout12 = binding.layout12;layout13 = binding.layout13;layout14 = binding.layout14;layout15 = binding.layout15;
        layout16 = binding.layout16;charger1Tv = binding.charger1Tv;charger2Tv = binding.charger2Tv;charger3Tv = binding.charger3Tv;charger4Tv = binding.charger4Tv;charger5Tv = binding.charger5Tv;charger6Tv = binding.charger6Tv;
        charger7Tv = binding.charger7Tv;charger8Tv = binding.charger8Tv;charger9Tv = binding.charger9Tv;charger10Tv = binding.charger10Tv;charger11Tv = binding.charger11Tv;charger12Tv = binding.charger12Tv;
        charger13Tv = binding.charger13Tv;charger14Tv = binding.charger14Tv;charger15Tv = binding.charger15Tv;charger16Tv = binding.charger16Tv;isLock1Tv = binding.isLock1Tv;isLock2Tv = binding.isLock2Tv;
        isLock3Tv = binding.isLock3Tv;isLock4Tv = binding.isLock4Tv;isLock5Tv = binding.isLock5Tv;isLock6Tv = binding.isLock6Tv;isLock7Tv = binding.isLock7Tv;isLock8Tv = binding.isLock8Tv;isLock9Tv = binding.isLock9Tv;
        isLock10Tv = binding.isLock10Tv;isLock11Tv = binding.isLock11Tv;isLock12Tv = binding.isLock12Tv;isLock13Tv = binding.isLock13Tv;isLock14Tv = binding.isLock14Tv;isLock15Tv = binding.isLock15Tv;
        isLock16Tv = binding.isLock16Tv;socketBssIdTv = binding.socketBssIdTv;statusTempTv = binding.statusTempTv;statusFanTv = binding.statusFanTv;statusHeaterTv = binding.statusHeaterTv;statusDoorTv = binding.statusDoorTv;
        statusHumidityTv = binding.statusHumidityTv;statusMqttTv = binding.statusMqttTv;statusRestTv = binding.statusRestTv;statusLocalTv = binding.statusLocalTv;statusRefreshIv = binding.statusRefreshIv;
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
        String binaryStatus = "";
        String indexString = "";
        int soc = 0; // default value
        JSONObject dataObject = socketStatus;
        try {

            int index = dataObject.getInt("index");
            indexString = Integer.toString(index);
            LinearLayout[] layouts = {layout1, layout2, layout3, layout4, layout5, layout6, layout7, layout8, layout9, layout10, layout11, layout12, layout13, layout14, layout15, layout16};
            TextView[] chargerTextViews = {charger1Tv, charger2Tv, charger3Tv, charger4Tv, charger5Tv, charger6Tv, charger7Tv, charger8Tv, charger9Tv, charger10Tv, charger11Tv, charger12Tv, charger13Tv, charger14Tv, charger15Tv, charger16Tv};
            TextView[] lockTextViews = {isLock1Tv, isLock2Tv, isLock3Tv, isLock4Tv, isLock5Tv, isLock6Tv, isLock7Tv, isLock8Tv, isLock9Tv, isLock10Tv, isLock11Tv, isLock12Tv, isLock13Tv, isLock14Tv, isLock15Tv, isLock16Tv};
            JSONObject chargerObject = dataObject.getJSONObject("charger");
            int charging = chargerObject.getInt("charging");

            if (dataObject.getJSONObject("bms").has("soc")) {
                soc = Math.round((float) dataObject.getJSONObject("bms").getInt("soc") / 10);
            }

            String status = dataObject.getString("status");
            binaryStatus = HexToBinUtil.hexToBin(status);


            char lockBit = binaryStatus.length() >= 31 ? binaryStatus.charAt(27) : '0';
            String lockStatus = lockBit == '0' ? "UNLOCK" : "LOCK";


            // Set background color
            if (binaryStatus.charAt(30) == '0' || binaryStatus.charAt(31) == '0') {

                layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#202124"), 60));// Dark Grey / Black
            } else if(isAllOne(binaryStatus, 31, 27) && !isAllZeroes(binaryStatus, 0, 11)) {
                layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#FFC20A"), 60));// Yellow
            }else
                switch (charging) {
                    case 0:
                        if(binaryStatus.charAt(29) == '0' && binaryStatus.charAt(28) == '0' && isAllZeroes(binaryStatus, 0, 11)) {
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#BFBEBD"), 60));// Light Grey
                        }else if(isAllOne(binaryStatus, 31, 27) && binaryStatus.charAt(25) == '0' && !isAllZeroes(binaryStatus, 0, 11)){
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#FFC20A"), 60));// Yellow
                    }else {
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#BFBEBD"), 60));// Light Grey
                        }
                        break;
                    case 1:

                        if(isAllOne(binaryStatus, 31, 27) && binaryStatus.charAt(25) == '1' && isAllZeroes(binaryStatus, 0, 11)){
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#FE423E"), 60));// Bright Red
                        }

                    case 2:
                        if(isAllOne(binaryStatus, 31, 27) && binaryStatus.charAt(25) == '1' && isAllZeroes(binaryStatus, 0, 11)){
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#FE423E"), 60));// Bright Red
                        }
                    case 3:
                        if(isAllOne(binaryStatus, 31, 27) && binaryStatus.charAt(25) == '1' && isAllZeroes(binaryStatus, 0, 11)){
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#FE423E"), 60));// Bright Red
                        }
                        break;
                    case 4:
                        if (soc > 95) {
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#27B6FF"), 60));// Bright Blue
                        } else {
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#FE423E"), 60));// Bright Red
                        }
                        break;
                    case 6:
                        if(isAllOne(binaryStatus, 31, 27))
                            layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#FFC20A"), 60));// Yellow

                        break;
                    default:
                        layouts[index].setBackground(getRoundedCornerDrawable(Color.parseColor("#BFBEBD"), 60));// Light Grey
                        break;
                }




            // Set charger info and lock status
            chargerTextViews[index].setText(String.valueOf(soc));
            lockTextViews[index].setText(lockStatus);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean isAllZeroes(String binaryStatus, int start, int end) {
        for (int i = start; i <= end; i++) {
            if (binaryStatus.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }

    private boolean isAllOne(String binaryStatus, int start, int end) {
        for (int i = start; i <= end; i++) {
            if (binaryStatus.charAt(i) != '1') {
                return false;
            }
        }
        return true;
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

