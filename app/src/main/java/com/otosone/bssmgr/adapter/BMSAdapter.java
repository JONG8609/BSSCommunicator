package com.otosone.bssmgr.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.otosone.bssmgr.listItem.BMSItem;
import com.otosone.bssmgr.R;
import com.otosone.bssmgr.utils.InputFilterMinMax;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
        }
        String modifiedId = String.format("%02d", Integer.parseInt(bmsItem.getId()) + 1);

        TextView bms1_tv = convertView.findViewById(R.id.bms1_tv);
        TextView bms2_et = convertView.findViewById(R.id.bms2_et);
        TextView bms_spinner_tv = convertView.findViewById(R.id.bms_spinner_tv);

        bms1_tv.setText(modifiedId);
        int newValue = bmsItem.getValue() / 10;
        bms2_et.setText(String.valueOf(newValue));
        bms2_et.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});

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

        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.bms_custom_dialog, null);

        Spinner dialogSpinner = dialogView.findViewById(R.id.spinner);  // Update with correct ID
        EditText valueEditText = dialogView.findViewById(R.id.value);   // Update with correct ID
        TextView titleId = dialogView.findViewById(R.id.title_id);

        // Set TextView
        titleId.setText(getItem(position).getId());

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.spinner_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogSpinner.setAdapter(adapter);
        dialogSpinner.setSelection(getItem(position).getCmd());

        // EditText setup
        int roundedValue = Math.round((float)getItem(position).getValue() / 10);
        valueEditText.setText(String.valueOf(roundedValue) + "%");
        valueEditText.setSelection(valueEditText.getText().length() - 1); // Set cursor position before "%"
        valueEditText.setEnabled(dialogSpinner.getSelectedItemPosition() == 0);

        valueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().endsWith("%")) {
                    valueEditText.removeTextChangedListener(this); // remove to prevent stackOverflow
                    valueEditText.setText(s.toString() + "%");
                    valueEditText.setSelection(s.toString().length());  // set cursor position
                    valueEditText.addTextChangedListener(this); // add it back
                }
            }
        });

        dialogSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                valueEditText.setEnabled(position == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        AlertDialog dialog = builder.setView(dialogView).create();

        Button okButton = dialogView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BMSItem bmsItem = getItem(position);
                int selectedValue = dialogSpinner.getSelectedItemPosition();
                bmsItem.setCmd(selectedValue);

                if (valueEditText.isEnabled()) {
                    int newValue;
                    try {
                        String valueText = valueEditText.getText().toString().replace("%", "");
                        newValue = Integer.parseInt(valueText);
                        if (newValue >= 0 && newValue <= 100) {
                            bmsItem.setValue(newValue * 10);
                        } else {
                            Toast.makeText(context, "Value should be between 0 and 100", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid value entered", Toast.LENGTH_SHORT).show();
                    }
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
