package com.otosone.bsscommunicator.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.otosone.bsscommunicator.BMSItem;
import com.otosone.bsscommunicator.R;
import com.otosone.bsscommunicator.utils.InputFilterMinMax;

import java.util.List;

public class BMSAdapter extends BaseAdapter {

    private Context context;
    private List<BMSItem> bmsItems;
    private LayoutInflater layoutInflater;

    public BMSAdapter(Context context, List<BMSItem> bmsItems) {
        this.context = context;
        this.bmsItems = bmsItems;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return bmsItems.size();
    }

    @Override
    public BMSItem getItem(int position) {
        return bmsItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_bms, parent, false);
        }

        BMSItem bmsItem = getItem(position);

        CheckBox checkBox = convertView.findViewById(R.id.bms_checkbox);
        TextView bms1_tv = convertView.findViewById(R.id.bms1_tv);
        TextView bms2_et = convertView.findViewById(R.id.bms2_et);
        TextView bms_spinner_tv = convertView.findViewById(R.id.bms_spinner_tv);

        checkBox.setChecked(bmsItem.isChecked());
        bms1_tv.setText(bmsItem.getId());
        bms2_et.setText(String.valueOf(bmsItem.getValue()));

        // Set the default value for bms_spinner_tv
        if (bmsItem.getCmd() == null || bmsItem.getCmd().isEmpty()) {
            bms_spinner_tv.setText("0. Set SoC");
        } else {
            bms_spinner_tv.setText(bmsItem.getCmd());
        }

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

        // Inflate custom layout containing a Spinner and an EditText
        View dialogView = inflater.inflate(R.layout.bms_dialog_spinner, null);
        Spinner dialogSpinner = dialogView.findViewById(R.id.dialog_spinner);
        EditText valueEditText = dialogView.findViewById(R.id.dialog_value_edit_text);
        TextView bmsid_tv = dialogView.findViewById(R.id.bmsid_tv);
        bmsid_tv.setText(getItem(position).getId());
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.spinner_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogSpinner.setAdapter(adapter);

        // Set the selected index of the Spinner based on the current value
        String currentValue = getItem(position).getCmd();
        dialogSpinner.setSelection(adapter.getPosition(currentValue));

        // Set the current value of the EditText
        valueEditText.setText(String.valueOf(getItem(position).getValue()));

        // Enable or disable the EditText based on the Spinner value
        valueEditText.setEnabled("0. Set SoC".equals(currentValue));

        // Update the EditText state when the Spinner value changes
        dialogSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                valueEditText.setEnabled("0. Set SoC".equals(selectedItem));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        builder.setView(dialogView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                BMSItem bmsItem = getItem(position);

                // Set the selected value from the Spinner
                String selectedValue = dialogSpinner.getSelectedItem().toString();
                bmsItem.setCmd(selectedValue);

                // Set the value fromthe EditText, if it is enabled
                if (valueEditText.isEnabled()) {
                    int newValue;
                    try {
                        newValue = Integer.parseInt(valueEditText.getText().toString());
                        if (newValue >= 0 && newValue <= 100) {
                            bmsItem.setValue(newValue);
                        } else {
                            Toast.makeText(context, "Value should be between 0 and 100", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid value entered", Toast.LENGTH_SHORT).show();
                    }
                }
                notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing, just close the dialog
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
