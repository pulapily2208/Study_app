package com.example.study_app.ui.Deadline.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.study_app.R;

public class IconAdapter extends BaseAdapter {

    Context context;
    int[] icons;
    LayoutInflater inflater;

    public IconAdapter(Context context, int[] icons) {
        this.context = context;
        this.icons = icons;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return icons.length;
    }

    @Override
    public Object getItem(int i) {
        return icons[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        View v = inflater.inflate(R.layout.deadline_icon_item, parent, false);
        ImageView img = v.findViewById(R.id.imgIcon);
        img.setImageResource(icons[i]);
        return v;
    }
}

