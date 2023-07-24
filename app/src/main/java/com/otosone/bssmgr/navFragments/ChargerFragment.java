package com.otosone.bssmgr.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import com.otosone.bssmgr.databinding.FragmentChargerBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChargerFragment extends Fragment {
    private BluetoothConnectionService bluetoothConnectionService;
    FragmentChargerBinding binding;
    private boolean isBound = false;

    private StringBuilder jsonStringBuilder = new StringBuilder();
    private Button chargerBtn;
    private EditText preChargerRefVoltageEt, preChargerCurrentEt, preChargerTempEt, preChargerTimeoutEt, chargerVoltageEt, chargerCurrentEt, chargerLimitTempEt, ChargerCutoffVoltageEt, ChargerCutoffCurrentEt, chargingTimeoutEt, cellDeltaVEt;
    private boolean responseReceived = false;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) iBinder;
            bluetoothConnectionService = binder.getService();
            isBound = true;
            //sendChargerRequest();


            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {

                getActivity().runOnUiThread(() -> {


                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("request") && receivedJson.getString("request").equals("CHG_CFG")) {

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
                                responseReceived = true;
                                Toast.makeText(getActivity(),"success", Toast.LENGTH_SHORT).show();
                            } else {
                                responseReceived = true; // set the flag
                                Toast.makeText(getActivity(),"fail", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("StationFragment", "Error parsing received JSON", e);
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_charger, container, false);
        View root = binding.getRoot();
        Databind();

        Map<EditText, String> editTextUnitMap = new HashMap<EditText, String>() {{
            put(preChargerRefVoltageEt, " V");
            put(preChargerCurrentEt, " A");
            put(preChargerTempEt, " ℃");
            put(preChargerTimeoutEt, " Min");
            put(chargerVoltageEt, " V");
            put(chargerCurrentEt, " A");
            put(chargerLimitTempEt, " ℃");
            put(ChargerCutoffVoltageEt, " V");
            put(ChargerCutoffCurrentEt, " A");
            put(chargingTimeoutEt, " Min");
            put(cellDeltaVEt, " mV");
        }};

        for (Map.Entry<EditText, String> entry : editTextUnitMap.entrySet()) {
            EditText editText = entry.getKey();
            String unit = entry.getValue();

            editText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    String text = editText.getText().toString();
                    if (!text.endsWith(unit)) {
                        editText.setText(text + unit);
                        editText.setSelection(text.length());
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String str = s.toString();
                    if (!str.endsWith(unit)) {
                        editText.setText(str + unit);
                        editText.setSelection(str.length());
                    }
                }
            });
        }

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


        preChargerRefVoltageEt.setText(String.valueOf(preChgRefVol) + " V");
        preChargerCurrentEt.setText(String.valueOf(preChgCur) + " A");
        preChargerTempEt.setText(String.valueOf(preChgTemp) + " ℃");
        preChargerTimeoutEt.setText(String.valueOf(preChgTimeout) + " Min");
        chargerVoltageEt.setText(String.valueOf(chgVol) + " V");
        chargerCurrentEt.setText(String.valueOf(chgCur) + " A");
        chargerLimitTempEt.setText(String.valueOf(chgLimitTemp) + " ℃");
        ChargerCutoffVoltageEt.setText(String.valueOf(chgCutoffVol) + " V");
        ChargerCutoffCurrentEt.setText(String.valueOf(chgCutoffCur) + " A");
        chargingTimeoutEt.setText(String.valueOf(chgTimeout) + " Min");
        cellDeltaVEt.setText(String.valueOf(cellDeltaV) + " mV");

        chargerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Extract values from EditText views
                String[] preChgRefVolParts = preChargerRefVoltageEt.getText().toString().split(" ");
                float preChgRefVol = Float.parseFloat(preChgRefVolParts[0]);

                String[] preChgCurParts = preChargerCurrentEt.getText().toString().split(" ");
                float preChgCur = Float.parseFloat(preChgCurParts[0]);

                String[] preChgTempParts = preChargerTempEt.getText().toString().split(" ");
                int preChgTemp = Integer.parseInt(preChgTempParts[0]);

                String[] preChgTimeoutParts = preChargerTimeoutEt.getText().toString().split(" ");
                int preChgTimeout = Integer.parseInt(preChgTimeoutParts[0]);

                String[] chgVolParts = chargerVoltageEt.getText().toString().split(" ");
                float chgVol = Float.parseFloat(chgVolParts[0]);

                String[] chgCurParts = chargerCurrentEt.getText().toString().split(" ");
                float chgCur = Float.parseFloat(chgCurParts[0]);

                String[] chgLimitTempParts = chargerLimitTempEt.getText().toString().split(" ");
                int chgLimitTemp = Integer.parseInt(chgLimitTempParts[0]);

                String[] chgCutoffVolParts = ChargerCutoffVoltageEt.getText().toString().split(" ");
                float chgCutoffVol = Float.parseFloat(chgCutoffVolParts[0]);

                String[] chgCutoffCurParts = ChargerCutoffCurrentEt.getText().toString().split(" ");
                float chgCutoffCur = Float.parseFloat(chgCutoffCurParts[0]);

                String[] chgTimeoutParts = chargingTimeoutEt.getText().toString().split(" ");
                int chgTimeout = Integer.parseInt(chgTimeoutParts[0]);

                String[] cellDeltaVParts = cellDeltaVEt.getText().toString().split(" ");
                int cellDeltaV = Integer.parseInt(cellDeltaVParts[0]);

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

                // Call the sendAsciiMessage method with the string as an argument
                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(jsonString);

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