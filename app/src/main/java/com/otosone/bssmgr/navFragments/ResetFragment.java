package com.otosone.bssmgr.navFragments;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.otosone.bssmgr.bluetooth.BluetoothConnectionService;
import com.otosone.bssmgr.R;
import com.otosone.bssmgr.databinding.FragmentResetBinding;

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
    private boolean responseReceived = false;

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


                        if (receivedJson.has("response") && receivedJson.getString("response").equals("CTRL_RESET")) {
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
                        Log.e("ResetFragment", "Error parsing received JSON", e);
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
        builder.setMessage("Would you like to run the command?");
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