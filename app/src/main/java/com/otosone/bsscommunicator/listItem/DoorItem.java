package com.otosone.bsscommunicator.listItem;

public class DoorItem {
    private boolean isChecked;
    private String id;
    private String doorStatus;

    public DoorItem(boolean isChecked, String id, String doorStatus) {
        this.isChecked = isChecked;
        this.id = id;
        this.doorStatus = doorStatus;
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

    public String getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(String doorStatus) {
        this.doorStatus = doorStatus;
    }
}
