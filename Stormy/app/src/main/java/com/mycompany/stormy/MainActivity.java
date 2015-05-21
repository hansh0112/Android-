package com.mycompany.stormy;

import android.app.DownloadManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private CurrentWeather mCurrentWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String apikey = "a9220788bc3c6589a303ba95e829e371";
        double latitude = 37.8267;
        double longtitude = -122.423;
        String forecastUrl = "https://api.forecast.io/forecast/"+ apikey +
                "/" + latitude + "," + longtitude;
        //if statement before we do anything with okhttp. (we are checking network connectivity)
        if (isNetworkAvailable()) {


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastUrl).build();
            Call call = client.newCall(request);
            //the enqueue method here executes the call, but it does so by putting in a queue.
            //it doesn't execute it right away, immediately. OkHttp has a queue that will execute
            //asynchronous calls in the way are added to the queue. This is the only call in the
            //queue so it pretty much gets executed right away. At this point, the call is executing
            //in the background and we can continue execute the code at main UI thread like normal.

            //callback: asynchronous processing makes use of this because it's the communication bridge
            //between a background worker thread and main thread.
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught", e);
                    }
                    catch(JSONException e){
                        Log.e(TAG, "Exception caught", e);
                    }

                }
            });
        }
        else{
            Toast.makeText(this, getString(R.string.network_unavailable), Toast.LENGTH_LONG).show();
        }
        //the main UI code will pick up from here (d for debug) 
        Log.d(TAG, "Main UI code is running!");

        }
    //use the JSON file to get data that we want
    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currently = forecast.getJSONObject("currently");
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON"+ timezone);

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);

        return currentWeather;
    }

    //checking if the network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        //check if the networkinfo is actually permitted and connected
        if(networkInfo != null && networkInfo.isConnected())
        {
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

}
