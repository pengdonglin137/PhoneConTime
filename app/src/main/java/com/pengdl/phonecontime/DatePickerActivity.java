package com.pengdl.phonecontime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "PCT_DP";
    private Button Okay, today;
    private DatePicker datepicker;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String datepicker_valid = null;
    private int year;
    private int month;
    private int day;
    private Boolean have_store = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_picker);

        preferences = getSharedPreferences(ShareConst.SHARESP, MODE_PRIVATE);
        editor = preferences.edit();
        datepicker_valid = preferences.getString(ShareConst.DATEPICKER_VALID, null);

        Okay = (Button)findViewById(R.id.OK);
        Okay.setOnClickListener(this);
        today = (Button)findViewById(R.id.Today);
        today.setOnClickListener(this);

        datepicker = (DatePicker)findViewById(R.id.datePicker);
        if (datepicker_valid.equals(ShareConst.VALID)) {
            year = preferences.getInt(ShareConst.DATEPICKER_YEAR, 0);
            month = preferences.getInt(ShareConst.DATEPICKER_MONTH, 0);
            day = preferences.getInt(ShareConst.DATEPICKER_DAY, 0);

            Log.d(TAG, "from preference file, got: " +year + "-" + month + "-" + day);
        }

        if (year == 0 || month == 0 || day == 0) {
            Log.d(TAG, "preference file invalid, reset date.");
            editor.putString(ShareConst.DATEPICKER_VALID, ShareConst.VALID);
            Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        datepicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Log.d(TAG, "onDateChanged.");
                DatePickerActivity.this.year = year;
                DatePickerActivity.this.month = monthOfYear;
                DatePickerActivity.this.day = dayOfMonth;
                DatePickerActivity.this.have_store = false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.OK: {

                String date = year + "-"
                        + ((month+1)>9 ? (month+1)+"" : "0"+(month+1)) + "-"
                        + (day>9 ? day + "" : "0" + day);
                Intent intent = new Intent();
                intent.putExtra("date", date);
                Log.d(TAG, "datePicker: " + date);
                setResult(1001, intent);

                if (!have_store) {
                    have_store = true;

                    editor.putInt(ShareConst.DATEPICKER_YEAR, year);
                    editor.putInt(ShareConst.DATEPICKER_MONTH, month);
                    editor.putInt(ShareConst.DATEPICKER_DAY, day);
                    editor.commit();
                }

                finish();
                break;
            }
            case R.id.Today:
                Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
                datepicker.updateDate(year, month, day);
                break;
            default:
                break;
        }
    }
}
