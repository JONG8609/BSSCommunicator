package com.otosone.bssmgr.navFragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.otosone.bssmgr.R;
import com.otosone.bssmgr.bluetooth.BluetoothConnectionService;
import com.otosone.bssmgr.databinding.FragmentSocketStatusListBinding;
import com.otosone.bssmgr.listAdapters.BasicAdapter;
import com.otosone.bssmgr.listAdapters.Bms1Adapter;
import com.otosone.bssmgr.listAdapters.Bms2Adapter;
import com.otosone.bssmgr.listAdapters.Bms3Adapter;
import com.otosone.bssmgr.listAdapters.ChargerAdapter;
import com.otosone.bssmgr.listAdapters.StatusAdapter;
import com.otosone.bssmgr.utils.DataHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SocketStatusListFragment extends Fragment {

    private FragmentSocketStatusListBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private Button statusRefreshBtn;
    private TextView basicTv, statusTv, chargerTv, bms1Tv, bms2Tv, bms3Tv;
    private ListView statusListListview;
    private View currentView;

    public SocketStatusListFragment() {
        // Required empty public constructor
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_socket_status_list, container, false);
        View root = binding.getRoot();
        Databind();


        statusRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(currentView != null) {
                    boolean success = currentView.callOnClick();
                    if(success) {
                        Toast.makeText(getContext(), "success", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "fail", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        basicTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(statusTv);
                removeUnderline(chargerTv);
                removeUnderline(bms1Tv);
                removeUnderline(bms2Tv);
                removeUnderline(bms3Tv);

                underlineTextView(basicTv);

                Map<String, JSONObject> map = DataHolder.getInstance().getSocketStatusMapDirectly();
                if (map != null) {
                    List<JSONObject> basicData = new ArrayList<>();

                    for (JSONObject obj : map.values()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();
                            simplifiedObj.put("index", obj.getInt("index"));
                            simplifiedObj.put("serial", obj.getJSONObject("bms").getString("serial"));
                            simplifiedObj.put("fwVer", obj.getJSONObject("cboard").getString("fwVer"));
                            basicData.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    BasicAdapter basicAdapter = new BasicAdapter(getContext(), basicData);
                    statusListListview.setAdapter(basicAdapter);
                }
            }
        });



        statusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(basicTv);
                removeUnderline(chargerTv);
                removeUnderline(bms1Tv);
                removeUnderline(bms2Tv);
                removeUnderline(bms3Tv);

                underlineTextView(statusTv);

                Map<String, String> map = DataHolder.getInstance().getBinaryStatusMapDirectly();

                if (map != null) {
                    List<JSONObject> statusData = new ArrayList<>();

                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();

                            // Get binary status string directly
                            String binaryStatus = entry.getValue();

                            // Extract necessary bits
                            int CAN = binaryStatus.charAt(31) - '0';
                            int CHG = binaryStatus.charAt(30) - '0';
                            int BMS = binaryStatus.charAt(29) - '0';
                            int BAT = binaryStatus.charAt(28) - '0';
                            int LOCK = binaryStatus.charAt(27) - '0';
                            int DOOR = binaryStatus.charAt(26) - '0';
                            int Status_CHG = binaryStatus.charAt(25) - '0';

                            simplifiedObj.put("index", entry.getKey());
                            simplifiedObj.put("CAN", CAN);
                            simplifiedObj.put("CHG", CHG);
                            simplifiedObj.put("BMS", BMS);
                            simplifiedObj.put("BAT", BAT);
                            simplifiedObj.put("LOCK", LOCK);
                            simplifiedObj.put("DOOR", DOOR);
                            simplifiedObj.put("Status_CHG", Status_CHG);

                            statusData.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    StatusAdapter statusAdapter = new StatusAdapter(getContext(), statusData);
                    statusListListview.setAdapter(statusAdapter);
                }
            }
        });


        chargerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(basicTv);
                removeUnderline(statusTv);
                removeUnderline(bms1Tv);
                removeUnderline(bms2Tv);
                removeUnderline(bms3Tv);

                underlineTextView(chargerTv);

                Map<String, JSONObject> map = DataHolder.getInstance().getSocketStatusMapDirectly();

                if (map != null) {
                    // Convert the values of the map into a list
                    List<JSONObject> chargerData = new ArrayList<>();

                    for (JSONObject obj : map.values()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();
                            simplifiedObj.put("index", obj.getInt("index"));
                            simplifiedObj.put("temp", obj.getJSONObject("charger").getInt("temp"));
                            simplifiedObj.put("volt", obj.getJSONObject("charger").getInt("voltage"));
                            simplifiedObj.put("amper", obj.getJSONObject("charger").getInt("current"));
                            simplifiedObj.put("charging", obj.getJSONObject("charger").getInt("charging"));

                            chargerData.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    ChargerAdapter chargerAdapter = new ChargerAdapter(getContext(), chargerData);
                    statusListListview.setAdapter(chargerAdapter);
                }
            }
        });

        bms1Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(basicTv);
                removeUnderline(statusTv);
                removeUnderline(chargerTv);
                removeUnderline(bms2Tv);
                removeUnderline(bms3Tv);

                underlineTextView(bms1Tv);

                Map<String, JSONObject> map = DataHolder.getInstance().getSocketStatusMapDirectly();

                if (map != null) {
                    // Convert the values of the map into a list
                    List<JSONObject> bms1Data = new ArrayList<>();

                    for (JSONObject obj : map.values()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();
                            simplifiedObj.put("index", obj.getInt("index"));
                            simplifiedObj.put("soc", obj.getJSONObject("bms").getInt("soc"));
                            simplifiedObj.put("packv", obj.getJSONObject("bms").getInt("pack_v"));
                            simplifiedObj.put("packa", obj.getJSONObject("bms").getInt("pack_a"));
                            simplifiedObj.put("cycle", obj.getJSONObject("bms").getJSONArray("cycle")); // Fetching cycle as an array
                            simplifiedObj.put("alarm", obj.getJSONObject("bms").getString("alarm")); // Fetching alarm as a string

                            bms1Data.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Bms1Adapter bms1Adapter = new Bms1Adapter(getContext(), bms1Data);
                    statusListListview.setAdapter(bms1Adapter);
                }
            }
        });


        bms2Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(basicTv);
                removeUnderline(statusTv);
                removeUnderline(chargerTv);
                removeUnderline(bms1Tv);
                removeUnderline(bms3Tv);

                underlineTextView(bms2Tv);

                Map<String, JSONObject> map = DataHolder.getInstance().getSocketStatusMapDirectly();

                if (map != null) {
                    // Convert the values of the map into a list
                    List<JSONObject> bms2Data = new ArrayList<>();

                    for (JSONObject obj : map.values()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();
                            simplifiedObj.put("index", obj.getInt("index"));

                            // Get the "bms" object
                            JSONObject bmsObject = obj.getJSONObject("bms");

                            // Get the "cell_v" array from "bms" object
                            JSONArray cellVArray = bmsObject.getJSONArray("cell_v");

                            // Put the "cell_v" array into simplifiedObj
                            simplifiedObj.put("cell_v", cellVArray);

                            bms2Data.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Bms2Adapter bms2Adapter = new Bms2Adapter(getContext(), bms2Data);
                    statusListListview.setAdapter(bms2Adapter);
                }
            }
        });

        bms3Tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView = v;  // Add this line

                removeUnderline(basicTv);
                removeUnderline(statusTv);
                removeUnderline(chargerTv);
                removeUnderline(bms1Tv);
                removeUnderline(bms2Tv);

                underlineTextView(bms3Tv);

                Map<String, JSONObject> map = DataHolder.getInstance().getSocketStatusMapDirectly();

                if (map != null) {
                    // Convert the values of the map into a list
                    List<JSONObject> bms3Data = new ArrayList<>();

                    for (JSONObject obj : map.values()) {
                        try {
                            JSONObject simplifiedObj = new JSONObject();
                            simplifiedObj.put("index", obj.getInt("index"));

                            // Get the "bms" object
                            JSONObject bmsObject = obj.getJSONObject("bms");

                            // Get the "cell_v" array from "bms" object
                            JSONArray cellVArray = bmsObject.getJSONArray("cell_t");

                            // Put the "cell_v" array into simplifiedObj
                            simplifiedObj.put("cell_t", cellVArray);

                            bms3Data.add(simplifiedObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Bms3Adapter bms3Adapter = new Bms3Adapter(getContext(), bms3Data);
                    statusListListview.setAdapter(bms3Adapter);
                }
            }
        });

        basicTv.callOnClick();

        return root;
    }

    private void Databind() {
        statusRefreshBtn = binding.statusRefreshBtn;
        basicTv = binding.basicTv;
        statusTv = binding.statusTv;
        chargerTv = binding.chargerTv;
        bms1Tv = binding.bms1Tv;
        bms2Tv = binding.bms2Tv;
        bms3Tv = binding.bms3Tv;
        statusListListview = binding.statusListListview;
    }

    private void underlineTextView(TextView textView) {
        SpannableString spanString = new SpannableString(textView.getText().toString());
        spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
        textView.setText(spanString);
    }

    private void removeUnderline(TextView textView) {
        textView.setText(textView.getText().toString());
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

