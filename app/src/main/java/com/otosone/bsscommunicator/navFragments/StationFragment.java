package com.otosone.bsscommunicator.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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

import com.google.android.gms.common.util.IOUtils;
import com.otosone.bsscommunicator.BluetoothConnectionService;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.databinding.FragmentStationBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StationFragment extends Fragment {

    FragmentStationBinding binding;

    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private StringBuilder jsonStringBuilder = new StringBuilder();

    private EditText reportPeriodEt, batteryInTimeoutEt, batteryOutTimeoutEt, paymentTimeoutEt, paymentTypeEt;
    private Button stationBtn, mainBtn;

    public StationFragment() {
        // Required empty public constructor
    }

    public static StationFragment newInstance(String param1, String param2) {
        StationFragment fragment = new StationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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

                    Toast.makeText(getActivity(), "Received JSON: " + completeJsonString, Toast.LENGTH_LONG).show();
                    Log.d("StationFragment", "Complete JSON: " + completeJsonString);
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
        stationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("222223", "11123");

                // Extract values from EditText views
                int reportPeriod = Integer.parseInt(reportPeriodEt.getText().toString());
                int batteryInTimeout = Integer.parseInt(batteryInTimeoutEt.getText().toString());
                int batteryOutTimeout = Integer.parseInt(batteryOutTimeoutEt.getText().toString());
                int paymentTimeout = Integer.parseInt(paymentTimeoutEt.getText().toString());
                int paymentType = Integer.parseInt(paymentTypeEt.getText().toString());

                // Create a JSON object
                JSONObject json = new JSONObject();
                try {
                    json.put("request", "STA_CFG");
                    json.put("reportPeriod", reportPeriod);
                    json.put("batInTimeout", batteryInTimeout);
                    json.put("batOutTimeout", batteryOutTimeout);
                    json.put("payTimeout", paymentTimeout);
                    json.put("payType", paymentType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String jsonString = json.toString();
                Log.d("ASCII", jsonString);
                // Call the sendAsciiMessage method with the string as an argument
                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendAsciiMessage(jsonString);
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

