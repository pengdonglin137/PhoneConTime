package com.pengdl.phonecontime;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener  {

    private final static String TAG = "OTP_A";
    private TextView dis;
    private Button get, show, delete;
    private TextView date;
    private MainService.MainBinder binder;
    private Intent intent;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;


    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MainService.MainBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        service_begin_bind();

        preferences_init();

        widget_init();

        Log.d(TAG, "onCreate");
    }

    public void service_begin_bind() {
        intent = new Intent(this, MainService.class);
        intent.setPackage("com.example.pengdl.onthephone");
        startService(intent);

        if (bindService(intent, conn, Service.BIND_AUTO_CREATE)) {
            Log.d(TAG, "Bind server succeed.");
        }
    }

    public void preferences_init() {
        preferences = getSharedPreferences(ShareConst.SHARESP, MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString(ShareConst.DATEPICKER_VALID, ShareConst.INVALID);
        editor.commit();
    }

    public void widget_init() {
        dis = (TextView) findViewById(R.id.dis);
        get = (Button) findViewById(R.id.Get);
        show = (Button) findViewById(R.id.Show);
        delete = (Button) findViewById(R.id.Delete);
        date = (TextView) findViewById(R.id.date);

        get.setOnClickListener(this);
        show.setOnClickListener(this);
        delete.setOnClickListener(this);
        date.setOnClickListener(this);
        date.setOnLongClickListener(this);
        dis.setOnClickListener(this);

        date_init();
    }

    public void date_init() {
        date.setText(ShareConst.GetNowYMD());
    }

    public void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        stopService(intent);
        Log.e(TAG, "onDestroy");
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.Get: {
                String dt = date.getText().toString();
                Boolean flag = ShareConst.GetNowYMD().toString().equals(dt);
                dis.setText(binder.onThePhoneTimeAt(dt, flag));
                break;
            }
            case R.id.Show:
                binder.showEvents();
                break;
            case R.id.Delete:
                binder.deleteEvents();
                break;
            case R.id.date: {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, DatePickerActivity.class);
                startActivityForResult(intent, 1000);
                break;
            }
            case R.id.dis: {
                Intent intent = new Intent();
                intent.setClass(this, LineChartActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult, requestCode: " + requestCode);

        if(requestCode == 1000 && resultCode == 1001)
        {
            String date_value = data.getStringExtra("date");
            Log.d(TAG, "got date: " + date_value);
            date.setText(date_value);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.date: {
                Intent intent = new Intent();
                intent.setClass(this, LineColumnDependencyActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.prompt);
            builder.setMessage(R.string.message);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }
            );

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.show();
        }

        return true;
    }
}
