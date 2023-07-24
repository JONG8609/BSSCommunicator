package com.otosone.bssmgr.listAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.otosone.bssmgr.R;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatusAdapter extends BaseAdapter {

    private Context mContext;
    private List<JSONObject> mDataList;

    public StatusAdapter(Context context, List<JSONObject> dataList) {
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
            convertView = inflater.inflate(R.layout.title_list_status, parent, false); // Your custom layout for the title row.
        } else { // For the content
            if (convertView == null || convertView.getTag().equals("TITLE")) {
                convertView = inflater.inflate(R.layout.status_list_status, parent, false);
                convertView.setTag("CONTENT");
            }

            TextView indexTextView = convertView.findViewById(R.id.index_text);
            TextView canTextView = convertView.findViewById(R.id.can_text);
            TextView chgTextView = convertView.findViewById(R.id.chg_text);
            TextView bmsTextView = convertView.findViewById(R.id.bms_text);
            TextView batTextView = convertView.findViewById(R.id.bat_text);
            TextView lockTextView = convertView.findViewById(R.id.lock_text);
            TextView doorTextView = convertView.findViewById(R.id.door_text);
            TextView chgStatusTextView = convertView.findViewById(R.id.chg_status_text);

            JSONObject dataItem = mDataList.get(position - 1);

            try {
                int index = dataItem.getInt("index");

                indexTextView.setText(String.format("%02d", index+1));

                canTextView.setText(Integer.toString(dataItem.getInt("CAN")));
                chgTextView.setText(Integer.toString(dataItem.getInt("CHG")));
                bmsTextView.setText(Integer.toString(dataItem.getInt("BMS")));
                batTextView.setText(Integer.toString(dataItem.getInt("BAT")));
                lockTextView.setText(Integer.toString(dataItem.getInt("LOCK")));
                doorTextView.setText(Integer.toString(dataItem.getInt("DOOR")));
                chgStatusTextView.setText(Integer.toString(dataItem.getInt("Status_CHG")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return convertView;
    }

}
