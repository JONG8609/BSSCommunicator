package com.otosone.bssmgr.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DataHolder {

    private static DataHolder instance;
    private MutableLiveData<JSONObject> bssStatus, info;
    private MutableLiveData<Map<String, JSONObject>> socketStatusMap;
    private MutableLiveData<Map<String, String>> binaryStatusMap;
    private MutableLiveData<Boolean> allDataReceived;

    private DataHolder() {
        socketStatusMap = new MutableLiveData<>(new HashMap<>());
        binaryStatusMap = new MutableLiveData<>(new HashMap<>());
        allDataReceived = new MutableLiveData<>(false);
        bssStatus = new MutableLiveData<>();
        info = new MutableLiveData<>();
    }

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public void setBssStatus(JSONObject bssStatus) {
        this.bssStatus.postValue(bssStatus);
        checkAllDataReceived();
    }

    public LiveData<JSONObject> getBssStatus() {
        return bssStatus;
    }

    public void setInfo(JSONObject info) {
        this.info.postValue(info);
        checkAllDataReceived();
    }

    public LiveData<JSONObject> getInfo() {
        return info;
    }

    public void setSocketStatusMap(Map<String, JSONObject> socketStatusMap) {
        this.socketStatusMap.postValue(socketStatusMap);
        checkAllDataReceived();
    }

    public LiveData<Map<String, JSONObject>> getSocketStatusMap() {
        return socketStatusMap;
    }

    public void setBinaryStatusMap(Map<String, String> binaryStatusMap) {
        this.binaryStatusMap.postValue(binaryStatusMap);
    }

    public LiveData<Map<String, String>> getBinaryStatusMap() {
        return binaryStatusMap;
    }

    public LiveData<Boolean> getAllDataReceived() {
        return allDataReceived;
    }

    public void setAllDataReceived(boolean value) {
        this.allDataReceived.postValue(value);
    }

    public void resetData() {
        // Reset LiveData values for next round of data
        bssStatus.postValue(null);
        socketStatusMap.postValue(new HashMap<>());
    }

    private void checkAllDataReceived() {
        if (bssStatus.getValue() != null && socketStatusMap.getValue() != null && socketStatusMap.getValue().size() == 16) {
            // Only emit when all data is ready
            allDataReceived.postValue(true);
        }
    }
}
