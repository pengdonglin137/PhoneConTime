package com.pengdl.phonecontime;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

public class MainService extends Service {

    private final static String TAG = "PCT_S";
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
    private AlarmManager am;


    private StageItem Latest_stageItem = null;

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
            } else if (Intent.ACTION_TIME_CHANGED.equals(srcAction)) {
                Log.d(TAG, "ACTION_TIME_CHANGED received.");
                //resetData(TIMECHANGE);
            } else if (ShareConst.ALARM_MASK.equals(srcAction)) {
                String date = intent.getStringExtra("date");
                int hour = intent.getIntExtra("hour", 0);
                Log.d(TAG, "Alarm event received, date:" + date + ", hour:" + intent.getIntExtra("hour", 0));
                Log.d(TAG, "olddate: " + intent.getStringExtra("olddate") + ", oldhour: " + intent.getIntExtra("oldhour", 0));

                if (date.split(" ")[1].equals("00-00-00")) {
                    Log.d(TAG, "Another day coming!");
                    String old_date = intent.getStringExtra("olddate");
                    int old_hour = intent.getIntExtra("oldhour", 0);

                    Boolean isactive = mpowermanager.isScreenOn();
                    if (isactive) {
                        addToDb(old_date,
                                SCREEN_OFF, mkeyguardManager.inKeyguardRestrictedInputMode(),
                                ShareConst.currentTimeSeconds());
                    }

                    updateLatestStageItem(getStageItemIndex(old_hour));
                    resetData(ANOTHERDAY);
                    initLatestStageItem(date.split(" ")[0]);

                } else {
                    updateLatestStageItem(getStageItemIndex(hour));
                }

                setNextAlarm();
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
        Latest_stageItem = null;
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
        } else {
            event = new screenEvent();
            event.setTime_ymd(ShareConst.GetNowYMD().toString());
            event.setTime_hms(ShareConst.MASK);
            event.setSeconds(ShareConst.currentTimeSeconds());
            event.setDuration(0);
            event.setType(SCREEN_OFF);
            event.setValid(true);
            event.setKb_locked(false);

            updateLatestEvent(event);
            event.setTime_hms(ShareConst.GetNowHMS().toString());
            setPreScnOff(event);
        }

        Latest_stageItem = dbMgr.QueryStageItem(ShareConst.GetNowYMD().toString());
        if (Latest_stageItem == null) {
            Log.d(TAG, "Can not find Latest_stageItem in database.");
            Latest_stageItem = new StageItem();
            Latest_stageItem.setTime_ymd(ShareConst.GetNowYMD().toString());
            int i = 0;
            while (i < ShareConst.STAGECOUNT) {
                Latest_stageItem.addValue(0);
                i++;
            }

            Latest_stageItem = dbMgr.addStageItem(Latest_stageItem);
        }

        updateLatestStageItem(getStageItemIndex(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
    }

    private void otherInit() {

        mkeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        mpowermanager = (PowerManager)getSystemService(POWER_SERVICE);

        /* receiver init */
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentfilter.addAction(Intent.ACTION_SCREEN_ON);
        intentfilter.addAction(Intent.ACTION_USER_PRESENT);
        intentfilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentfilter.addAction(ShareConst.ALARM_MASK);
        screenBroadcastReceiver = new ScreenBroadcastReceiver();
        this.registerReceiver(screenBroadcastReceiver, intentfilter);

        am = (AlarmManager)getSystemService(ALARM_SERVICE);
        setNextAlarm();
    }

    public void setNextAlarm() {
        Intent alarmIntent = new Intent(ShareConst.ALARM_MASK);;

        Calendar calendar = Calendar.getInstance();
        alarmIntent.putExtra("olddate", DateFormat.format("20yy-MM-dd HH-mm-ss", calendar));
        alarmIntent.putExtra("oldhour", calendar.get(Calendar.HOUR_OF_DAY));

        long timeMills = ShareConst.getMillisBasedOnDate(getNextDateTime());
        calendar.setTimeInMillis(timeMills);
        alarmIntent.putExtra("date", DateFormat.format("20yy-MM-dd HH-mm-ss", calendar));
        alarmIntent.putExtra("hour", calendar.get(Calendar.HOUR_OF_DAY));

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.set(AlarmManager.RTC, timeMills, alarmPendingIntent);
        //am.setRepeating(AlarmManager.RTC, timeMills, 30*1000, alarmPendingIntent);
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

        switch (evenType)
        {
            case SCREEN_OFF:
                event.setDuration(computeDuration(event));
                setPreScnOff(event);
                //dbMgr.addItem(event);
                //dumpEvent(event);
                updateLatestEvent(event);
                updateLatestStageItem(getStageItemIndex(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
                break;
            case SCREEN_ON:
                setPreScnOn(event);
                break;
            case USER_PRESENT:
                setPrevUserPre(event);
                //dbMgr.addItem(event);
                //dumpEvent(event);
                break;
            default:
                break;
        }
    }

    private screenEvent find_event(screenEvent event) {

        screenEvent compareEvent = null;

        if (getPrevUserPre() != null && getPreScnOn() != null) {
            if (getPreScnOn().getKb_locked()) {
                compareEvent = getPrevUserPre();
            } else {
                compareEvent = getPreScnOn();
            }
        } else if (getPreScnOn() != null) {
            compareEvent = getPreScnOn();
            if (event != null && event.getKb_locked()) {
                compareEvent.setValid(false);
            }
        } else if (getPrevUserPre() != null) {
            compareEvent = getPrevUserPre();
        }

        return compareEvent;
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

        screenEvent compareEvent = find_event(event);

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
                screenEvent compareEvent = find_event(null);
                if (compareEvent != null) {
                    duration = ShareConst.currentTimeSeconds() - compareEvent.getSeconds();
                } else {
                    duration = ShareConst.currentTimeSeconds() - getServerStartTime();
                }

                Log.d(TAG, "duration: " + duration + ", currentSeconds: " + ShareConst.currentTimeSeconds() + ", lastSeconds: " + event.getSeconds());
            }

            return FormatMiss(event.getDuration() + duration);
        }
    }

    public int getStageItemIndex(int hour) {
        int index = 0;

        if (hour >= 0 && hour <2) {
            index = 0;
        } else if (hour >= 2 && hour < 4) {
            index = 1;
        } else if (hour >= 4 && hour < 6) {
            index = 2;
        } else if (hour >= 6 && hour < 8) {
            index = 3;
        } else if (hour >= 8 && hour < 10) {
            index = 4;
        } else if (hour >= 10 && hour < 12) {
            index = 5;
        } else if (hour >= 12 && hour < 14) {
            index = 6;
        } else if (hour >= 14 && hour < 16) {
            index = 7;
        } else if (hour >= 16 && hour < 18) {
            index = 8;
        } else if (hour >= 18 && hour < 20) {
            index = 9;
        } else if (hour >= 20 && hour < 22) {
            index = 10;
        }  else if (hour >= 22 && hour <= 23) {
            index = 11;
        }

        return index;
    }

    public DateTime getNextDateTime() {
        int hour_n = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); // 24
        int minues_c = Calendar.getInstance().get(Calendar.MINUTE);
        int minues_n = 0;

        if (minues_c >= 0 && minues_c < 30) {
            minues_n = 30;
        } else if (minues_c >= 30 && minues_c <= 59) {
            minues_n = 0;
            hour_n += 1;
        }

        Log.d(TAG, "next Date, hour: " + hour_n + ", minutes: " + minues_n);

        return new DateTime(hour_n, minues_n, 0);
    }

    public void updateLatestStageItem(int index) {
        Latest_stageItem.updateValue(index, getAllTimeDuration());
        dbMgr.updateStageItem(Latest_stageItem);
        dumpStageItem();
    }

    public void dumpStageItem() {
        String tr = new String();
        int i = 0;
        while (i < ShareConst.STAGECOUNT) {
            tr = tr + DatabaseManager.stageNameIndex[i] + ": " + Latest_stageItem.getStage(i) + "\n";
            i++;
        }

        Log.d(TAG, "*************************\nid: " + Latest_stageItem.getId()
        + "\nYMD: " + Latest_stageItem.getTime_ymd() + "\n" + tr +
                   "**************************");
    }

    public void initLatestStageItem(String date) {

        Latest_stageItem = dbMgr.QueryStageItem(date);
        if (Latest_stageItem == null) {
            Log.d(TAG, "Can not find Latest_stageItem in database.");
            Latest_stageItem = new StageItem();
            Latest_stageItem.setTime_ymd(ShareConst.GetNowYMD().toString());
            int i = 0;
            while (i < ShareConst.STAGECOUNT) {
                Latest_stageItem.addValue(0);
                i++;
            }

            Latest_stageItem = dbMgr.addStageItem(Latest_stageItem);
        }
    }
}
