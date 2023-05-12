package com.otosone.bsscommunicator.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.databinding.FragmentStationBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class StationFragment extends Fragment {

    FragmentStationBinding binding;

    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;

    private EditText reportPeriodEt, batteryInTimeoutEt, batteryOutTimeoutEt, paymentTimeoutEt, paymentTypeEt;
    private Button stationBtn;

    public StationFragment() {
        // Required empty public constructor
    }

    public static StationFragment newInstance(String param1, String param2) {
        StationFragment fragment = new StationFragment();
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

                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("request") && receivedJson.getString("request").equals("STA_CFG")) {
                            // Create a response JSON object
                            JSONObject responseJson = new JSONObject();
                            responseJson.put("response", "STA_CFG");
                            responseJson.put("result", "ok");
                            responseJson.put("error_code", 0);

                            JSONObject dataObject = new JSONObject();
                            dataObject.put("reportPeriod", Integer.parseInt(reportPeriodEt.getText().toString()));
                            dataObject.put("batInTimeout", Integer.parseInt(batteryInTimeoutEt.getText().toString()));
                            dataObject.put("batOutTimeout", Integer.parseInt(batteryOutTimeoutEt.getText().toString()));
                            dataObject.put("payTimeout", Integer.parseInt(paymentTimeoutEt.getText().toString()));
                            dataObject.put("payType", Integer.parseInt(paymentTypeEt.getText().toString()));

                            responseJson.put("data", dataObject);

                            // Send the response to the server
                            bluetoothConnectionService.sendMessage(responseJson.toString());
                        }

                        if (receivedJson.has("response") && receivedJson.getString("response").equals("STA_CFG")) {
                            String result = receivedJson.getString("result");
                            int errorCode = receivedJson.getInt("error_code");

                            if (result.equals("ok") && errorCode == 0) {
                                JSONObject responseJson = new JSONObject();
                                responseJson.put("response", "STA_CFG");
                                responseJson.put("result", "ok");
                                responseJson.put("error_code", 0);
                                // Handle success case here
                            } else {
                                // Handle error case here
                                Log.e("StationFragment", "Received error: result = " + result + ", error_code = " + errorCode);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("StationFragment", "Error parsing received JSON", e);
                    }
                });

                Log.d("StationFragment", "Complete JSON: " + completeJsonString);
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_station, container, false);
        View root = binding.getRoot();
        Databind();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("stationValue", Context.MODE_PRIVATE);
        int reportPeriod = sharedPreferences.getInt("reportPeriod", 60);
        int batteryInTimeout = sharedPreferences.getInt("batteryInTimeout", 60);
        int batteryOutTimeout = sharedPreferences.getInt("batteryOutTimeout", 60);
        int paymentTimeout = sharedPreferences.getInt("paymentTimeout", 60);
        int paymentType = sharedPreferences.getInt("paymentType", 3);

        reportPeriodEt.setText(String.valueOf(reportPeriod));
        batteryInTimeoutEt.setText(String.valueOf(batteryInTimeout));
        batteryOutTimeoutEt.setText(String.valueOf(batteryOutTimeout));
        paymentTimeoutEt.setText(String.valueOf(paymentTimeout));
        paymentTypeEt.setText(String.valueOf(paymentType));
        stationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Extract values from EditText views
                int reportPeriod = Integer.parseInt(reportPeriodEt.getText().toString());
                int batteryInTimeout = Integer.parseInt(batteryInTimeoutEt.getText().toString());
                int batteryOutTimeout = Integer.parseInt(batteryOutTimeoutEt.getText().toString());
                int paymentTimeout = Integer.parseInt(paymentTimeoutEt.getText().toString());
                int paymentType = Integer.parseInt(paymentTypeEt.getText().toString());

                // Save the values to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("reportPeriod", reportPeriod);
                editor.putInt("batteryInTimeout", batteryInTimeout);
                editor.putInt("batteryOutTimeout", batteryOutTimeout);
                editor.putInt("paymentTimeout", paymentTimeout);
                editor.putInt("paymentType", paymentType);
                editor.apply();

                // Create a JSON object
                JSONObject json = new JSONObject();
                JSONObject dataJson = new JSONObject();
                try {
                    dataJson.put("reportPeriod", reportPeriod);
                    dataJson.put("batInTimeout", batteryInTimeout);
                    dataJson.put("batOutTimeout", batteryOutTimeout);
                    dataJson.put("payTimeout", paymentTimeout);
                    dataJson.put("payType", paymentType);

                    json.put("request", "STA_CFG");
                    json.put("data", dataJson);
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
                    Log.e("StationFragment", "BluetoothConnectionService is not bound");
                }

            }
        });


        return root;
    }

    private void Databind() {
        stationBtn = binding.stationBtn;
        reportPeriodEt = binding.reportPeriodEt;
        batteryInTimeoutEt = binding.batteryInTimeoutEt;
        batteryOutTimeoutEt = binding.batteryOutTimeoutEt;
        paymentTimeoutEt = binding.paymentTimeoutEt;
        paymentTypeEt = binding.paymentTypeEt;
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

