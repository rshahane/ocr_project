package com.paragyte.ocrproject.classes;

public class DateWithFormat {

    String date;
    String dateFormat;

    public DateWithFormat(String date, String dateFormat) {
        this.date = date;
        this.dateFormat = dateFormat;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}
