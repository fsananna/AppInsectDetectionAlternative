package com.example.insectdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.CountryViewHolder> {

    private List<CountriesQuery.Country> countries;

    public CountryAdapter(List<CountriesQuery.Country> countries) {
        this.countries = countries;
    }

    @Override
    public CountryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_country, parent, false);
        return new CountryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CountryViewHolder holder, int position) {
        CountriesQuery.Country country = countries.get(position);
        holder.countryName.setText(country.name());
        holder.countryCapital.setText(country.capital() != null ? country.capital() : "N/A");
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }

    public class CountryViewHolder extends RecyclerView.ViewHolder {

        TextView countryName, countryCapital;

        public CountryViewHolder(View itemView) {
            super(itemView);
            countryName = itemView.findViewById(R.id.country_name);
            countryCapital = itemView.findViewById(R.id.country_capital);
        }
    }
}


