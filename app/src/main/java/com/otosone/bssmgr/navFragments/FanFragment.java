package com.otosone.bssmgr.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.otosone.bssmgr.bluetooth.BluetoothConnectionService;
import com.otosone.bssmgr.R;
import com.otosone.bssmgr.databinding.FragmentFanBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class FanFragment extends Fragment {

    FragmentFanBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private StringBuilder jsonStringBuilder = new StringBuilder();
    private Button fanBtn;
    private EditText fanStartTempEt, fanStopTempEt;
    private boolean responseReceived = false;

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
                                responseReceived = true; // set the flag
                                Toast.makeText(getActivity(),"success", Toast.LENGTH_SHORT).show();
                            } else {
                                responseReceived = true; // set the flag
                                Toast.makeText(getActivity(),"fail", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("FanFragment", "Error parsing received JSON", e);
                    }
                });


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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_fan, container, false);
        View root = binding.getRoot();
        Databind();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("fanValue", Context.MODE_PRIVATE);
        int fanStartTemp = sharedPreferences.getInt("fanStartTemp", 35);
        int fanStopTemp = sharedPreferences.getInt("fanStopTemp", 30);

        fanStartTempEt.setText(String.valueOf(fanStartTemp) + " ℃");
        fanStopTempEt.setText(String.valueOf(fanStopTemp) + " ℃");

        // Adding TextWatchers and OnTouchListeners
        setupTemperatureEditText(fanStartTempEt);
        setupTemperatureEditText(fanStopTempEt);

        fanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Extract values from EditText views and remove any spaces
                String fanStartTempStr = fanStartTempEt.getText().toString().replace("℃", "").trim();
                String fanStopTempStr = fanStopTempEt.getText().toString().replace("℃", "").trim();

                // Now convert them to integers. This should no longer throw a NumberFormatException
                int fanStartTemp = Integer.parseInt(fanStartTempStr);
                int fanStopTemp = Integer.parseInt(fanStopTempStr);

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


                // Call the sendAsciiMessage method with the string as an argument
                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(fanJsonString);

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

    private void setupTemperatureEditText(EditText editText) {
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no operation
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // no operation
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().endsWith(" ℃")) {
                    editText.removeTextChangedListener(this); // remove to prevent stackOverflow
                    String updatedString = s.toString() + " ℃";
                    editText.setText(updatedString);
                    editText.setSelection(updatedString.length() - 2);  // set cursor position before "℃"
                    editText.addTextChangedListener(this); // add it back
                }
            }
        };

        editText.addTextChangedListener(textWatcher);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && editText.getText().toString().contains(" ℃")) {
                    editText.setSelection(editText.getText().toString().indexOf(" ℃")); // place the cursor right before " ℃"
                }
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