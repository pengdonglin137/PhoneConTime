package com.pengdl.phonecontime;

import android.content.ContentValues;
import android.content.Context;
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

    private final static String TAG = "OTP_DM";

    public final static String TABLE_NAME = "OTPB";
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
    public final static String ID_PREV_FILED = "_id_prev";
    public final static String ID_NEXT_FILED = "_id_next";

    public DatabaseManager(Context context) {
        super(context, "otp_db", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");

        String sql = "CREATE TABLE " + TABLE_NAME
                + " (" + ID_FIELD + " INTEGER, "
                + TYPE_FIELD + " TEXT, "
                + TIME_YMD_FILED + " TEXT, "
                + TIME_HMS_FILED + " TEXT, "
                + TIME_IN_SECONDS_FILED + " INTEGER, "
                + DUR_FIELD + " INTEGER, "
                + VALID_FILED + " TEXT, "
                + KB_LOCK_FILED + " TEXT, "
                + ID_PREV_FILED + " INTEGER, "
                + ID_NEXT_FILED + " INTEGER, "
                + " PRIMARY KEY (" + ID_FIELD +"));";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
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
        values.put(ID_PREV_FILED, event.getId_prev());
        values.put(ID_NEXT_FILED, event.getId_next());

        event.setId(db.insert(TABLE_NAME, null, values));
        db.close();

        return event;
    }

    public List<screenEvent> getAllEvents() {
        List<screenEvent> events = new ArrayList<screenEvent>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME;

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
            event.setId_prev(cursor.getInt(8));
            event.setId_next(cursor.getInt(9));

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
                + DUR_FIELD
                + " FROM " + TABLE_NAME + " WHERE "
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

        } else {
            Log.e(TAG, cursor.getCount() + " event matches.");
        }

        return result;
    }

    public void updateEvent(screenEvent event) {
        Log.d(TAG, "updateEvent.");
        String selectQuery = "UPDATE " + TABLE_NAME + " SET "
                + DUR_FIELD + " = " + event.getDuration() + " "
                + "WHERE "
                + ID_FIELD + " = " + event.getId();

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(selectQuery);
    }
}
