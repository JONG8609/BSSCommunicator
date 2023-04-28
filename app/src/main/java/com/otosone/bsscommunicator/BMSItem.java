package com.otosone.bsscommunicator;

public class BMSItem {
    private boolean isChecked;
    private String id;
    private String cmd;
    private int value;
    public BMSItem(boolean isChecked, String id, int value) {
        this.isChecked = isChecked;
        this.id = id;
        this.value = value;
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

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
