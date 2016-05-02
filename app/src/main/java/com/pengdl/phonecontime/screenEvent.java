package com.pengdl.phonecontime;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by pengdl on 4/24/16.
 */
public class screenEvent {
    private long id;
    private String type;
    private String time_ymd;
    private String time_hms;
    private long duration;
    private Boolean valid;
    private Boolean kb_locked;
    private long seconds;

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public Boolean getKb_locked() {
        return kb_locked;
    }

    public void setKb_locked(Boolean kb_locked) {
        this.kb_locked = kb_locked;
    }

    public screenEvent() {


    }

    public String getType() {
        return type;
    }

    public String getTime_ymd() {
        return time_ymd;
    }

    public void setTime_ymd(String time_ymd) {
        this.time_ymd = time_ymd;
    }

    public String getTime_hms() {
        return time_hms;
    }

    public void setTime_hms(String time_hms) {
        this.time_hms = time_hms;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}


class StageItem {
    private final static String TAG = "PCT_StageItem";
    private long id;
    private String time_ymd;
    private List<Long> stage = new ArrayList<Long>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTime_ymd() {
        return time_ymd;
    }

    public void setTime_ymd(String time_ymd) {
        this.time_ymd = time_ymd;
    }

    public int getCount () {
        return stage.size();
    }

    public long getStage(int index) {
        //Log.d(TAG, "getStage");
        return stage.get(index);
    }

    public void addValue(long value) {
        //Log.d(TAG, "addValue");
        stage.add(value);
    }

    public void updateValue(int index, long value) {
        //Log.d(TAG, "updateValue");
        stage.set(index, value);
    }
}