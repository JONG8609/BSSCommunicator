package com.otosone.bssmgr.listItem;

public class ChargingItem {
    private boolean isChecked;
    private String id;
    private String charging;

    public ChargingItem(boolean isChecked, String id, String charging) {
        this.isChecked = isChecked;
        this.id = id;
        this.charging = charging;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCharging() {
        return charging;
    }

    public void setCharging(String charging) {
        this.charging = charging;
    }
}
