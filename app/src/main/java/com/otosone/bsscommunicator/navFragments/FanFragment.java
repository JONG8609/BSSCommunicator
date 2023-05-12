package com.otosone.bsscommunicator.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.Toast;

import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.databinding.FragmentFanBinding;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FanFragment extends Fragment {

    FragmentFanBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private StringBuilder jsonStringBuilder = new StringBuilder();
    private Button fanBtn;
    private EditText fanStartTempEt, fanStopTempEt;

    public FanFragment() {
        // Required empty public constructor
    }


    public static FanFragment newInstance() {
        FanFragment fragment = new FanFragment();
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
            //sendFanRequest();
            Log.d("FanFragment", "Service connected");

            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {

                getActivity().runOnUiThread(() -> {

                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("request") && receivedJson.getString("request").equals("FAN_CFG")) {
                            JSONObject responseJson = new JSONObject();
                            responseJson.put("response", "FAN_CFG");
                            responseJson.put("result", "ok");
                            responseJson.put("error_code", 0);
                            JSONObject dataObject = new JSONObject();
                            dataObject.put("startTemp", Integer.parseInt(fanStartTempEt.getText().toString()));
                            dataObject.put("stopTemp", Integer.parseInt(fanStopTempEt.getText().toString()));
                            responseJson.put("data", dataObject);

                            // Send the response to the server
                            bluetoothConnectionService.sendMessage(responseJson.toString());
                        }
                        if (receivedJson.has("response") && receivedJson.getString("response").equals("FAN_CFG")) {
                            String result = receivedJson.getString("result");
                            int errorCode = receivedJson.getInt("error_code");

                            if (result.equals("ok") && errorCode == 0) {
                                JSONObject responseJson = new JSONObject();
                                responseJson.put("response", "FAN_CFG");
                                responseJson.put("result", "ok");
                                responseJson.put("error_code", 0);
                                // Handle success case here
                            } else {
                                // Handle error case here
                                Log.e("fanFragment", "Received error: result = " + result + ", error_code = " + errorCode);
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("FanFragment", "Error parsing received JSON", e);
                    }
                });

                Log.d("FanFragment", "Complete JSON: " + completeJsonString);

            });

        }
        private void sendFanRequest() {
            // Create a JSON object
            JSONObject json = new JSONObject();

            try {
                json.put("request", "FAN_CFG");

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
            Log.d("FanFragment", "Service disconnected");
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_fan, container, false);
        View root = binding.getRoot();
        Databind();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("fanValue", Context.MODE_PRIVATE);
        int fanStartTemp = sharedPreferences.getInt("fanStartTemp", 35);
        int fanStopTemp = sharedPreferences.getInt("fanStopTemp", 30);

        fanStartTempEt.setText(String.valueOf(fanStartTemp));
        fanStopTempEt.setText(String.valueOf(fanStopTemp));

        fanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Extract values from EditText views
                int fanStartTemp = Integer.parseInt(fanStartTempEt.getText().toString());
                int fanStopTemp = Integer.parseInt(fanStopTempEt.getText().toString());

                // Save the values to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("fanStartTemp", fanStartTemp);
                editor.putInt("fanStopTemp", fanStopTemp);
                editor.apply();

                // Create a JSON object
                JSONObject fanJson = new JSONObject();
                JSONObject fanDataJson = new JSONObject();
                try {
                    fanDataJson.put("startTemp", fanStartTemp);
                    fanDataJson.put("stopTemp", fanStopTemp);

                    fanJson.put("request", "FAN_CFG");
                    fanJson.put("data", fanDataJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String fanJsonString = fanJson.toString();
                Log.d("UTF-8", fanJsonString);

                // Call the sendAsciiMessage method with the string as an argument
                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(fanJsonString);
                    Log.d("json11", fanJsonString);
                } else {
                    Log.e("FanFragment", "BluetoothConnectionService is not bound");
                }

            }
        });



        return root;
    }

    private void Databind() {
        fanBtn = binding.fanBtn;
        fanStartTempEt = binding.fanStartTempEt;
        fanStopTempEt = binding.fanStopTempEt;
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