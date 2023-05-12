package com.otosone.bsscommunicator.navFragments;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
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

import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.databinding.FragmentResetBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResetFragment extends Fragment {

    FragmentResetBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private Button resetBtn;

    public static ResetFragment newInstance(String param1, String param2) {
        ResetFragment fragment = new ResetFragment();
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


            // Set the MessageReceivedListener
            bluetoothConnectionService.setMessageReceivedListener(completeJsonString -> {
                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        // Check if it's a request
                        if (receivedJson.has("response")) {
                            resetResponse(completeJsonString);
                        }

                    } catch (JSONException e) {
                        Log.e("ResetFragment", "Error parsing received JSON", e);
                    }
                });

            });


        }

        private void resetResponse(String jsonData) {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);

                String responseType = jsonObject.getString("response");
                String result = jsonObject.getString("result");
                int errorCode = jsonObject.getInt("error_code");

            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reset, container, false);
        resetBtn = binding.resetBtn;

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResetConfirmationDialog();
            }
        });
        return binding.getRoot();
    }

    private void showResetConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Reset Confirmation");
        builder.setMessage("Would you lkie to run the command?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResetRequest();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void sendResetRequest() {
        JSONObject resetRequest = new JSONObject();
        try {
            resetRequest.put("request", "CTRL_RESET");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (bluetoothConnectionService != null) {
            bluetoothConnectionService.sendMessage(resetRequest.toString());
        }
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