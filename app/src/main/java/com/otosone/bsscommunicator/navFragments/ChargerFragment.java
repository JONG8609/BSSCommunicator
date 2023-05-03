package com.otosone.bsscommunicator.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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

            Log.d("ChargeFragment", "Service connected");

            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {
                Log.d("ChargeFragment", "MessageReceivedListener called");
                getActivity().runOnUiThread(() -> {

                    Toast.makeText(getActivity(), "Received JSON: " + completeJsonString, Toast.LENGTH_LONG).show();
                    Log.d("ChargeFragment", "Complete JSON: " + completeJsonString);

                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("type") && receivedJson.getString("type").equals("CHG_CFG")) {
                            if (receivedJson.has("preChgRefVol")) {
                                preChargerRefVoltageEt.setText(String.valueOf(receivedJson.getInt("reportPeriod")));
                            }
                            if (receivedJson.has("preChgCur")) {
                                preChargerCurrentEt.setText(String.valueOf(receivedJson.getInt("batInTimeout")));
                            }
                            if (receivedJson.has("preChgTemp")) {
                                preChargerTempEt.setText(String.valueOf(receivedJson.getInt("batOutTimeout")));
                            }
                            if (receivedJson.has("preChgTimeout")) {
                                preChargerTimeoutEt.setText(String.valueOf(receivedJson.getInt("payTimeout")));
                            }
                            if (receivedJson.has("chgVol")) {
                                chargerVoltageEt.setText(String.valueOf(receivedJson.getInt("payType")));
                            }
                            if (receivedJson.has("chgCur")) {
                                chargerCurrentEt.setText(String.valueOf(receivedJson.getInt("payTimeout")));
                            }
                            if (receivedJson.has("chgLimitTemp")) {
                                chargerLimitTempEt.setText(String.valueOf(receivedJson.getInt("payType")));
                            }
                            if (receivedJson.has("chgCutoffVol")) {
                                ChargerCutoffVoltageEt.setText(String.valueOf(receivedJson.getInt("payTimeout")));
                            }
                            if (receivedJson.has("chgCutoffCur")) {
                                ChargerCutoffCurrentEt.setText(String.valueOf(receivedJson.getInt("payType")));
                            }
                            if (receivedJson.has("chgTimeout")) {
                                chargingTimeoutEt.setText(String.valueOf(receivedJson.getInt("payTimeout")));
                            }
                            if (receivedJson.has("cellDeltaV")) {
                                cellDeltaVEt.setText(String.valueOf(receivedJson.getInt("payType")));
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

        chargerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Extract values from EditText views
                int preChgRefVol = Integer.parseInt(preChargerRefVoltageEt.getText().toString());
                int preChgCur = Integer.parseInt(preChargerCurrentEt.getText().toString());
                int preChgTemp = Integer.parseInt(preChargerTempEt.getText().toString());
                int preChgTimeout = Integer.parseInt(preChargerTimeoutEt.getText().toString());
                int chgVol = Integer.parseInt(chargerVoltageEt.getText().toString());
                int chgCur = Integer.parseInt(chargerCurrentEt.getText().toString());
                int chgLimitTemp = Integer.parseInt(chargerLimitTempEt.getText().toString());
                int chgCutoffVol = Integer.parseInt(ChargerCutoffVoltageEt.getText().toString());
                int chgCutoffCur = Integer.parseInt(ChargerCutoffCurrentEt.getText().toString());
                int chgTimeout = Integer.parseInt(chargingTimeoutEt.getText().toString());
                int cellDeltaV = Integer.parseInt(cellDeltaVEt.getText().toString());


                // Create a JSON object
                JSONObject json = new JSONObject();
                try {
                    json.put("result", "CHG_CFG");
                    json.put("preChgRefVol", preChgRefVol);
                    json.put("preChgCur", preChgCur);
                    json.put("preChgTemp", preChgTemp);
                    json.put("preChgTimeout", preChgTimeout);
                    json.put("chgVol", chgVol);
                    json.put("chgCur", chgCur);
                    json.put("chgLimitTemp", chgLimitTemp);
                    json.put("chgCutoffVol", chgCutoffVol);
                    json.put("chgCutoffCur", chgCutoffCur);
                    json.put("chgTimeout", chgTimeout);
                    json.put("cellDeltaV", cellDeltaV);
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