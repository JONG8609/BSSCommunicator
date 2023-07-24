package com.otosone.bssmgr.listAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.otosone.bssmgr.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Bms2Adapter extends BaseAdapter {

    private Context mContext;
    private List<JSONObject> mDataList;

    public Bms2Adapter(Context context, List<JSONObject> dataList) {
        mContext = context;

        Collections.sort(dataList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                try {
                    int index1 = o1.getInt("index");
                    int index2 = o2.getInt("index");
                    return Integer.compare(index1, index2);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        mDataList = dataList;
    }

    @Override
    public int getCount() {
        // Adding 1 to account for the title row
        return mDataList.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        // Adjusting the index to account for the title row
        if (position == 0) {
            return null;
        } else {
            return mDataList.get(position - 1);
        }
    }

    @Override
    public long getItemId(int position) {
        // The ID of the item is its position
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (position == 0) { // For the title
            convertView = inflater.inflate(R.layout.title_list_bms2, parent, false); // Your custom layout for the title row.
        } else { // For the content
            if (convertView == null || (convertView.getTag() != null && convertView.getTag().equals("TITLE"))) {
                convertView = inflater.inflate(R.layout.status_list_bms2, parent, false);
                convertView.setTag("CONTENT");
            }

            TextView indexTextView = convertView.findViewById(R.id.index_text);
            TextView cell1TextView = convertView.findViewById(R.id.cell_1_text);
            TextView cell2TextView = convertView.findViewById(R.id.cell_2_text);
            TextView cell3TextView = convertView.findViewById(R.id.cell_3_text);
            TextView cell4TextView = convertView.findViewById(R.id.cell_4_text);
            TextView cell5TextView = convertView.findViewById(R.id.cell_5_text);
            TextView cell6TextView = convertView.findViewById(R.id.cell_6_text);
            TextView cell7TextView = convertView.findViewById(R.id.cell_7_text);
            TextView cell8TextView = convertView.findViewById(R.id.cell_8_text);
            TextView cell9TextView = convertView.findViewById(R.id.cell_9_text);
            TextView cell10TextView = convertView.findViewById(R.id.cell_10_text);


            JSONObject dataItem = mDataList.get(position - 1);

            try {
                indexTextView.setText(String.format("%02d", dataItem.getInt("index")+1));

                // Check if "cell_v" array exists in the dataItem
                if (dataItem.has("cell_v") && !dataItem.isNull("cell_v")) {
                    JSONArray cellVArray = dataItem.getJSONArray("cell_v");

                    // Access values from the cellVArray, format them, and set to respective TextViews
                    cell1TextView.setText(formatCellValue(cellVArray.getInt(0)));
                    cell2TextView.setText(formatCellValue(cellVArray.getInt(1)));
                    cell3TextView.setText(formatCellValue(cellVArray.getInt(2)));
                    cell4TextView.setText(formatCellValue(cellVArray.getInt(3)));
                    cell5TextView.setText(formatCellValue(cellVArray.getInt(4)));
                    cell6TextView.setText(formatCellValue(cellVArray.getInt(5)));
                    cell7TextView.setText(formatCellValue(cellVArray.getInt(6)));
                    cell8TextView.setText(formatCellValue(cellVArray.getInt(7)));
                    cell9TextView.setText(formatCellValue(cellVArray.getInt(8)));
                    cell10TextView.setText(formatCellValue(cellVArray.getInt(9)));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }



        }

        return convertView;
    }

    private String formatCellValue(int value) {
        float floatValue = value / 1000f; // Convert to float and shift decimal
        floatValue = Math.round(floatValue * 100) / 100f; // Round to two decimal places
        return String.format("%.2f", floatValue); // Format as string with two decimal places
    }

}
