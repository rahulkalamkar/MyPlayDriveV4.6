package com.lukedeighton.wheelview.adapter;

import java.util.List;

import android.content.Context;

public abstract class WheelArrayAdapter<T> implements WheelAdapter {
    private List<T> mItems;

    public WheelArrayAdapter(List<T> items,Context context) {
        mItems = items;
    }

    public T getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }
}
