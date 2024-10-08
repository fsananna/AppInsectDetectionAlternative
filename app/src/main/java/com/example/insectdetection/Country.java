package com.example.insectdetection;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;


import java.util.List;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class Country extends AppCompatActivity {

    private RecyclerView countryRecyclerView;
    private CountryAdapter countryAdapter;
    private ApolloClient apolloClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country);

        countryRecyclerView = findViewById(R.id.country_recycler_view);
        countryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Apollo Client
        apolloClient = ApolloClient.builder()
                .serverUrl("https://countries.trevorblades.com/graphql")
                .build();

        // Fetch country data
        fetchCountryData();
    }

    private void fetchCountryData() {
        CountriesQuery countriesQuery = CountriesQuery.builder().build();

        // Use RxJava to handle the Apollo query asynchronously
        Rx2Apollo.from(apolloClient.query(countriesQuery))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.Observer<Response<CountriesQuery.Data>>() {
                    @Override
                    public void onSubscribe(io.reactivex.disposables.Disposable d) {
                        // Handle subscription if needed
                    }

                    @Override
                    public void onNext(Response<CountriesQuery.Data> response) {
                        if (response.hasErrors()) {
                            Log.e("Country", "GraphQL Errors: " + response.errors());
                        } else {
                            // Extract country data from the response
                            List<CountriesQuery.Country> countries = response.getData().countries();
                            updateCountryUI(countries);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Country", "Error fetching countries", e);
                    }

                    @Override
                    public void onComplete() {
                        // Called when the query completes successfully
                    }
                });
    }

    private void updateCountryUI(List<CountriesQuery.Country> countries) {
        // Set up the RecyclerView adapter with the fetched data
        countryAdapter = new CountryAdapter(countries);
        countryRecyclerView.setAdapter(countryAdapter);
    }
}
