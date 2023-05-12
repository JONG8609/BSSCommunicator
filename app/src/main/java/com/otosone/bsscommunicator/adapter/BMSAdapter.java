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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.otosone.bsscommunicator.listItem.BMSItem;
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

        bms1_tv.setText(Integer.toString(bmsItem.getId()));
        bms2_et.setText(Integer.toString(bmsItem.getValue()));
        bms2_et.setFilters(new InputFilter[] {new InputFilterMinMax(0, 100)});

        checkBox.setOnCheckedChangeListener(null); // Remove any existing listeners
        checkBox.setChecked(bmsItem.isChecked()); // Set the initial state of the checkbox

        // Add a new OnCheckedChangeListener
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Your logic for handling the checkbox change
            bmsItem.setChecked(isChecked);
        });

        // Set the default value for bms_spinner_tv
        if (bmsItem.getCmd() == -1) {
            bms_spinner_tv.setText("0. Set SoC");
        } else {
            String[] spinnerChoices = context.getResources().getStringArray(R.array.spinner_choices);
            bms_spinner_tv.setText(spinnerChoices[bmsItem.getCmd()]);
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
        valueEditText.setFilters(new InputFilter[] {new InputFilterMinMax(0, 100)});
        TextView bmsid_tv = dialogView.findViewById(R.id.bmsid_tv);
        bmsid_tv.setText(String.valueOf(getItem(position).getId()));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.spinner_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogSpinner.setAdapter(adapter);

        // Set the selected index of the Spinner based on the current value
        int currentValue = getItem(position).getCmd();
        dialogSpinner.setSelection(currentValue);

        // Set the current value of the EditText
        valueEditText.setText(String.valueOf(getItem(position).getValue()));

        // Enable or disable the EditText based on the Spinner value
        valueEditText.setEnabled(currentValue == 0);
        // Update the EditText state when the Spinner value changes
        dialogSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                valueEditText.setEnabled(position == 0);
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
                int selectedValue = dialogSpinner.getSelectedItemPosition();
                bmsItem.setCmd(selectedValue);

                // Set the value from the EditText, if it is enabled
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
