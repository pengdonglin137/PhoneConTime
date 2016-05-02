package com.pengdl.phonecontime;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengdl on 4/24/16.
 */
public class DatabaseManager extends SQLiteOpenHelper {

    private final static String TAG = "PCT_DM";

    public final static String TABLE1_NAME = "PCT_TB1";
    public final static String ID_FIELD = "_id";

    /**
     * TYPE_FILED:
     * USR_PRESENT
     * SCREEN_ON
     * SCREEN_OFF
     */
    public final static String TYPE_FIELD = "event_type";
    /**
     * DUR_FIELD: second
     */
    public final static String DUR_FIELD = "duration";
    /**
     * TIME_YMD_FILED: 2016-12-22 (YY-MM-HH)
     */
    public final static String TIME_YMD_FILED = "time_ymd";
    /**
     * TIME_HMS_FILED: 11:49:30 (KK:mm:ss)
     */
    public final static String TIME_HMS_FILED = "time_hms";

    public final static String TIME_IN_SECONDS_FILED = "time_in_seconds";
    /**
     * VALID_FILED:
     * valid:     0
     * invalid:   1
     */
    public final static String VALID_FILED = "valid";
    public final static String KB_LOCK_FILED = "kb_locked";


    /**
     *  table2
     */
    public final static String TABLE2_NAME = "PCT_TB2";
    public final static String STAGE0_2 = "stage0_2";
    public final static String STAGE2_4 = "stage2_4";
    public final static String STAGE4_6 = "stage4_6";
    public final static String STAGE6_8 = "stage6_8";
    public final static String STAGE8_10 = "stage8_10";
    public final static String STAGE10_12 = "stage10_12";
    public final static String STAGE12_14 = "stage12_14";
    public final static String STAGE14_16 = "stage14_16";
    public final static String STAGE16_18 = "stage16_18";
    public final static String STAGE18_20 = "stage18_20";
    public final static String STAGE20_22 = "stage20_22";
    public final static String STAGE22_0 = "stage22_0";

    public final static String[] stageNameIndex = new String[]{
            STAGE0_2,
            STAGE2_4,
            STAGE4_6,
            STAGE6_8,
            STAGE8_10,
            STAGE10_12,
            STAGE12_14,
            STAGE14_16,
            STAGE16_18,
            STAGE18_20,
            STAGE20_22,
            STAGE22_0,
    };

