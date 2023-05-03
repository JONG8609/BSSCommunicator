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
import com.otosone.bsscommunicator.databinding.FragmentFanAndHeaterBinding;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FanAndHeaterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FanAndHeaterFragment extends Fragment {

    FragmentFanAndHeaterBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private StringBuilder jsonStringBuilder = new StringBuilder();
    private Button fanAndHeatBtn;
    private EditText fanStartTempEt, fanStopTempEt, heaterStartTempEt, heaterStopTempEt;

    public FanAndHeaterFragment() {
        // Required empty public constructor
    }


    public static FanAndHeaterFragment newInstance(String param1, String param2) {
        FanAndHeaterFragment fragment = new FanAndHeaterFragment();
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

            Log.d("FanAndHeaterFragment", "Service connected");

            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {
                Log.d("FanAndHeaterFragment", "MessageReceivedListener called");
                getActivity().runOnUiThread(() -> {

                    Toast.makeText(getActivity(), "Received JSON: " + completeJsonString, Toast.LENGTH_LONG).show();
                    Log.d("FanAndHeaterFragment", "Complete JSON: " + completeJsonString);

                    // Parse the received JSON string
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("result") && receivedJson.getString("result").equals("FAN_CFG")) {
                            if (receivedJson.has("startTemp")) {
                                fanStartTempEt.setText(String.valueOf(receivedJson.getInt("startTemp")));
                            }
                            if (receivedJson.has("stopTemp")) {
                                fanStopTempEt.setText(String.valueOf(receivedJson.getInt("stopTemp")));
                            }
                        }
                        if (receivedJson.has("result") && receivedJson.getString("result").equals("HEATER_CFG")) {
                            if (receivedJson.has("startTemp")) {
                                heaterStartTempEt.setText(String.valueOf(receivedJson.getInt("startTemp")));
                            }
                            if (receivedJson.has("stopTemp")) {
                                heaterStopTempEt.setText(String.valueOf(receivedJson.getInt("stopTemp")));
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
            Log.d("FanAndHeaterFragment", "Service disconnected");
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_fan_and_heater, container, false);
        View root = binding.getRoot();
        Databind();

        fanAndHeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Extract values from EditText views
                int fanStartTemp = Integer.parseInt(fanStartTempEt.getText().toString());
                int fanStopTemp = Integer.parseInt(fanStopTempEt.getText().toString());
                int heaterStartTemp = Integer.parseInt(heaterStartTempEt.getText().toString());
                int heaterStopTemp = Integer.parseInt(heaterStopTempEt.getText().toString());

                // Create a JSON object
                JSONObject fanJson = new JSONObject();
                try {
                    fanJson.put("result", "FAN_CFG");
                    fanJson.put("startTemp", fanStartTemp);
                    fanJson.put("stopTemp", fanStopTemp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject heaterJson = new JSONObject();
                try {
                    heaterJson.put("result", "HEATER_CFG");
                    heaterJson.put("startTemp", heaterStartTemp);
                    heaterJson.put("stopTemp", heaterStopTemp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String fanJsonString = fanJson.toString();
                String heaterJsonString = heaterJson.toString();
                String fanAndHeaterJsonString = fanJsonString + "\r\n" + heaterJsonString;
                Log.d("UTF-8", fanJsonString);
                Log.d("UTF-8", heaterJsonString);
                // Call the sendAsciiMessage method with the string as an argument
                if (isBound && bluetoothConnectionService != null) {
                    bluetoothConnectionService.sendMessage(fanAndHeaterJsonString);
                    Log.d("json11", fanAndHeaterJsonString);
                } else {
                    Log.e("StationFragment", "BluetoothConnectionService is not bound");
                }

            }
        });


        return root;
    }

    private void Databind() {
        fanAndHeatBtn = binding.fanAndHeatBtn;
        fanStartTempEt = binding.fanStartTempEt;
        fanStopTempEt = binding.fanStopTempEt;
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