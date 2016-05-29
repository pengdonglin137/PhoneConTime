package com.pengdl.phonecontime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.datatype.Duration;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class LineColumnDependencyActivity extends ActionBarActivity {

    private final static String TAG = "PCT_LCDA";
    private DatabaseManager dbMgr;
    private String date;
    private final static int latest_x_days = 10;
    private static StageItem[] items = new StageItem[ShareConst.STAGECOUNT];
    private static long Durations[][] = new long[latest_x_days][ShareConst.STAGECOUNT];
    private static long DayDuration[] = new long[latest_x_days];
    private static long MaxScan[] = new long[ShareConst.STAGECOUNT];
    public final static String[] days = new String[latest_x_days];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_column_dependency);

        getDisDate();
        getDuration();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    private void getDisDate(){
        Intent intent = getIntent();
        date = intent.getStringExtra("date");
    }

    private String new_date(String date, int j) {
        Calendar calendar = Calendar.getInstance();

        int year = Integer.parseInt(date.split("-")[0]);
        int months = Integer.parseInt(date.split("-")[1]);
        int day = Integer.parseInt(date.split("-")[2]);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, months-1);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        calendar.add(Calendar.DAY_OF_YEAR, -1 * (j+1));

        Log.d(TAG, "new date " + DateFormat.format("20yy-MM-dd", calendar).toString());
        return DateFormat.format("20yy-MM-dd", calendar).toString();
    }

    public void getDuration() {
        int j = 0;

        String tmp_date = new String();
        tmp_date = date;

        dbMgr = new DatabaseManager(this);
        while (j < latest_x_days) {
            int i = 0;

            days[latest_x_days - 1 -j] = tmp_date.split("-")[1] + "." + tmp_date.split("-")[2];
            items[j] = dbMgr.QueryStageItem(tmp_date);

            while(i<ShareConst.STAGECOUNT) {
                if (items[j] != null) {
                    Durations[j][i] = items[j].getStage(i)/(60)/10;  // seconds --> minutes
                } else {
                    Durations[j][i] = 0;
                    //Durations[j][i] = i * 30 + j * 30;

                }
                i++;
            }

            i = 0;
            long temp = Durations[j][i];
            while(i < ShareConst.STAGECOUNT) {
                if (Durations[j][i] != 0) {
                    temp = Durations[j][i];
                } else {
                    Durations[j][i] = temp;
                }
                i++;
            }

            i = 0;
            MaxScan[j] = Durations[j][0];
            while(i < ShareConst.STAGECOUNT) {
                if (Durations[j][i] > MaxScan[j]) {
                    MaxScan[j] = Durations[j][i];
                }
                i++;
            }
            MaxScan[j] = (MaxScan[j] + 10) / 10 * 10;

            screenEvent event = new screenEvent();
            event.setTime_hms(ShareConst.MASK);
            event.setTime_ymd(tmp_date);
            event = dbMgr.queryEvent(event);
            if (event != null) {
                DayDuration[j] = event.getDuration();
            } else {
                DayDuration[j] = 0;
                //DayDuration[j] = j * 60;

            }

            tmp_date = new_date(date, j);
            j++;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        public final static String[] times = new String[]{"2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24"};

        private LineChartView chartTop;
        private ColumnChartView chartBottom;

        private LineChartData lineData;
        private ColumnChartData columnData;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_line_column_dependency, container, false);

            // *** TOP LINE CHART ***
            chartTop = (LineChartView) rootView.findViewById(R.id.chart_top);

            // Generate and set data for line chart
            generateInitialLineData();

            // *** BOTTOM COLUMN CHART ***

            chartBottom = (ColumnChartView) rootView.findViewById(R.id.chart_bottom);

            generateColumnData();

            return rootView;
        }

        private void generateColumnData() {

            int numSubcolumns = 1;
            int numColumns = latest_x_days;

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<Column> columns = new ArrayList<Column>();
            List<SubcolumnValue> values;
            for (int i = 0; i < numColumns; ++i) {

                values = new ArrayList<SubcolumnValue>();
                for (int j = 0; j < numSubcolumns; ++j) {
                    Log.d(TAG, "DayDuration: " + DayDuration[i]);
                    values.add(new SubcolumnValue(DayDuration[i], ChartUtils.pickColor()));
                }

                axisValues.add(new AxisValue(i).setLabel(days[i]));
                columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
            }

            columnData = new ColumnChartData(columns);

            columnData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
            columnData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(2));

            chartBottom.setColumnChartData(columnData);

            // Set value touch listener that will trigger changes for chartTop.
            chartBottom.setOnValueTouchListener(new ValueTouchListener());

            // Set selection mode to keep selected month column highlighted.
            chartBottom.setValueSelectionEnabled(true);

            chartBottom.setZoomType(ZoomType.HORIZONTAL);
        }

        /**
         * Generates initial data for line chart. At the begining all Y values are equals 0. That will change when user
         * will select value on column chart.
         */
        private void generateInitialLineData() {
            int numValues = ShareConst.STAGECOUNT;

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<PointValue> values = new ArrayList<PointValue>();
            for (int i = 0; i < numValues; ++i) {
                values.add(new PointValue(i, 0));
                axisValues.add(new AxisValue(i).setLabel(times[i]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_GREEN).setCubic(true);

            List<Line> lines = new ArrayList<Line>();
            lines.add(line);

            lineData = new LineChartData(lines);
            lineData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
            lineData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

            chartTop.setLineChartData(lineData);

            // For build-up animation you have to disable viewport recalculation.
            chartTop.setViewportCalculationEnabled(false);

            // And set initial max viewport and current viewport- remember to set viewports after data.
            Viewport v = new Viewport(0, 24*60/10, ShareConst.STAGECOUNT - 1, 0);
            chartTop.setMaximumViewport(v);
            chartTop.setCurrentViewport(v);

            chartTop.setZoomType(ZoomType.HORIZONTAL);
        }

        private void generateLineData(int color, int index) {
            // Cancel last animation if not finished.
            chartTop.cancelDataAnimation();

            // Modify data targets
            Line line = lineData.getLines().get(0);// For this example there is always only one line.
            line.setColor(color);
            int i = 0;
            for (PointValue value : line.getValues()) {
                // Change target only for Y value.
                value.setTarget(value.getX(), Durations[index][i++]);
            }

            // Start new data animation with 300ms duration;
            Viewport v = new Viewport(0, MaxScan[index], ShareConst.STAGECOUNT - 1, 0);
            chartTop.setMaximumViewport(v);
            chartTop.setCurrentViewport(v);
            chartTop.startDataAnimation(300);
        }

        private class ValueTouchListener implements ColumnChartOnValueSelectListener {

            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
                Log.d(TAG, "columuIndex: " + columnIndex + " subcolumnIndex: " + subcolumnIndex + " value: " + value);
                generateLineData(value.getColor(), columnIndex);
            }

            @Override
            public void onValueDeselected() {
                generateLineData(ChartUtils.COLOR_GREEN, 0);
            }
        }
    }
}
