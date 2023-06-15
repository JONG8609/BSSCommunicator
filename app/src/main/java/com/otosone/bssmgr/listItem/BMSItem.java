package com.otosone.bssmgr.listItem;

public class BMSItem {
    private boolean isChecked;
    private String id;
    private int cmd;
    private int value;

    public BMSItem(boolean isChecked, String id, int cmd, int value) {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
