package com.otosone.bssmgr.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.otosone.bssmgr.R;
import com.otosone.bssmgr.bluetooth.BluetoothConnectionService;
import com.otosone.bssmgr.databinding.FragmentSocketStatusListBinding;
import com.otosone.bssmgr.listAdapters.BasicAdapter;
import com.otosone.bssmgr.listAdapters.Bms1Adapter;
import com.otosone.bssmgr.listAdapters.Bms2Adapter;
import com.otosone.bssmgr.listAdapters.Bms3Adapter;
import com.otosone.bssmgr.listAdapters.ChargerAdapter;
import com.otosone.bssmgr.listAdapters.StatusAdapter;
import com.otosone.bssmgr.utils.DataHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SocketStatusListFragment extends Fragment {

    private FragmentSocketStatusListBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private Button statusRefreshBtn;
    private TextView basicTv, statusTv, chargerTv, bms1Tv, bms2Tv, bms3Tv;
    private ListView statusListListview;
    private View currentView;
    public Queue<JSONObject> requestQueue;
    private int retryCount = 0;
    private Runnable retryRunnable;
    private static final int STATION_ID_LENGTH = 16;
    private static final int CELL_V_LENGTH = 10;
    private static final int CELL_T_LENGTH = 6;
    private static final int CYCLE_LENGTH = 2;
    private static final int MAX_RETRY = 3;
    private Handler retryHandler = new Handler();

    public SocketStatusListFragment() {
        // Required empty public constructor
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

            // If requestQueue is empty, all requests are processed.
            if(requestQueue.isEmpty()) {
                // Perform your action here
                // In your case, showing a Toast message
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "All requests processed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_socket_status_list, container, false);
        View root = binding.getRoot();
        Databind();


        statusRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(currentView != null) {
                    initiateRequests();
                    currentView.callOnClick();
                }
            }
        });


        basicTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(statusTv);
                removeUnderline(chargerTv);
                removeUnderline(bms1Tv);
                removeUnderline(bms2Tv);
                removeUnderline(bms3Tv);

                underlineTextView(basicTv);

                Map<String, JSONObject> map = DataHolder.getInstance().getSocketStatusMapDirectly();
                if (map != null) {
                    List<JSONObject> basicData = new ArrayList<>();

                    for (JSONObject obj : map.values()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();
                            simplifiedObj.put("index", obj.getInt("index"));
                            simplifiedObj.put("serial", obj.getJSONObject("bms").getString("serial"));
                            simplifiedObj.put("fwVer", obj.getJSONObject("cboard").getString("fwVer"));
                            basicData.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    BasicAdapter basicAdapter = new BasicAdapter(getContext(), basicData);
                    statusListListview.setAdapter(basicAdapter);
                }
            }
        });



        statusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(basicTv);
                removeUnderline(chargerTv);
                removeUnderline(bms1Tv);
                removeUnderline(bms2Tv);
                removeUnderline(bms3Tv);

                underlineTextView(statusTv);

                Map<String, String> map = DataHolder.getInstance().getBinaryStatusMapDirectly();

                if (map != null) {
                    List<JSONObject> statusData = new ArrayList<>();

                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();

                            // Get binary status string directly
                            String binaryStatus = entry.getValue();

                            // Extract necessary bits
                            int CAN = binaryStatus.charAt(31) - '0';
                            int CHG = binaryStatus.charAt(30) - '0';
                            int BMS = binaryStatus.charAt(29) - '0';
                            int BAT = binaryStatus.charAt(28) - '0';
                            int LOCK = binaryStatus.charAt(27) - '0';
                            int DOOR = binaryStatus.charAt(26) - '0';
                            int Status_CHG = binaryStatus.charAt(25) - '0';

                            simplifiedObj.put("index", entry.getKey());
                            simplifiedObj.put("CAN", CAN);
                            simplifiedObj.put("CHG", CHG);
                            simplifiedObj.put("BMS", BMS);
                            simplifiedObj.put("BAT", BAT);
                            simplifiedObj.put("LOCK", LOCK);
                            simplifiedObj.put("DOOR", DOOR);
                            simplifiedObj.put("Status_CHG", Status_CHG);

                            statusData.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    StatusAdapter statusAdapter = new StatusAdapter(getContext(), statusData);
                    statusListListview.setAdapter(statusAdapter);
                }
            }
        });


        chargerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(basicTv);
                removeUnderline(statusTv);
                removeUnderline(bms1Tv);
                removeUnderline(bms2Tv);
                removeUnderline(bms3Tv);

                underlineTextView(chargerTv);

                Map<String, JSONObject> map = DataHolder.getInstance().getSocketStatusMapDirectly();

                if (map != null) {
                    // Convert the values of the map into a list
                    List<JSONObject> chargerData = new ArrayList<>();

                    for (JSONObject obj : map.values()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();
                            simplifiedObj.put("index", obj.getInt("index"));
                            simplifiedObj.put("temp", obj.getJSONObject("charger").getInt("temp"));
                            simplifiedObj.put("volt", obj.getJSONObject("charger").getInt("voltage"));
                            simplifiedObj.put("amper", obj.getJSONObject("charger").getInt("current"));
                            simplifiedObj.put("charging", obj.getJSONObject("charger").getInt("charging"));

                            chargerData.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    ChargerAdapter chargerAdapter = new ChargerAdapter(getContext(), chargerData);
                    statusListListview.setAdapter(chargerAdapter);
                }
            }
        });

        bms1Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(basicTv);
                removeUnderline(statusTv);
                removeUnderline(chargerTv);
                removeUnderline(bms2Tv);
                removeUnderline(bms3Tv);

                underlineTextView(bms1Tv);

                Map<String, JSONObject> map = DataHolder.getInstance().getSocketStatusMapDirectly();

                if (map != null) {
                    // Convert the values of the map into a list
                    List<JSONObject> bms1Data = new ArrayList<>();

                    for (JSONObject obj : map.values()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();
                            simplifiedObj.put("index", obj.getInt("index"));
                            simplifiedObj.put("soc", obj.getJSONObject("bms").getInt("soc"));
                            simplifiedObj.put("packv", obj.getJSONObject("bms").getInt("pack_v"));
                            simplifiedObj.put("packa", obj.getJSONObject("bms").getInt("pack_a"));
                            simplifiedObj.put("cycle", obj.getJSONObject("bms").getJSONArray("cycle")); // Fetching cycle as an array
                            simplifiedObj.put("alarm", obj.getJSONObject("bms").getString("alarm")); // Fetching alarm as a string

                            bms1Data.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Bms1Adapter bms1Adapter = new Bms1Adapter(getContext(), bms1Data);
                    statusListListview.setAdapter(bms1Adapter);
                }
            }
        });


        bms2Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(basicTv);
                removeUnderline(statusTv);
                removeUnderline(chargerTv);
                removeUnderline(bms1Tv);
                removeUnderline(bms3Tv);

                underlineTextView(bms2Tv);

                Map<String, JSONObject> map = DataHolder.getInstance().getSocketStatusMapDirectly();

                if (map != null) {
                    // Convert the values of the map into a list
                    List<JSONObject> bms2Data = new ArrayList<>();

                    for (JSONObject obj : map.values()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();
                            simplifiedObj.put("index", obj.getInt("index"));

                            // Get the "bms" object
                            JSONObject bmsObject = obj.getJSONObject("bms");

                            // Get the "cell_v" array from "bms" object
                            JSONArray cellVArray = bmsObject.getJSONArray("cell_v");

                            // Put the "cell_v" array into simplifiedObj
                            simplifiedObj.put("cell_v", cellVArray);

                            bms2Data.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Bms2Adapter bms2Adapter = new Bms2Adapter(getContext(), bms2Data);
                    statusListListview.setAdapter(bms2Adapter);
                }
            }
        });

        bms3Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(basicTv);
                removeUnderline(statusTv);
                removeUnderline(chargerTv);
                removeUnderline(bms1Tv);
                removeUnderline(bms2Tv);

                underlineTextView(bms3Tv);

                Map<String, JSONObject> map = DataHolder.getInstance().getSocketStatusMapDirectly();

                if (map != null) {
                    // Convert the values of the map into a list
                    List<JSONObject> bms3Data = new ArrayList<>();

                    for (JSONObject obj : map.values()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();
                            simplifiedObj.put("index", obj.getInt("index"));

                            // Get the "bms" object
                            JSONObject bmsObject = obj.getJSONObject("bms");

                            // Get the "cell_v" array from "bms" object
                            JSONArray cellVArray = bmsObject.getJSONArray("cell_t");

                            // Put the "cell_v" array into simplifiedObj
                            simplifiedObj.put("cell_t", cellVArray);

                            bms3Data.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Bms3Adapter bms3Adapter = new Bms3Adapter(getContext(), bms3Data);
                    statusListListview.setAdapter(bms3Adapter);
                }
            }
        });

        basicTv.callOnClick();

        return root;
    }

    private void Databind() {
        statusRefreshBtn = binding.statusRefreshBtn;
        basicTv = binding.basicTv;
        statusTv = binding.statusTv;
        chargerTv = binding.chargerTv;
        bms1Tv = binding.bms1Tv;
        bms2Tv = binding.bms2Tv;
        bms3Tv = binding.bms3Tv;
        statusListListview = binding.statusListListview;
    }

    private void underlineTextView(TextView textView) {
        SpannableString spanString = new SpannableString(textView.getText().toString());
        spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
        textView.setText(spanString);
    }

    private void removeUnderline(TextView textView) {
        textView.setText(textView.getText().toString());
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

