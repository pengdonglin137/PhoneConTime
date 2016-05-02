package com.pengdl.phonecontime;

import android.text.format.DateFormat;

import java.util.Calendar;

/**
 * Created by pengdl on 16-4-26.
 */
public class ShareConst {

    public final static String SHARESP = "PCT_SP";
    public final static String DATEPICKER_VALID = "datepicker_valid";
    public final static String INVALID = "invalid";
    public final static String VALID = "valid";
    public final static String DATEPICKER_YEAR = "datepicker_year";
    public final static String DATEPICKER_MONTH = "datepicker_month";
    public final static String DATEPICKER_DAY = "datepicker_day";
    public final static String MASK = "99-99-99";
    public final static String ALARM_MASK = "com.pengdl.phonecontime.alarm";
    public final static int STAGECOUNT = 12;

    public static CharSequence GetNowYMD (){
        return DateFormat.format("20yy-MM-dd", Calendar.getInstance());
    }

    public static CharSequence GetNowHMS (){
        return DateFormat.format("HH-mm-ss", Calendar.getInstance());
    }

    public static CharSequence GetNowYMD_HMS() {
        return GetNowYMD() + " " + GetNowHMS();
    }

    public static long currentTimeSeconds() {
        return System.currentTimeMillis()/1000;
    }

    public static long getMillisBasedOnDate(DateTime datetime) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, datetime.getHour());
        calendar.set(Calendar.MINUTE, datetime.getMinutes());
        calendar.set(Calendar.SECOND, datetime.getSeconds());

        return calendar.getTimeInMillis();
    }
}

class DateTime {

    DateTime(int hour, int minutes, int seconds) {
        this.hour = hour;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    private int hour;
    private int minutes;

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    private int seconds;

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
}
