package com.example.android.udacitysunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForecastFragment extends Fragment {
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    String forecastJsonStr = null;
    String[] weatherData = new String[10];
    String baseForecastUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    ArrayAdapter<String> forecastAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
            weatherTask.execute(location);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] weatherData = new String[]{
                "Today-Sunny-88/63",
                "Tomorrow-Foggy-80/60",
                "Wed-Rain-100/10",
                "Today-Sunny-88/63",
                "Tomorrow-Foggy-80/60",
                "Wed-Rain-100/10",
                "Today-Sunny-88/63",
                "Tomorrow-Foggy-80/60"

        };

        List<String> weatherDataList = new ArrayList<String>(Arrays.asList(weatherData));
        forecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weatherDataList);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = forecastAdapter.getItem(position);
                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);


            }
        });


        new FetchWeatherTask().execute("94043");

        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            forecastAdapter.clear();
            for (String s : result) {
                forecastAdapter.add(s);
            }
        }

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(baseForecastUrl).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();
                Log.v(LOG_TAG, builtUri.toString());
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Forecast JSON String: " + forecastJsonStr);

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            WeatherDataParser weatherDataParser = new WeatherDataParser();

            try {
                weatherData = weatherDataParser.getWeatherDataFromJson(forecastJsonStr, numDays);
                System.out.println("weatherData = " + weatherData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weatherData;
        }
    }
}
