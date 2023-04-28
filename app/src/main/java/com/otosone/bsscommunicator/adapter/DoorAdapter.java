package com.otosone.bsscommunicator.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.otosone.bsscommunicator.DoorItem;
import com.otosone.bsscommunicator.R;

import java.util.List;

public class DoorAdapter extends BaseAdapter {

    private Context context;
    private List<DoorItem> doorItems;
    private LayoutInflater layoutInflater;

    public DoorAdapter(Context context, List<DoorItem> doorItems) {
        this.context = context;
        this.doorItems = doorItems;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return doorItems.size();
    }

    @Override
    public DoorItem getItem(int position) {
        return doorItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_door, parent, false);
            holder = new ViewHolder();
            holder.checkBox = convertView.findViewById(R.id.door_checkbox);
            holder.text1 = convertView.findViewById(R.id.door1_tv);
            holder.text2 = convertView.findViewById(R.id.door2_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DoorItem doorItem = doorItems.get(position);
        holder.checkBox.setChecked(doorItem.isChecked());
        holder.text1.setText(doorItem.getText1());
        holder.text2.setText(doorItem.getText2());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToggleDialog(position);
            }
        });

        return convertView;
    }

    private void showToggleDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Inflate custom title view
        LayoutInflater inflater = LayoutInflater.from(context);
        View titleView = inflater.inflate(R.layout.door_dialog_title, null);
        TextView titleId = titleView.findViewById(R.id.title_id);
        titleId.setText("ID: " + getItem(position).getText1());
        builder.setCustomTitle(titleView);

        String[] choices = {"LOCK", "UNLOCK"};
        builder.setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DoorItem doorItem = doorItems.get(position);

                if (which == 0) {
                    doorItem.setText2("LOCK");
                } else {
                    doorItem.setText2("UNLOCK");
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static class ViewHolder {
        CheckBox checkBox;
        TextView text1;
        TextView text2;
    }
}
