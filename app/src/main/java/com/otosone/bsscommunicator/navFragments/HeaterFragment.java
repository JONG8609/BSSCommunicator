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

import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.databinding.FragmentHeaterBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class HeaterFragment extends Fragment {

    private FragmentHeaterBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private StringBuilder jsonStringBuilder = new StringBuilder();
    private Button heaterBtn;
    private EditText heaterStartTempEt, heaterStopTempEt;

    public HeaterFragment() {
        // Required empty public constructor
    }

    public static HeaterFragment newInstance() {
        HeaterFragment fragment = new HeaterFragment();
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
            //sendHeaterRequest();
            Log.d("HeaterFragment", "Service connected");

            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {
                getActivity().runOnUiThread(() -> {

                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("request") && receivedJson.getString("request").equals("HEATER_CFG")) {
                            JSONObject responseJson = new JSONObject();
                            responseJson.put("response", "HEATER_CFG");
                            responseJson.put("result", "ok");
                            responseJson.put("error_code", 0);
                            JSONObject dataObject = new JSONObject();
                            dataObject.put("startTemp", Integer.parseInt(heaterStartTempEt.getText().toString()));
                            dataObject.put("stopTemp", Integer.parseInt(heaterStopTempEt.getText().toString()));
                            responseJson.put("data", dataObject);

                            // Send the response to the server
                            bluetoothConnectionService.sendMessage(responseJson.toString());
                        }

                        if (receivedJson.has("response") && receivedJson.getString("response").equals("HEATER_CFG")) {
                            String result = receivedJson.getString("result");
                            int errorCode = receivedJson.getInt("error_code");

                            if (result.equals("ok") && errorCode == 0) {
                                JSONObject responseJson = new JSONObject();
                                responseJson.put("response", "HEATER_CFG");
                                responseJson.put("result", "ok");
                                responseJson.put("error_code", 0);
                                // Handle success case here
                            } else {
                                // Handle error case here
                                Log.e("HeaterFragment", "Received error: result = " + result + ", error_code = " + errorCode);
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("HeaterFragment", "Error parsing received JSON", e);
                    }
                });

                Log.d("HeaterFragment", "Complete JSON: " + completeJsonString);

            });

        }

        private void sendHeaterRequest() {
            // Create a JSON object
            JSONObject json = new JSONObject();

            try {
                json.put("request", "HEATER_CFG");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            String jsonString = json.toString();

            // Call the sendAsciiMessage method with the string as an argument
            if (isBound && bluetoothConnectionService != null) {
                bluetoothConnectionService.sendMessage(jsonString);

            } else {
                Log.e("StatusFragment","Cannot send message, service is not bound or null");
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothConnectionService = null;
            isBound = false;
            Log.d("HeaterFragment", "Service disconnected");
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_heater, container, false);
        View root = binding.getRoot();
        Databind();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("heaterValue", Context.MODE_PRIVATE);
        int heaterStartTemp = sharedPreferences.getInt("heaterStartTemp", 15);
        int heaterStopTemp = sharedPreferences.getInt("heaterStopTemp", 25);

        heaterStartTempEt.setText(String.valueOf(heaterStartTemp));
        heaterStopTempEt.setText(String.valueOf(heaterStopTemp));

        heaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Extract values from EditText views
                int heaterStartTemp = Integer.parseInt(heaterStartTempEt.getText().toString());
                int heaterStopTemp = Integer.parseInt(heaterStopTempEt.getText().toString());

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("heaterStartTemp", heaterStartTemp);
                editor.putInt("heaterStopTemp", heaterStopTemp);
                editor.apply();

                // Create a JSON object
                JSONObject heaterJson = new JSONObject();
                JSONObject heaterDataJson = new JSONObject();
                try {
                    heaterDataJson.put("startTemp", heaterStartTemp);
                    heaterDataJson.put("stopTemp", heaterStopTemp);

                    heaterJson.put("request", "HEATER_CFG");
                    heaterJson.put("data", heaterDataJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String heaterJsonString = heaterJson.toString();
                Log.d("UTF-8", heaterJsonString);

                // Call the sendAsciiMessage method with the string as an argument
                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(heaterJsonString);
                    Log.d("json11", heaterJsonString);
                } else {
                    Log.e("HeaterFragment", "BluetoothConnectionService is not bound");
                }

            }
        });


        return root;
    }

    private void Databind() {
        heaterBtn = binding.heatBtn;
        heaterStartTempEt = binding.heaterStartTempEt;
        heaterStopTempEt = binding.heaterStopTempEt;
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