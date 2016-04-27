package com.pengdl.phonecontime;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

public class MainService extends Service {

    private final static String TAG = "OTP_S";
    private long serverStartTime = 0;
    private ScreenBroadcastReceiver screenBroadcastReceiver;
    private MainBinder mainbinder = new MainBinder();
    private CharSequence time = "00:00:00";
    private long allTimeDuration = 0;
    private DatabaseManager dbMgr;
    private final static String SCREEN_OFF = "SCREEN_OFF";
    private final static String SCREEN_ON = "SCREEN_ON";
    private final static String USER_PRESENT = "USER_PRESENT";
    private screenEvent prevUserPre = null;
    private screenEvent preScnOff = null;
    private screenEvent preScnOn = null;
    private screenEvent cache_latest = null;
    private KeyguardManager mkeyguardManager;
    private PowerManager mpowermanager;
    private final static int DELETE_DB = 0;
    private final static int ANOTHERDAY = 1;
    private final static int TIMECHANGE = 2;

    class ScreenBroadcastReceiver extends BroadcastReceiver {



        @Override
        public void onReceive(Context context, Intent intent) {
            String srcAction = intent.getAction();
            long seconds;

            Log.d(TAG, "onReceive enter.");

            time = ShareConst.GetNowYMD_HMS();
            seconds = ShareConst.currentTimeSeconds();
            Boolean kb_locked = mkeyguardManager.inKeyguardRestrictedInputMode();
            if (Intent.ACTION_SCREEN_OFF.equals(srcAction)) {
                Log.d(TAG, "ACTION_SCREEN_OFF received, kb_locked: " + kb_locked);
                addToDb(time, SCREEN_OFF, kb_locked, seconds);
            } else if (Intent.ACTION_SCREEN_ON.equals(srcAction)) {
                Log.d(TAG, "ACTION_SCREEN_ON received, kb_locked: " + kb_locked);
                addToDb(time, SCREEN_ON, kb_locked, seconds);
            } else if (Intent.ACTION_USER_PRESENT.equals(srcAction)) {
                Log.d(TAG, "ACTION_USER_PRESENT received, kb_locked: " + kb_locked);
                addToDb(time, USER_PRESENT, kb_locked, seconds);
            } else if (Intent.ACTION_TIME_TICK.equals(srcAction)){
                Log.d(TAG, "ACTION_TIME_TICK received, time: " + time);

            } else if (Intent.ACTION_TIME_CHANGED.equals(srcAction)) {
                Log.d(TAG, "ACTION_TIME_CHANGED received.");
                resetData(TIMECHANGE);
            }
        }
    }

    public MainService() {
    }

    public long getAllTimeDuration() {
        return allTimeDuration;
    }

    public long setAllTimeDuration(long allTimeDuration) {
        this.allTimeDuration = allTimeDuration;
        return allTimeDuration;
    }

    public long getServerStartTime() {
        return serverStartTime;
    }

    public void setServerStartTime(long serverStartTime) {
        this.serverStartTime = serverStartTime;
    }

    public screenEvent getPreScnOn() {
        return preScnOn;
    }

    public void setPreScnOn(screenEvent preScnOn) {
        this.preScnOn = preScnOn;
    }

    public screenEvent getPreScnOff() {
        return preScnOff;
    }

    public void setPreScnOff(screenEvent preScnOff) {
        this.preScnOff = preScnOff;
    }

    public screenEvent getPrevUserPre() {
        return prevUserPre;
    }

    public void setPrevUserPre(screenEvent prevUserPre) {
        this.prevUserPre = prevUserPre;
    }

    public class MainBinder extends Binder {
        public CharSequence onThePhoneTime() {

            return getAllDuration();
        }

        public CharSequence onThePhoneTimeAt(String date, boolean flag) {

            return getAllDurationAt(date, flag);
        }

        public void showEvents(){
            int num = 0;
            List<screenEvent> events = dbMgr.getAllEvents();

            while(num < events.size()) {
                screenEvent event = events.get(num);
                dumpEvent(event);
                num++;
            }
        }

