package com.otosone.bsscommunicator;

public class DoorItem {
    private boolean isChecked;
    private String text1;
    private String text2;

    public DoorItem(boolean isChecked, String text1, String text2) {
        this.isChecked = isChecked;
        this.text1 = text1;
        this.text2 = text2;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }
}
