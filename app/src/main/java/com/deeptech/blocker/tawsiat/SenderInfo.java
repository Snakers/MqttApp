package com.deeptech.blocker.tawsiat;

public class SenderInfo {
    private String name, data , date ;
    private boolean isArabic;

    public boolean isArabic() {
        return isArabic;
    }

    public void setArabic(boolean arabic) {
        isArabic = arabic;
    }

    public SenderInfo(String name, String data, String date,boolean isArabic) {
        this.name = name;
        this.data = data;
        this.isArabic=isArabic;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
