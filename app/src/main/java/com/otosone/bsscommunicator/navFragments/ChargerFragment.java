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
import com.otosone.bsscommunicator.databinding.FragmentChargerBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class ChargerFragment extends Fragment {
    private BluetoothConnectionService bluetoothConnectionService;
    FragmentChargerBinding binding;
    private boolean isBound = false;

    private StringBuilder jsonStringBuilder = new StringBuilder();
    private Button chargerBtn;
    private EditText preChargerRefVoltageEt, preChargerCurrentEt, preChargerTempEt, preChargerTimeoutEt, chargerVoltageEt, chargerCurrentEt, chargerLimitTempEt, ChargerCutoffVoltageEt, ChargerCutoffCurrentEt, chargingTimeoutEt, cellDeltaVEt;


    public ChargerFragment() {
        // Required empty public constructor
    }

    public static ChargerFragment newInstance(String param1, String param2) {
        ChargerFragment fragment = new ChargerFragment();
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
            //sendChargerRequest();
            Log.d("ChargeFragment", "Service connected");

            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {

                getActivity().runOnUiThread(() -> {

                    double preChgRefVol = Double.parseDouble(preChargerRefVoltageEt.getText().toString());
                    double preChgCur = Double.parseDouble(preChargerCurrentEt.getText().toString());
                    int preChgTemp = Integer.parseInt(preChargerTempEt.getText().toString());
                    int preChgTimeout = Integer.parseInt(preChargerTimeoutEt.getText().toString());
                    double chgVol = Double.parseDouble(chargerVoltageEt.getText().toString());
                    double chgCur = Double.parseDouble(chargerCurrentEt.getText().toString());
                    int chgLimitTemp = Integer.parseInt(chargerLimitTempEt.getText().toString());
                    double chgCutoffVol = Double.parseDouble(ChargerCutoffVoltageEt.getText().toString());
                    double chgCutoffCur = Double.parseDouble(ChargerCutoffCurrentEt.getText().toString());
                    int chgTimeout = Integer.parseInt(chargingTimeoutEt.getText().toString());
                    int cellDeltaV = Integer.parseInt(cellDeltaVEt.getText().toString());
                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("request") && receivedJson.getString("request").equals("CHG_CFG")) {

                            JSONObject responseJson = new JSONObject();
                            responseJson.put("response", "CHG_CFG");
                            responseJson.put("result", "ok");
                            responseJson.put("error_code", 0);
                            JSONObject dataObject = new JSONObject();
                            dataObject.put("preChgRefVol", preChgRefVol * 10);
                            dataObject.put("preChgCur", preChgCur * 10);
                            dataObject.put("preChgTemp", preChgTemp);
                            dataObject.put("preChgTimeout", preChgTimeout);
                            dataObject.put("chgVol", chgVol * 10);
                            dataObject.put("chgCur", chgCur * 10);
                            dataObject.put("chgLimitTemp", chgLimitTemp);
                            dataObject.put("chgCutoffVol", chgCutoffVol * 10);
                            dataObject.put("chgCutoffCur", chgCutoffCur * 10);
                            dataObject.put("chgTimeout", chgTimeout);
                            dataObject.put("cellDeltaV", cellDeltaV);
                            responseJson.put("data", dataObject);

                            // Send the response to the server
                            bluetoothConnectionService.sendMessage(responseJson.toString());

                        }

                        if (receivedJson.has("response") && receivedJson.getString("response").equals("CHG_CFG")) {
                            String result = receivedJson.getString("result");
                            int errorCode = receivedJson.getInt("error_code");

                            if (result.equals("ok") && errorCode == 0) {
                                JSONObject responseJson = new JSONObject();
                                responseJson.put("response", "CHG_CFG");
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

        private void sendChargerRequest() {
            // Create a JSON object
            JSONObject json = new JSONObject();

            try {
                json.put("request", "CHG_CFG");

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
            Log.d("ChargeFragment", "Service disconnected");
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_charger, container, false);
        View root = binding.getRoot();
        Databind();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("stationValue", Context.MODE_PRIVATE);
        float preChgRefVol = sharedPreferences.getFloat("preChgRefVol", 31.0F);
        float preChgCur = sharedPreferences.getFloat("preChgCur", 5.0F);
        int preChgTemp = sharedPreferences.getInt("preChgTemp", 5);
        int preChgTimeout = sharedPreferences.getInt("preChgTimeout", 15);
        float chgVol = sharedPreferences.getFloat("chgVol", 41.2F);
        float chgCur = sharedPreferences.getFloat("chgCur", 12.0F);
        int chgLimitTemp = sharedPreferences.getInt("chgLimitTemp", 45);
        float chgCutoffVol = sharedPreferences.getFloat("chgCutoffVol", 41.0F);
        float chgCutoffCur = sharedPreferences.getFloat("chgCutoffCur", 4.0F);
        int chgTimeout = sharedPreferences.getInt("chgTimeout", 240);
        int cellDeltaV = sharedPreferences.getInt("cellDeltaV", 150);


        preChargerRefVoltageEt.setText(String.valueOf(preChgRefVol));
        preChargerCurrentEt.setText(String.valueOf(preChgCur));
        preChargerTempEt.setText(String.valueOf(preChgTemp));
        preChargerTimeoutEt.setText(String.valueOf(preChgTimeout));
        chargerVoltageEt.setText(String.valueOf(chgVol));
        chargerCurrentEt.setText(String.valueOf(chgCur));
        chargerLimitTempEt.setText(String.valueOf(chgLimitTemp));
        ChargerCutoffVoltageEt.setText(String.valueOf(chgCutoffVol));
        ChargerCutoffCurrentEt.setText(String.valueOf(chgCutoffCur));
        chargingTimeoutEt.setText(String.valueOf(chgTimeout));
        cellDeltaVEt.setText(String.valueOf(cellDeltaV));

        chargerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Extract values from EditText views
                float preChgRefVol = Float.parseFloat(preChargerRefVoltageEt.getText().toString());
                float preChgCur = Float.parseFloat(preChargerCurrentEt.getText().toString());
                int preChgTemp = Integer.parseInt(preChargerTempEt.getText().toString());
                int preChgTimeout = Integer.parseInt(preChargerTimeoutEt.getText().toString());
                float chgVol = Float.parseFloat(chargerVoltageEt.getText().toString());
                float chgCur = Float.parseFloat(chargerCurrentEt.getText().toString());
                int chgLimitTemp = Integer.parseInt(chargerLimitTempEt.getText().toString());
                float chgCutoffVol = Float.parseFloat(ChargerCutoffVoltageEt.getText().toString());
                float chgCutoffCur = Float.parseFloat(ChargerCutoffCurrentEt.getText().toString());
                int chgTimeout = Integer.parseInt(chargingTimeoutEt.getText().toString());
                int cellDeltaV = Integer.parseInt(cellDeltaVEt.getText().toString());

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("preChgRefVol", preChgRefVol);
                editor.putFloat("preChgCur", preChgCur);
                editor.putInt("preChgTemp", preChgTemp);
                editor.putInt("preChgTimeout", preChgTimeout);
                editor.putFloat("chgVol", chgVol);
                editor.putFloat("chgCur", chgCur);
                editor.putInt("chgLimitTemp", chgLimitTemp);
                editor.putFloat("chgCutoffVol", chgCutoffVol);
                editor.putFloat("chgCutoffCur", chgCutoffCur);
                editor.putInt("chgTimeout", chgTimeout);
                editor.putInt("cellDeltaV", cellDeltaV);
                editor.apply();

                // Create a JSON object
                JSONObject json = new JSONObject();
                JSONObject chargerDataJson = new JSONObject();
                try {
                    chargerDataJson.put("preChgRefVol", preChgRefVol * 10);
                    chargerDataJson.put("preChgCur", preChgCur * 10);
                    chargerDataJson.put("preChgTemp", preChgTemp);
                    chargerDataJson.put("preChgTimeout", preChgTimeout);
                    chargerDataJson.put("chgVol", chgVol * 10);
                    chargerDataJson.put("chgCur", chgCur * 10);
                    chargerDataJson.put("chgLimitTemp", chgLimitTemp);
                    chargerDataJson.put("chgCutoffVol", chgCutoffVol * 10);
                    chargerDataJson.put("chgCutoffCur", chgCutoffCur * 10);
                    chargerDataJson.put("chgTimeout", chgTimeout);
                    chargerDataJson.put("cellDeltaV", cellDeltaV);

                    json.put("request", "CHG_CFG");
                    json.put("data", chargerDataJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String jsonString = json.toString();
                Log.d("UTF-8", jsonString);
                // Call the sendAsciiMessage method with the string as an argument
                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(jsonString);
                    Log.d("json11", jsonString);
                } else {
                    Log.e("ChargerFragment", "BluetoothConnectionService is not bound");
                }

            }
        });

        return root;
    }

    private void Databind() {
        chargerBtn = binding.chargerBtn;
        preChargerRefVoltageEt = binding.preChargerRefVoltageEt;
        preChargerCurrentEt = binding.preChargerCurrentEt;
        preChargerTempEt = binding.preChargerTempEt;
        preChargerTimeoutEt = binding.preChargerTimeoutEt;
        chargerVoltageEt = binding.chargerVoltageEt;
        chargerCurrentEt = binding.chargerCurrentEt;
        chargerLimitTempEt = binding.chargerLimitTempEt;
        ChargerCutoffVoltageEt = binding.ChargerCutoffVoltageEt;
        ChargerCutoffCurrentEt = binding.ChargerCutoffCurrentEt;
        chargingTimeoutEt = binding.chargingTimeoutEt;
        cellDeltaVEt = binding.cellDeltaVEt;

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