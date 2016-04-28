package com.pengdl.phonecontime;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.LineChartView;

public class LineChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart);
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
        private int numberOfLines = 1;
        private int maxNumberOfLines = 4;
        private int numberOfPoints = 12;

        float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];

        private boolean hasAxes = true;
        private boolean hasAxesNames = true;
        private boolean hasLabels = false;
        private boolean hasLabelForSelected = false;
        private boolean pointsHaveDifferentColor;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_line_chart, container, false);

            chart = (LineChartView) rootView.findViewById(R.id.chart);
            chart.setOnValueTouchListener(new ValueTouchListener());

            // Generate some random values.
            generateValues();

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

            if (id == R.id.action_toggle_labels) {
                toggleLabels();
                return true;
            }
            if (id == R.id.action_animate) {
                prepareDataAnimation();
                chart.startDataAnimation();
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        private void generateValues() {
            for (int i = 0; i < maxNumberOfLines; ++i) {
                for (int j = 0; j < numberOfPoints; ++j) {
                    randomNumbersTab[i][j] = (float) Math.random() * 100f;
                }
            }
        }

        private void reset() {
            numberOfLines = 1;

            hasAxes = true;
            hasAxesNames = true;
            hasLabels = false;
            hasLabelForSelected = false;
            pointsHaveDifferentColor = false;

            chart.setValueSelectionEnabled(hasLabelForSelected);
            resetViewport();
        }

        private void resetViewport() {
            // Reset viewport height range to (0,100)
            final Viewport v = new Viewport(chart.getMaximumViewport());
            v.bottom = 0;
            v.top = 100;
            v.left = 0;
            v.right = numberOfPoints - 1;
            chart.setMaximumViewport(v);
            chart.setCurrentViewport(v);
        }

        private void generateData() {

            List<Line> lines = new ArrayList<Line>();
            for (int i = 0; i < numberOfLines; ++i) {

                List<PointValue> values = new ArrayList<PointValue>();
                for (int j = 0; j < numberOfPoints; ++j) {
                    values.add(new PointValue(j, randomNumbersTab[i][j]));
                }

                Line line = new Line(values);
                line.setColor(ChartUtils.COLORS[i]);
                line.setShape(ValueShape.CIRCLE);
                line.setCubic(true);
                line.setFilled(false);
                line.setHasLabels(false);
                line.setHasLabelsOnlyForSelected(hasLabelForSelected);
                line.setHasLines(true);
                line.setHasPoints(true);
                if (pointsHaveDifferentColor){
                    line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
                }
                lines.add(line);
            }

            data = new LineChartData(lines);

            if (hasAxes) {
                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);
                if (hasAxesNames) {
                    axisX.setName("Axis X");
                    axisY.setName("Axis Y");
                }
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }

            data.setBaseValue(Float.NEGATIVE_INFINITY);
            chart.setLineChartData(data);

        }

        /**
         * Adds lines to data, after that data should be set again with
         * {@link LineChartView#setLineChartData(LineChartData)}. Last 4th line has non-monotonically x values.
         */
        private void addLineToData() {
            if (data.getLines().size() >= maxNumberOfLines) {
                Toast.makeText(getActivity(), "Samples app uses max 4 lines!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                ++numberOfLines;
            }

            generateData();
        }

        private void toggleLabels() {
            hasLabels = !hasLabels;

            if (hasLabels) {
                hasLabelForSelected = false;
                chart.setValueSelectionEnabled(hasLabelForSelected);
            }

            generateData();
        }

         /**
         * To animate values you have to change targets values and then call {@link Chart#startDataAnimation()}
         * method(don't confuse with View.animate()). If you operate on data that was set before you don't have to call
         * {@link LineChartView#setLineChartData(LineChartData)} again.
         */
        private void prepareDataAnimation() {
            for (Line line : data.getLines()) {
                for (PointValue value : line.getValues()) {
                    // Here I modify target only for Y values but it is OK to modify X targets as well.
                    value.setTarget(value.getX(), (float) Math.random() * 100);
                }
            }
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