        public void deleteEvents() {
            resetData(DELETE_DB);
            dbMgr.deleteEvents();
        }
    }

    public void resetData(int reason) {
        Log.d(TAG, "Reseting data, reason: " + reason);

        if (reason == ANOTHERDAY) {

        }

        cache_latest = null;
        setPreScnOn(null);
        setPreScnOff(null);
        setPrevUserPre(null);

        setAllTimeDuration(0);
        setServerStartTime(ShareConst.currentTimeSeconds());
    }

    private void dumpEvent(screenEvent event) {

        Log.d(TAG, "***************************"
                + "\n\tid: " + event.getId()
                + "\n\ttype: " + event.getType()
                + "\n\ttime: " + event.getTime_ymd() + " " + event.getTime_hms()
                + "\n\tSeconds: " + event.getSeconds()
                + "\n\tduration: "+ event.getDuration()
                + "\n\tvalid: " + event.getValid()
                + "\n\tkb_locked: " + event.getKb_locked()
                + "\n\tid_prev: " + event.getId_prev()
                + "\n\tid_next: " + event.getId_next()
                + "\n***************************");
    }


    public void onCreate() {
        super.onCreate();

        /* Get the server start time */
        setServerStartTime(ShareConst.currentTimeSeconds());

        dataBaseInit();

        otherInit();

        Log.d(TAG, "Started.");
    }

    private void dataBaseInit() {
        dbMgr = new DatabaseManager(this);

        screenEvent event = new screenEvent();

        event.setTime_ymd(ShareConst.GetNowYMD().toString());
        event.setTime_hms(ShareConst.MASK);

        event = dbMgr.queryEvent(event);

        if (event != null) {
            dumpEvent(event);
            setAllTimeDuration(event.getDuration());
            setPreScnOff(event);
        }
    }

    private void otherInit() {

        mkeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        mpowermanager = (PowerManager)getSystemService(POWER_SERVICE);

        /* receiver init */
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentfilter.addAction(Intent.ACTION_SCREEN_ON);
        intentfilter.addAction(Intent.ACTION_USER_PRESENT);
        intentfilter.addAction(Intent.ACTION_TIME_TICK);
        intentfilter.addAction(Intent.ACTION_TIME_CHANGED);
        screenBroadcastReceiver = new ScreenBroadcastReceiver();
        this.registerReceiver(screenBroadcastReceiver, intentfilter);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mainbinder;
    }

    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(screenBroadcastReceiver);

        Boolean isactive = mpowermanager.isScreenOn();
        if (isactive) {
            addToDb(ShareConst.GetNowYMD_HMS(),
                    SCREEN_OFF, mkeyguardManager.inKeyguardRestrictedInputMode(),
                    ShareConst.currentTimeSeconds());
        }


