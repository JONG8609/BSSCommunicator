package com.otosone.bssmgr.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.otosone.bssmgr.listItem.DoorItem;
import com.otosone.bssmgr.R;

import java.util.List;

public class DoorAdapter extends BaseAdapter {

    private Context context;
    private List<DoorItem> doorItems;
    private LayoutInflater layoutInflater;

    public DoorAdapter(Context context, List<DoorItem> doorItems) {
        this.context = context;
        this.doorItems = doorItems;
        this.layoutInflater = LayoutInflater.from(context);
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
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_door, parent, false);
        }

        DoorItem doorItem = getItem(position);

        CheckBox checkBox = convertView.findViewById(R.id.door_checkbox);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
        }
        TextView door1_tv = convertView.findViewById(R.id.door1_tv);
        TextView door2_tv = convertView.findViewById(R.id.door2_tv);

        checkBox.setChecked(doorItem.isChecked());
        door1_tv.setText(doorItem.getId());
        door2_tv.setText(doorItem.getDoorStatus());

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doorItem.setChecked(!doorItem.isChecked());
            }
        });

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

        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.door_dialog_title, null);
        TextView titleId = dialogView.findViewById(R.id.title_id);
        titleId.setText(getItem(position).getId());

        String[] choices = {"LOCK", "UNLOCK"};
        int[] selectedIndex = new int[]{getItem(position).getDoorStatus().equals("LOCK") ? 0 : 1};

        ListView listView = dialogView.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.my_list_item_single_choice, choices);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(selectedIndex[0], true);
        listView.setOnItemClickListener((parent, view, position1, id) -> selectedIndex[0] = position1);

        AlertDialog dialog = builder.setView(dialogView).create();

        Button okButton = dialogView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoorItem doorItem = getItem(position);

                if (selectedIndex[0] == 0) {
                    doorItem.setDoorStatus("LOCK");
                } else {
                    doorItem.setDoorStatus("UNLOCK");
                }

                notifyDataSetChanged();
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
