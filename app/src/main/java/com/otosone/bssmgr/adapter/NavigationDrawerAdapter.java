package com.otosone.bssmgr.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.otosone.bssmgr.R;
import com.otosone.bssmgr.listItem.NavigationDrawerItem;

public class NavigationDrawerAdapter extends ArrayAdapter<NavigationDrawerItem> {
    private final Context context;
    private final NavigationDrawerItem[] values;

    public NavigationDrawerAdapter(Context context, NavigationDrawerItem[] values) {
        super(context, 0, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public boolean isEnabled(int position) {
        return !(values[position].isHeader() || values[position] instanceof NavigationDrawerItem.NavigationDivider);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        NavigationDrawerItem currentItem = values[position];

        if (currentItem instanceof NavigationDrawerItem.NavigationHeader) {
            NavigationDrawerItem.NavigationHeader header = (NavigationDrawerItem.NavigationHeader) currentItem;
            if (header.isLarge()) {
                row = inflater.inflate(R.layout.header_row_large, parent, false);
            } else {
                row = inflater.inflate(R.layout.header_row_small, parent, false);
            }

            if (header.isImageHeader()) {
                ImageView imageView = row.findViewById(R.id.image); // replace with your image view id
                imageView.setImageResource(header.getImageId());
            } else {
                TextView textView = row.findViewById(R.id.header);
                textView.setText(header.getTitle());
            }

            row.setBackgroundColor(Color.TRANSPARENT);
        } else if (currentItem instanceof NavigationDrawerItem.NavigationItem) {
            row = inflater.inflate(R.layout.item_row, parent, false);
            TextView textView = row.findViewById(R.id.label);
            ImageView imageView = row.findViewById(R.id.icon);
            textView.setText(((NavigationDrawerItem.NavigationItem) currentItem).getTitle());
            imageView.setImageResource(((NavigationDrawerItem.NavigationItem) currentItem).getImageId());

            // Set the background color to change on state change
            row.setBackgroundResource(R.drawable.item_background_selector);

            ColorStateList imageColorStateList = ContextCompat.getColorStateList(context, R.color.image_tint_selector);
            ImageViewCompat.setImageTintList(imageView, imageColorStateList);
        } else if (currentItem instanceof NavigationDrawerItem.NavigationDivider) {
            row = inflater.inflate(R.layout.divider_row, parent, false);
        }

        return row;
    }
}
