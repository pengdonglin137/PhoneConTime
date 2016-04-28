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
}
