package com.devtwist.medtalk.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.devtwist.medtalk.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

public class AutoSliderAdapter extends SliderViewAdapter<AutoSliderAdapter.SliderAdapterViewHolder> {

    // list for storing urls of images.
    private Context context;
    private int[] imagesList;

    // Constructor


    public AutoSliderAdapter(Context context, int[] imagesList) {
        this.context = context;
        this.imagesList = imagesList;
    }

    // We are inflating the slider_layout
    // inside on Create View Holder method.
    @Override
    public SliderAdapterViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.auto_slider_layout, null);
        return new SliderAdapterViewHolder(inflate);
    }

    // Inside on bind view holder we will
    // set data to item of Slider View.
    @Override
    public void onBindViewHolder(SliderAdapterViewHolder viewHolder, final int position) {


        viewHolder.imageViewBackground.setImageResource(imagesList[position]);

    }

    // this method will return the count of our list...
    @Override
    public int getCount() {
        return imagesList.length;
    }

    static class SliderAdapterViewHolder extends ViewHolder {
        // Adapter class for initializing
        // the views of our slider view.
        View itemView;
        ImageView imageViewBackground;

        public SliderAdapterViewHolder(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id._aSliderImageItem);
            this.itemView = itemView;
        }
    }
}
