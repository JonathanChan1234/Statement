package com.jonathan.statement;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class SettingAdapter extends ArrayAdapter<SettingItem> implements View.OnClickListener {
    private ArrayList<SettingItem> dataSet;
    Context context;

    private static class ViewHolder {
        TextView name;
        ImageView icon;
    }

    public SettingAdapter(ArrayList<SettingItem> data, Context context) {
        super(context, R.layout.setting_row_layout, data);
        this.dataSet = data;
        this.context = context;
    }

    @Override
    public void onClick(View v) {

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SettingItem settingItem = getItem(position);
        ViewHolder viewHolder;
        final View result;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.setting_row_layout, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.itemName);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.itemIcon);
            result = convertView;
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }
        viewHolder.name.setText(settingItem.getName());
        viewHolder.icon.setImageResource(settingItem.icon_uri);
        return convertView;
    }
}