        Log.d(TAG, "Destroyed.");
    }

    private void addToDb(CharSequence time, String evenType, Boolean kb_locked, long seconds) {
        screenEvent event = new screenEvent();

        event.setTime_ymd(time.toString().split(" ")[0].trim());
        event.setTime_hms(time.toString().split(" ")[1].trim());
        event.setSeconds(seconds);
        event.setType(evenType);
        event.setKb_locked(kb_locked);

        event.setDuration(0);
        event.setValid(true);
        event.setId_prev(0);
        event.setId_next(0);

        switch (evenType)
        {
            case SCREEN_OFF:
                if (getPreScnOff() == null) {
                    setPreScnOff(event);
                }
                event.setDuration(computeDuration(event));
                setPreScnOff(event);
                dbMgr.addItem(event);
                dumpEvent(event);
                updateLatestEvent(event);
                break;
            case SCREEN_ON:
                setPreScnOn(event);
                break;
            case USER_PRESENT:
                setPrevUserPre(event);
                dbMgr.addItem(event);
                dumpEvent(event);
                break;
            default:
                break;
        }
    }

    private long computeDuration(screenEvent event) {
        long duration = 0;
        long prev_duration = 0;

        if (!event.getType().equals("SCREEN_OFF")) {
            Log.e(TAG, "only compute duration at screen off event arriving.");
            return duration;
        }

        if (getPreScnOff() != null) {
            prev_duration = getPreScnOff().getDuration();
        }

        screenEvent compareEvent = null;

        if (getPrevUserPre() != null && getPreScnOn() != null) {
            if (getPreScnOn().getKb_locked()) {
                compareEvent = getPrevUserPre();
            } else {
                compareEvent = getPreScnOn();
            }
        } else if (getPreScnOn() != null) {
            compareEvent = getPreScnOn();
            if (event.getKb_locked()) {
                compareEvent.setValid(false);
            }
        } else if (getPrevUserPre() != null) {
            compareEvent = getPrevUserPre();
        }

        if (compareEvent != null) {
            if (compareEvent.getValid()) {
                Log.d(TAG, "compare event type: " + compareEvent.getType() + ", time: " + compareEvent.getTime_hms());
                compareEvent.setValid(false);
                duration = event.getSeconds() - compareEvent.getSeconds();
            } else {
                duration = 0;
            }
        } else {
            duration = event.getSeconds() - getServerStartTime();
        }

        Log.d(TAG, "This time duration: " + duration + ", all time duration: " + (duration + prev_duration) + ".");

        return setAllTimeDuration(duration+prev_duration);
    }

    private String getAllDuration() {

        return FormatMiss(getAllTimeDuration());
    }

    private String FormatMiss(long duration) {

        byte hour = (byte) (duration / 3600);
        byte min = (byte) (duration % 3600 / 60);
        byte sec = (byte) (duration % 3600 % 60);

        String hh = hour>9 ? hour + "" : "0" + hour;
        String mm = min>9 ? min + "" : "0" + min;
        String ss = sec>9 ? sec + "" : "0" + sec;

        return hh + ":" + mm + ":" + ss;
    }

    private void updateLatestEvent(screenEvent event) {
        Log.d(TAG, "UpdateLatestEvent.");
        screenEvent latest = new screenEvent();

        latest.setType(event.getType());
        latest.setTime_ymd(event.getTime_ymd());
        latest.setTime_hms(ShareConst.MASK);
        latest.setDuration(event.getDuration());
        latest.setSeconds(event.getSeconds());
        latest.setValid(event.getValid());
        latest.setKb_locked(event.getKb_locked());
        latest.setId_next(0);
        latest.setId_prev(0);

        if (cache_latest != null) {
            if (cache_latest.getTime_ymd().equals(event.getTime_ymd())) {
                cache_latest.setDuration(event.getDuration());
                cache_latest.setSeconds(event.getSeconds());
                dbMgr.updateEvent(cache_latest);
            } else {
                cache_latest = dbMgr.addItem(latest);
            }
        } else if ((cache_latest = dbMgr.queryEvent(latest)) == null) {
            cache_latest = dbMgr.addItem(latest);
        } else {
            cache_latest.setDuration(latest.getDuration());
            dbMgr.updateEvent(cache_latest);
        }

        dumpEvent(cache_latest);
    }

    public CharSequence getAllDurationAt(String date, boolean flag) {
        screenEvent event = new screenEvent();

        event.setTime_ymd(date);
        event.setTime_hms(ShareConst.MASK);

        if ((event = dbMgr.queryEvent(event)) == null) {
            Log.d(TAG, "Not found, date: " + date + ", flag: " + flag);
            if (flag) {
                return FormatMiss(ShareConst.currentTimeSeconds() - getServerStartTime());
            } else {
                return "00:00:00";
            }
        } else {
            long duration = 0;
            Log.d(TAG, "found, date: " + event.getDuration() + ", flag: " + flag);
            if (flag) {
                duration = ShareConst.currentTimeSeconds() - event.getSeconds();
                Log.d(TAG, "duration: " + duration + ", currentSeconds: " + ShareConst.currentTimeSeconds() + ", lastSeconds: " + event.getSeconds());
            }

            return FormatMiss(event.getDuration() + duration);
        }
    }
}
