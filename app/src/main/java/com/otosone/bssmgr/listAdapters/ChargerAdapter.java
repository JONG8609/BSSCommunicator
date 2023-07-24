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

public class ChargerAdapter extends BaseAdapter {

    private Context mContext;
    private List<JSONObject> mDataList;

    public ChargerAdapter(Context context, List<JSONObject> dataList) {
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
            convertView = inflater.inflate(R.layout.title_list_charger, parent, false); // Your custom layout for the title row.
        } else { // For the content
            if (convertView == null || (convertView.getTag() != null && convertView.getTag().equals("TITLE"))) {
                convertView = inflater.inflate(R.layout.status_list_charger, parent, false);
                convertView.setTag("CONTENT");
            }

            TextView indexTextView = convertView.findViewById(R.id.index_text);
            TextView thermometerTextView = convertView.findViewById(R.id.thermometer_text);
            TextView voltTextView = convertView.findViewById(R.id.volt_text);
            TextView amperTextView = convertView.findViewById(R.id.amper_text);
            TextView statusTextView = convertView.findViewById(R.id.status_text);

            JSONObject dataItem = mDataList.get(position - 1);

            try {
                indexTextView.setText(String.format("%02d", dataItem.getInt("index")+1));
                thermometerTextView.setText(dataItem.getString("temp"));
                voltTextView.setText(dataItem.getString("volt"));
                amperTextView.setText(dataItem.getString("amper"));

                int chargingStatus = dataItem.getInt("charging");
                String statusText;
                switch (chargingStatus) {
                    case 0:
                        statusText = "IDLE";
                        break;
                    case 1:
                        statusText = "PRE_CHARGE";
                        break;
                    case 2:
                        statusText = "NORMAL_CHARGE";
                        break;
                    case 3:
                        statusText = "POST_CHARGE";
                        break;
                    case 4:
                        statusText = "COMPLETE\nCHARGE";
                        break;
                    case 5:
                        statusText = "NONE";
                        break;
                    case 6:
                        statusText = "ERROR";
                        break;
                    default:
                        statusText = "UNKNOWN\nSTATUS";
                }
                statusTextView.setText(statusText);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return convertView;
    }
}
