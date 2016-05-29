package com.pengdl.phonecontime;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.Duration;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.LineChartView;

public class LineChartActivity extends AppCompatActivity {

    private final static String TAG = "PCT_LCRT";

    private String date;
    private static long[] Durations = new long[ShareConst.STAGECOUNT];
    private StageItem item;
    private DatabaseManager dbMgr;
    private static long MaxScan = 24*60;

    private void getDisDate(){
        Intent intent = getIntent();
        date = intent.getStringExtra("date");
    }

    public void getDuration() {
        int i = 0;

        dbMgr = new DatabaseManager(this);
        item = dbMgr.QueryStageItem(date);
        if (item != null) {
            while(i<ShareConst.STAGECOUNT) {
                Durations[i] = item.getStage(i)/(60);  // seconds --> minutes
                i++;
            }
        } else {
            while(i < ShareConst.STAGECOUNT) {
                Durations[i] = 0;
                i++;
            }
        }

        i = 0;
        long temp = Durations[0];
        while(i < ShareConst.STAGECOUNT) {
            if (Durations[i] != 0) {
                temp = Durations[i];
            } else {
                Durations[i] = temp;
            }
            i++;
        }

        i = 0;
        MaxScan = Durations[0];
        while(i < ShareConst.STAGECOUNT) {
            if (Durations[i] > MaxScan) {
                MaxScan = Durations[i];
            }
            i++;
        }

        MaxScan = (MaxScan + 10) / 10 * 10;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart);

        getDisDate();
        getDuration();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    /**
     * A fragment containing a line chart.
     */
    public static class PlaceholderFragment extends Fragment {

        private LineChartView chart;
        private LineChartData data;
        private int numberOfPoints = ShareConst.STAGECOUNT;

        private boolean hasLabelForSelected = false;

        public final static String[] times = new String[]{"2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24"};

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_line_chart, container, false);

            chart = (LineChartView) rootView.findViewById(R.id.chart);
            chart.setOnValueTouchListener(new ValueTouchListener());

            generateData();

            // Disable viewport recalculations, see toggleCubic() method for more info.
            chart.setViewportCalculationEnabled(false);

            resetViewport();

            return rootView;
        }

        // MENU
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.line_chart, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_reset) {
                reset();
                generateData();
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        private void reset() {
            hasLabelForSelected = false;
            chart.setValueSelectionEnabled(hasLabelForSelected);
            resetViewport();
        }

        private void resetViewport() {
            // Reset viewport height range to (0,100)
            final Viewport v = new Viewport(chart.getMaximumViewport());
            v.bottom = 0;
            v.top = MaxScan;
            v.left = 0;
            v.right = numberOfPoints - 1;
            chart.setMaximumViewport(v);
            chart.setCurrentViewport(v);
        }

        private void generateData() {

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<PointValue> values = new ArrayList<PointValue>();

            for (int j = 0; j < numberOfPoints; ++j) {
                values.add(new PointValue(j, Durations[j]));
                axisValues.add(new AxisValue(j).setLabel(times[j]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_GREEN).setCubic(true);
            line.setCubic(false);

            List<Line> lines = new ArrayList<Line>();
            lines.add(line);

            data = new LineChartData(lines);

            Axis axisX = new Axis(axisValues).setHasLines(true);
            Axis axisY = new Axis().setHasLines(true).setMaxLabelChars(3);
            axisX.setName("时间/小时");
            axisY.setName("时长/分钟");
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);

            data.setBaseValue(Float.NEGATIVE_INFINITY);
            chart.setLineChartData(data);
        }

        private class ValueTouchListener implements LineChartOnValueSelectListener {

            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
                Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {
                // TODO Auto-generated method stub

            }

        }
    }
}
