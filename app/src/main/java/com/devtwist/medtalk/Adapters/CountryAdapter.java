package com.devtwist.medtalk.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devtwist.medtalk.Models.CountryCodeData;
import com.devtwist.medtalk.R;

import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.ViewHolder> {
    private Context mContext;
    private List<CountryCodeData> mList;
    private LinearLayout _otpLayout, _cCodeLayout;

    private TextView _otpCountryCode, _otpCountryName;

    public CountryAdapter(Context mContext, List<CountryCodeData> mList, LinearLayout _otpLayout,
                          LinearLayout _cCodeLayout, TextView _otpCountryCode, TextView _otpCountryName) {
        this.mContext = mContext;
        this.mList = mList;
        this._otpLayout = _otpLayout;
        this._cCodeLayout = _cCodeLayout;
        this._otpCountryCode = _otpCountryCode;
        this._otpCountryName = _otpCountryName;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.country_code_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CountryCodeData countryData = mList.get(position);

        holder._countryName.setText(countryData.getCountryName());
        holder._countryCode.setText(countryData.getCountryCode());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _otpCountryCode.setText(countryData.getCountryCode());
                _otpCountryName.setText(countryData.getCountryCode() + "  " + countryData.getCountryName());
                _cCodeLayout.setVisibility(View.GONE);
                _otpLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView _countryCode,_countryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            _countryCode = itemView.findViewById(R.id.cCodeItem);
            _countryName = itemView.findViewById(R.id.cNameItem);
        }
    }
}
