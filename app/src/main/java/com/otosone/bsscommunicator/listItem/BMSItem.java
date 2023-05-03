package com.otosone.bsscommunicator.listItem;

public class BMSItem {
    private boolean isChecked;
    private int id;
    private int cmd;
    private int value;

    public BMSItem(boolean isChecked, int id, int cmd, int value) {
        this.isChecked = isChecked;
        this.id = id;
        this.cmd = cmd;
        this.value = value;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
