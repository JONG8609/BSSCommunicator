package com.otosone.bsscommunicator.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DataHolder {

    private static DataHolder instance;
    private MutableLiveData<JSONObject> bssStatus;
    private MutableLiveData<Map<String, JSONObject>> socketStatusMap;

    private DataHolder() {
        socketStatusMap = new MutableLiveData<>(new HashMap<>());
        bssStatus = new MutableLiveData<>();
    }

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public void setBssStatus(JSONObject bssStatus) {
        this.bssStatus.postValue(bssStatus);
    }

    public LiveData<JSONObject> getBssStatus() {
        return bssStatus;
    }

    public void setSocketStatusMap(Map<String, JSONObject> socketStatusMap) {
        this.socketStatusMap.postValue(socketStatusMap);
    }

    public LiveData<Map<String, JSONObject>> getSocketStatusMap() {
        return socketStatusMap;
    }
}