package com.otosone.bsscommunicator.bluetooth;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;

import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.ScanActivity;
import com.otosone.bsscommunicator.utils.DataHolder;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BluetoothConnectionService extends Service {

    private final IBinder binder = new LocalBinder();
    private Disposable connectionDisposable;
    private RxBleConnection connection;
    private RxBleClient rxBleClient;
    private MessageReceivedListener messageReceivedListener;
    private ConnectionStateListener connectionStateListener;
    private JSONObject bssStatus;
    private Map<String, JSONObject> socketStatusMap = new HashMap<>();

    private static final String CHANNEL_ID = "BluetoothConnectionServiceChannel";
    private ConnectionFailedListener connectionFailedListener;

    private Disposable notificationDisposable;
    private StringBuilder receivedMessageBuilder = new StringBuilder();
    public interface MessageReceivedListener {
        void onMessageReceived(String message);
    }

    public void setMessageReceivedListener(MessageReceivedListener messageReceivedListener) {
        this.messageReceivedListener = messageReceivedListener;
        Log.d("BluetoothConnService", "MessageReceivedListener set");
    }

    public class LocalBinder extends Binder {
        public BluetoothConnectionService getService() {
            return BluetoothConnectionService.this;
        }
    }

    public void setConnectionStateListener(ConnectionStateListener listener) {
        this.connectionStateListener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rxBleClient = RxBleClient.create(this);
        startScanActivityIfNeeded();
    }

    public interface ConnectionStateListener {
        void onDeviceConnected();

        void onDeviceDisconnected();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void connectToDevice(RxBleDevice device) {
        disconnect();

        connectionDisposable = device.establishConnection(false)
                .subscribe(
                        connection -> {
                            onDeviceConnected(connection);
                            if (connectionStateListener != null) {
                                connectionStateListener.onDeviceConnected();
                            }
                        },
                        throwable -> {
                            Log.e("ConnectionError", "Error connecting to device", throwable);
                        }
                );
    }

    public void disconnect() {
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
            if (connectionStateListener != null) {
                connectionStateListener.onDeviceDisconnected();
            }
            startScanActivityIfNeeded();
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    private void onDeviceConnected(RxBleConnection connection) {
        this.connection = connection;
        setupNotification(connection);
        sendrequestInfo();
        sendBssStatusRequest();
        for (int index = 0; index <= 15; index++) {
            sendSocketStatusRequest(index);
        }
    }

    public void sendrequestInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("request", "INFO");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(jsonObject.toString());
    }

    public void sendBssStatusRequest(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("request", "BSS_STATUS");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(jsonObject.toString());
    }

    public void sendSocketStatusRequest(int index){
        // Create a JSON object
        JSONObject json = new JSONObject();
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("index", index);

            json.put("request", "SOCKET_STATUS");
            json.put("data", dataJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonString = json.toString();
        Log.d("UTF-8", jsonString);

        // Call the sendAsciiMessage method with the string as an argument
        sendMessage(jsonString);

    }

    public void sendMessage(String message) {
        if (connection == null || message
                == null) {
            Log.e("SendAsciiMessageError", "Connection is null or message is null");
            return;
        }

        // \r\n 더해주기
        message += "\r\n";

        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        String dataAsString = new String(data, StandardCharsets.UTF_8);
        Log.d("SendData", dataAsString);

        // Determine the chunk size
        int chunkSize = 10; // You can adjust this value depending on your needs

        for (int i = 0; i < data.length; i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, data.length);
            byte[] chunk = Arrays.copyOfRange(data, i, endIndex);

            connection.writeCharacteristic(UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"), chunk)
                    .delay(50, TimeUnit.MILLISECONDS, Schedulers.io())
                    .subscribe(
                            bytes -> {
                                Log.d("Response", new String(bytes, StandardCharsets.UTF_8));
                            },
                            throwable -> {
                                Log.e("SendDataError", "Error sending data", throwable);
                            }
                    );

        }
    }

    private void setupNotification(RxBleConnection connection) {
        connection.setupNotification(UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"))
                .flatMap(notificationObservable -> notificationObservable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        bytes -> {
                            String receivedMessage = new String(bytes, StandardCharsets.UTF_8);
                            Log.d("Received UTF-8 Message", receivedMessage);
                            processReceivedData(receivedMessage);
                        },
                        throwable -> {
                            Log.e("ReceiveDataError", "Error receiving data: " + throwable.getMessage());
                        }
                );
    }

    private void processReceivedData(String receivedMessage) {
        Log.d("ProcessReceivedData", "Received message: " + receivedMessage);

        receivedMessageBuilder.append(receivedMessage);

        int openBraceIndex = receivedMessageBuilder.indexOf("{");
        while (openBraceIndex != -1) {
            int closeBraceIndex = findMatchingClosingBrace(receivedMessageBuilder, openBraceIndex);

            if (closeBraceIndex != -1) {
                String completeJsonString = receivedMessageBuilder.substring(openBraceIndex, closeBraceIndex + 1);
                Log.d("ProcessReceivedData", "Complete JSON String: " + completeJsonString);

                if (messageReceivedListener != null) {
                    messageReceivedListener.onMessageReceived(completeJsonString);

                    try {
                        JSONObject receivedJson = new JSONObject(completeJsonString);

                        if (receivedJson.has("response") && receivedJson.getString("response").equals("INFO")) {
                            String apkVersion = receivedJson.getJSONObject("data").getString("apkVersion");
                            saveApkVersion(apkVersion);
                        }

                        // Check both "request" and "response"
                        if (receivedJson.has("request") || receivedJson.has("response")) {
                            String messageType = receivedJson.has("request") ? "request" : "response";
                            String type = receivedJson.getString(messageType);

                            // Check if type is "BSS_STATUS" or "SOCKET_STATUS"
                            if (type.equals("BSS_STATUS") || type.equals("SOCKET_STATUS")) {
                                JSONObject data = receivedJson.getJSONObject("data");

                                if (type.equals("BSS_STATUS")) {
                                    DataHolder.getInstance().setBssStatus(data); // Store BSS_STATUS
                                    bssStatus = data;
                                } else {
                                    String index = data.getString("index");
                                    LiveData<Map<String, JSONObject>> liveData = DataHolder.getInstance().getSocketStatusMap();
                                    Map<String, JSONObject> currentMap = liveData != null ? liveData.getValue() : null;
                                    if (currentMap == null) {
                                        currentMap = new HashMap<>();
                                    }
                                    Map<String, JSONObject> newMap = new HashMap<>(currentMap);
                                    newMap.put(index, data);
                                    DataHolder.getInstance().setSocketStatusMap(newMap); // Store SOCKET_STATUS
                                    Log.d("BluetoothConnService", "Added SOCKET_STATUS for index " + index);

                                    // Update the socketStatusMap reference
                                    socketStatusMap = newMap;
                                }
                            }
                            if (socketStatusMap.size() == 16) {
                                Log.d("BluetoothConnService", "All SOCKET_STATUS messages processed. Creating log file.");
                                createLogFile();
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("BluetoothConnService", "Error parsing received JSON", e);
                    }
                } else {
                    Log.d("ProcessReceivedData", "messageReceivedListener is null");
                }

                receivedMessageBuilder.delete(0, closeBraceIndex + 1);
            } else {
                break;
            }

            openBraceIndex = receivedMessageBuilder.indexOf("{");
        }
    }

    private void saveApkVersion(String apkVersion) {
        SharedPreferences sharedPref = getSharedPreferences("apkVersion", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("apkVersion", apkVersion);
        editor.apply();
    }

    private void createLogFile() {
        new Thread(() -> {
            try {
                String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
                Log.d("BluetoothConnService", "Current date: " + currentDate);

                // Get the external storage directory
                File externalDir = getExternalFilesDir(null);
                if (externalDir == null) {
                    Log.e("BluetoothConnService", "Error getting external storage directory.");
                    return;
                }

                Log.d("BluetoothConnService", "External dir: " + externalDir.getAbsolutePath());

                // Create the log file
                String stationId = bssStatus.getString("stationId");
                File logFile = new File(externalDir, stationId +"_" + currentDate + ".log");
                Log.d("BluetoothConnService", "Log file path: " + logFile.getAbsolutePath());

                // Checking if the file already exists
                if (logFile.exists()) {
                    Log.d("BluetoothConnService", "Log file for today already exists.");
                } else {
                    boolean isCreated = logFile.createNewFile();
                    if (isCreated) {
                        Log.d("BluetoothConnService", "Created new log file for today.");
                    } else {
                        Log.e("BluetoothConnService", "Failed to create log file.");
                        return;
                    }
                }

                FileOutputStream fos = new FileOutputStream(logFile, true);
                OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

                // Here, create your log file content based on bssStatus and socketStatusMap
                String logContent = createLogContent();

                osw.write(logContent);
                osw.close();
                fos.close();
                Log.d("BluetoothConnService", "Successfully wrote to the file.");

                // Clear the socketStatusMap after the log file has been created
                socketStatusMap.clear();

            } catch (IOException e) {
                Log.e("BluetoothConnService", "Error writing to log file", e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }




    private String createLogContent() {
        try {
            // Main JSON object to hold everything
            JSONObject logContentJson = new JSONObject();

            // Add timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
            logContentJson.put("timestamp", timestamp);

            // Add BSS_STATUS
            logContentJson.put("BSS_STATUS", bssStatus);

            // Add SOCKET_STATUS
            JSONArray socketStatusArray = new JSONArray();
            for (int i = 0; i < 16; i++) {
                JSONObject socketStatusJson = socketStatusMap.get(String.valueOf(i));
                if (socketStatusJson != null) {
                    socketStatusArray.put(socketStatusJson);
                }
            }
            logContentJson.put("SOCKET_STATUS", socketStatusArray);

            // Create compact string from JSON object
            return logContentJson.toString();
        } catch (JSONException e) {
            Log.e("BluetoothConnService", "Error creating log content", e);
            return null;
        }
    }




    private int findMatchingClosingBrace(StringBuilder builder, int openBraceIndex) {
        int balance = 0;
        for (int i = openBraceIndex; i < builder.length(); i++) {
            char currentChar = builder.charAt(i);
            if (currentChar == '{') {
                balance++;
            } else if (currentChar == '}') {
                balance--;
            }

            if (balance == 0) {
                return i;
            }
        }

        return -1;
    }



    public void setConnectionFailedListener(ConnectionFailedListener connectionFailedListener) {
        this.connectionFailedListener = connectionFailedListener;
    }

    public void scanBleDevices(ScanSettings scanSettings, ScanFilter scanFilter, Consumer<ScanResult> onScanResult) {
        Disposable scanDisposable = rxBleClient.scanBleDevices(scanSettings, scanFilter)
                .subscribe(onScanResult, throwable -> {
                    // Handle error
                });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForegroundServiceWithNotification();
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Bluetooth Connection Service";
            String description = "Service for managing Bluetooth connections";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void startScanActivityIfNeeded() {
        if (connection == null) {
            Intent scanActivityIntent = new Intent(this, ScanActivity.class);
            scanActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(scanActivityIntent);
        }
    }


    private void startForegroundServiceWithNotification() {
        Intent notificationIntent = new Intent(this, ScanActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bluetooth Connection Service")
                .setContentText("Service is running")
                .setSmallIcon(R.mipmap.ic_launcher) // Replace with your own notification icon
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }
}