    public DatabaseManager(Context context) {
        super(context, "otp_db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");

        String sql = "CREATE TABLE " + TABLE1_NAME
                + " (" + ID_FIELD + " INTEGER, "
                + TYPE_FIELD + " TEXT, "
                + TIME_YMD_FILED + " TEXT, "
                + TIME_HMS_FILED + " TEXT, "
                + TIME_IN_SECONDS_FILED + " INTEGER, "
                + DUR_FIELD + " INTEGER, "
                + VALID_FILED + " TEXT, "
                + KB_LOCK_FILED + " TEXT, "
                + " PRIMARY KEY (" + ID_FIELD +"));";

        db.execSQL(sql);

        sql = "CREATE TABLE " + TABLE2_NAME
                + " (" + ID_FIELD + " INTEGER, "
                + TIME_YMD_FILED + " TEXT, "
                + STAGE0_2 + " INTEGER, "
                + STAGE2_4 + " INTEGER, "
                + STAGE4_6 + " INTEGER, "
                + STAGE6_8 + " INTEGER, "
                + STAGE8_10 + " INTEGER, "
                + STAGE10_12 + " INTEGER, "
                + STAGE12_14 + " INTEGER, "
                + STAGE14_16 + " INTEGER, "
                + STAGE16_18 + " INTEGER, "
                + STAGE18_20 + " INTEGER, "
                + STAGE20_22 + " INTEGER, "
                + STAGE22_0 + " INTEGER, "
                + " PRIMARY KEY (" + ID_FIELD +"));";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE1_NAME);
        onCreate(db);
    }

    public screenEvent addItem(screenEvent event) {
        Log.d(TAG, "addItem");

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TYPE_FIELD, event.getType());
        values.put(TIME_YMD_FILED, event.getTime_ymd());
        values.put(TIME_HMS_FILED, event.getTime_hms());
        values.put(TIME_IN_SECONDS_FILED, event.getSeconds());
        values.put(DUR_FIELD, event.getDuration());
        values.put(VALID_FILED, event.getValid().toString());
        values.put(KB_LOCK_FILED, event.getKb_locked().toString());

        event.setId(db.insert(TABLE1_NAME, null, values));
        db.close();

        return event;
    }

    public List<screenEvent> getAllEvents() {
        List<screenEvent> events = new ArrayList<screenEvent>();

        String selectQuery = "SELECT * FROM " + TABLE1_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        while(cursor.moveToNext()) {
            screenEvent event = new screenEvent();

            event.setId(cursor.getInt(0));
            event.setType(cursor.getString(1));
            event.setTime_ymd(cursor.getString(2));
            event.setTime_hms(cursor.getString(3));
            event.setSeconds(cursor.getInt(4));
            event.setDuration(cursor.getInt(5));
            event.setValid(Boolean.parseBoolean(cursor.getString(6)));
            event.setKb_locked(Boolean.parseBoolean(cursor.getString(7)));

            events.add(event);
        }

        return events;
    }

    public void deleteEvents(){
        Log.d(TAG, "deleteEvents");

        onUpgrade(this.getReadableDatabase(), 1, 1);
    }

    public screenEvent queryEvent(screenEvent event) {
        String selectQuery = "SELECT "
                + ID_FIELD + ", "
                + TIME_YMD_FILED + ", "
                + TIME_HMS_FILED + ", "
                + DUR_FIELD + ", "
                + TIME_IN_SECONDS_FILED
                + " FROM " + TABLE1_NAME + " WHERE "
                + TIME_YMD_FILED + " = \"" + event.getTime_ymd()
                + "\" AND "
                + TIME_HMS_FILED + " = \"" + event.getTime_hms() + "\"";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        try {
            Log.d(TAG, "SQL: " + selectQuery);
            cursor = db.rawQuery(selectQuery, null);
        } catch (android.database.sqlite.SQLiteException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "cursor: " + cursor);
        screenEvent result = null;

        if (cursor.getCount() == 1) {
            Log.d(TAG, "one match.");

            cursor.moveToFirst();
            result = new screenEvent();

            result.setId(cursor.getInt(0));
            result.setTime_ymd(cursor.getString(1));
            result.setTime_hms(cursor.getString(2));
            result.setDuration(cursor.getInt(3));
            result.setSeconds(cursor.getInt(4));

        } else {
            Log.e(TAG, cursor.getCount() + " event matches.");
        }

        return result;
    }

    public void updateEvent(screenEvent event) {
        Log.d(TAG, "updateEvent.");
        String selectQuery = "UPDATE " + TABLE1_NAME + " SET "
                + DUR_FIELD + " = " + event.getDuration() + ", "
                + TIME_IN_SECONDS_FILED + " = " + event.getSeconds() + " "
                + "WHERE "
                + ID_FIELD + " = " + event.getId();

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(selectQuery);
    }

    public StageItem QueryStageItem(String date) {
        StageItem stageitem = null;

        String selectQuery = "SELECT "
                + ID_FIELD + ", "
                + TIME_YMD_FILED + ", "
                + STAGE0_2 + ", "
                + STAGE2_4 + ", "
                + STAGE4_6 + ", "
                + STAGE6_8 + ", "
                + STAGE8_10 + ", "
                + STAGE10_12 + ", "
                + STAGE12_14 + ", "
                + STAGE14_16 + ", "
                + STAGE16_18 + ", "
                + STAGE18_20 + ", "
                + STAGE20_22 + ", "
                + STAGE22_0 + " "
                + " FROM " + TABLE2_NAME + " WHERE "
                + TIME_YMD_FILED + " = \"" + date + "\"";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() != 1) {
            Log.e(TAG, "Can not find " + date + " in table2");
        } else {
            int i = 0;
            stageitem = new StageItem();
            cursor.moveToFirst();
            stageitem.setId(cursor.getInt(0));
            stageitem.setTime_ymd(cursor.getString(1));
            while(i < ShareConst.STAGECOUNT) {
                stageitem.addValue(cursor.getLong(i+2));
                i++;
            }
        }

        return stageitem;
    }

    public List<StageItem> getAllStages() {
        List<StageItem> stageitems = new ArrayList<StageItem>();

        String selectQuery = "SELECT * FROM " + TABLE2_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        while(cursor.moveToNext()) {
            StageItem stageitem = new StageItem();
            int i = 0;
            stageitem = new StageItem();
            stageitem.setId(cursor.getInt(0));
            stageitem.setTime_ymd(cursor.getString(1));
            while(i < ShareConst.STAGECOUNT) {
                stageitem.addValue(cursor.getLong(i+2));
                i++;
            }
            stageitems.add(stageitem);
        }

        return stageitems;
    }

    public StageItem addStageItem(StageItem item) {
        Log.d(TAG, "addStageItem");

        if (item.getCount() != ShareConst.STAGECOUNT) {
            Log.e(TAG, "please fill StageItem.");
            return null;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TIME_YMD_FILED, item.getTime_ymd());
        int i = 0;
        while (i < ShareConst.STAGECOUNT) {
            values.put(stageNameIndex[i], item.getStage(i));
            i++;
        }

        item.setId(db.insert(TABLE2_NAME, null, values));
        db.close();

        return item;
    }

    public void updateStageItem(StageItem item) {
        Log.d(TAG, "updateStageItem.");

        int i = 0;
        String tr = new String();
        while (i < (ShareConst.STAGECOUNT-1)) {
            tr = tr + stageNameIndex[i] + " = " + item.getStage(i) + ", ";
            i++;
        }

        tr = tr + stageNameIndex[i] + " = " + item.getStage(i);

        String selectQuery = "UPDATE " + TABLE2_NAME + " SET "
                + tr + " WHERE "
                + ID_FIELD + " = " + item.getId();
        Log.d(TAG, "sql cmd: " + selectQuery);

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(selectQuery);
    }
}
