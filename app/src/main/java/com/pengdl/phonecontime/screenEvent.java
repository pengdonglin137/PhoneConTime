package com.pengdl.phonecontime;

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
    private long id_prev;
    private long id_next;
    private long seconds;

    public long getId_prev() {
        return id_prev;
    }

    public void setId_prev(long id_prev) {
        this.id_prev = id_prev;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public long getId_next() {
        return id_next;

    }

    public void setId_next(long id_next) {
        this.id_next = id_next;
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
