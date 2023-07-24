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

public class Bms1Adapter extends BaseAdapter {

    private Context mContext;
    private List<JSONObject> mDataList;

    public Bms1Adapter(Context context, List<JSONObject> dataList) {
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
            convertView = inflater.inflate(R.layout.title_list_bms1, parent, false); // Your custom layout for the title row.
        } else { // For the content
            if (convertView == null || (convertView.getTag() != null && convertView.getTag().equals("TITLE"))) {
                convertView = inflater.inflate(R.layout.status_list_bms1, parent, false);
                convertView.setTag("CONTENT");
            }

            TextView indexTextView = convertView.findViewById(R.id.index_text);
            TextView socTextView = convertView.findViewById(R.id.soc_text);
            TextView packVTextView = convertView.findViewById(R.id.pack_v_text);
            TextView packATextView = convertView.findViewById(R.id.pack_a_text);
            TextView cycleTextView = convertView.findViewById(R.id.cycle_text);
            TextView alarmTextView = convertView.findViewById(R.id.alarm_text);

            JSONObject dataItem = mDataList.get(position - 1);

            try {
                indexTextView.setText(String.format("%02d", dataItem.getInt("index")+1));

                // Divide soc by 10
                int soc = dataItem.getInt("soc");
                socTextView.setText(String.valueOf(soc / 10));

                // Divide packv and packa by 100
                int packV = dataItem.getInt("packv");
                packVTextView.setText(String.format("%.2f", packV / 100.0));

                int packA = dataItem.getInt("packa");
                packATextView.setText(String.format("%.2f", packA / 100.0));

                JSONArray cycleArray = dataItem.getJSONArray("cycle");
                StringBuilder cycleBuilder = new StringBuilder();
                for(int i = 0; i < cycleArray.length(); i++) {
                    if(i > 0) {
                        cycleBuilder.append(",");
                    }
                    cycleBuilder.append(cycleArray.getInt(i));
                }
                cycleTextView.setText(cycleBuilder.toString());

                alarmTextView.setText(dataItem.getString("alarm"));

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        return convertView;
    }

}
