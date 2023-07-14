package com.otosone.bssmgr.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.otosone.bssmgr.R;
import com.otosone.bssmgr.bluetooth.BluetoothConnectionService;
import com.otosone.bssmgr.databinding.FragmentHeaterBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class HeaterFragment extends Fragment {

    private FragmentHeaterBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private StringBuilder jsonStringBuilder = new StringBuilder();
    private Button heaterBtn;
    private EditText heaterStartTempEt, heaterStopTempEt;
    private boolean responseReceived = false;

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
                                responseReceived = true; // set the flag
                                Toast.makeText(getActivity(),"success", Toast.LENGTH_SHORT).show();
                            } else {
                                responseReceived = true; // set the flag
                                Toast.makeText(getActivity(),"fail", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("HeaterFragment", "Error parsing received JSON", e);
                    }
                });


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
                Log.e("StatusFragment", "Cannot send message, service is not bound or null");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothConnectionService = null;
            isBound = false;
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

        heaterStartTempEt.setText(String.valueOf(heaterStartTemp) + " ℃");
        heaterStopTempEt.setText(String.valueOf(heaterStopTemp) + " ℃");

        setupTemperatureEditText(heaterStartTempEt);
        setupTemperatureEditText(heaterStopTempEt);

        heaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Extract values from EditText views

                String heaterStartTempStr = heaterStartTempEt.getText().toString().replace("℃", "").trim();
                String heaterStopTempStr = heaterStopTempEt.getText().toString().replace("℃", "").trim();

                int heaterStartTemp = Integer.parseInt(heaterStartTempStr);
                int heaterStopTemp = Integer.parseInt(heaterStopTempStr);

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

                // Call the sendAsciiMessage method with the string as an argument
                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(heaterJsonString);

                    responseReceived = false; // reset the flag

                    // Start a Handler to check for response
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!responseReceived) {
                                Toast.makeText(getActivity(),"fail", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 3000);
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

    private void setupTemperatureEditText(EditText editText) {
        editText.setSelection(editText.getText().length() - 2);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not required
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().endsWith(" ℃")) {
                    editText.setText(s.toString() + " ℃");
                    editText.setSelection(editText.getText().length() - 2);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not required
            }
        });

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editText.setSelection(editText.getText().length() - 2);
                return false;
            }
        });
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