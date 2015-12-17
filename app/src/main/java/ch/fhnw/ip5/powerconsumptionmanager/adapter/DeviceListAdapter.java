package ch.fhnw.ip5.powerconsumptionmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ip5.powerconsumptionmanager.R;
import ch.fhnw.ip5.powerconsumptionmanager.util.ChartHelper;
import ch.fhnw.ip5.powerconsumptionmanager.util.PowerConsumptionManagerAppContext;

/**
 * List adapter to manage the device list that is shown below consumption chart
 */
public class DeviceListAdapter extends ArrayAdapter<String> {
    private int mLayout;
    private List<String> mDevices;
    private ChartHelper mConsumptionChart;
    private ArrayList<Integer> removedDataSetIndexes = new ArrayList<>();

    public DeviceListAdapter(Context context, int resource, ArrayList<String> objects, ChartHelper chart) {
        super(context, resource, objects);
        mLayout = resource;
        mDevices = objects;
        mConsumptionChart = chart;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mainViewHolder = null;

        if(convertView == null) {
            if(mConsumptionChart == null && mDevices.size() == 1) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(mLayout, parent, false);

                TextView noDevice = (TextView) convertView.findViewById(R.id.textNoDevice);
                noDevice.setText(mDevices.get(position));
            } else {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(mLayout, parent, false);

                final ViewHolder vh = new ViewHolder();

                vh.textDevice = (TextView) convertView.findViewById(R.id.textDevice);
                vh.textDevice.setText(mDevices.get(position));

                // Set color indicator to see which device is connected to each graph
                vh.graphColor = (View) convertView.findViewById(R.id.graphColor);
                vh.graphColor.setBackgroundColor(mConsumptionChart.getGraphColor(position));

                vh.switchDevice = (Switch) convertView.findViewById(R.id.switchDevice);
                vh.switchDevice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int shiftPos = getShiftPosition(position);
                        if (!vh.switchDevice.isChecked()) {
                            /* Data sets are managed in a list and as soon one device graph is not shown in the chart anymore
                             * the indices are out of sync
                             */
                            mConsumptionChart.getChart().getLineData().removeDataSet(position - shiftPos);
                            mConsumptionChart.displayNoneAnimated();
                            // Add removed data set index to list
                            removedDataSetIndexes.add(position);
                        } else {
                            PowerConsumptionManagerAppContext appContext = (PowerConsumptionManagerAppContext) getContext().getApplicationContext();
                            // Indicate that graph of certain device is visible again
                            removedDataSetIndexes.remove(Integer.valueOf(position));
                            // Generate new data set with the correct consumption data
                            mConsumptionChart.generateDataSet(appContext.getConsumptionData().get(position - shiftPos), position - shiftPos);
                            mConsumptionChart.updateChartData(removedDataSetIndexes);
                            mConsumptionChart.displayNoneAnimated();
                        }
                    }
                });
                convertView.setTag(vh);
            }
        }
        else {
            mainViewHolder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    // Returns amount of not shown device-graphs in the chart that have a smaller index in the list view as the clicked one
    public int getShiftPosition(int position) {
        int shiftPos = 0;
        for(int i = 0; i < removedDataSetIndexes.size(); i++) {
            if(removedDataSetIndexes.get(i) < position) {
                shiftPos++;
            }
        }
        return shiftPos;
    }

    // View holder contains all ui elements on a list item
    static class ViewHolder {
        TextView textDevice;
        View graphColor;
        Switch switchDevice;
    }
}
