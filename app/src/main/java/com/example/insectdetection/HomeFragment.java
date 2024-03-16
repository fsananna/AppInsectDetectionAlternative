package com.example.insectdetection;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        ImageButton mapButton = rootView.findViewById(R.id.map_button);
        ImageButton weatherButton = rootView.findViewById(R.id.weather_button);

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapActivity();
            }
        });

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWeatherActivity();
            }
        });

        return rootView;
    }

    private void openMapActivity() {
        Intent intent = new Intent(requireContext(), Map.class);
        startActivity(intent);
    }

    private void openWeatherActivity() {
        Intent intent = new Intent(requireContext(), Weather.class); // Replace WeatherActivity with the actual name of your Weather activity
        startActivity(intent);
    }
}
