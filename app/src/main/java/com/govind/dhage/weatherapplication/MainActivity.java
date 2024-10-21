package com.govind.dhage.weatherapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.govind.dhage.weatherapplication.adapter.WeatherAdapter;
import com.govind.dhage.weatherapplication.databinding.ActivityMainBinding;
import com.govind.dhage.weatherapplication.model.WeatherModel;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {
    private ImageView weatherIcon;
    private ArrayList<WeatherModel> weatherModelArrayList;
    private WeatherAdapter weatherAdapter;
    LocationManager locationManager;
    ActivityMainBinding mainBinding;
    private static final int RC_LOCATION_PERM = 124;
    private static final long MIN_TIME_BW_UPDATES = 200;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 2.5f;
    String cityName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        checkLocationPermissionsAndServices();
        getCurrentAddress();
        mainBinding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = mainBinding.edtCityName.getText().toString().trim();
                if (!cityName.isEmpty()) {

                    mainBinding.tvCityName.setText(cityName);

                    // Fetch weather info for the entered city
                    getWeatherInfo(cityName);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });



        weatherModelArrayList = new ArrayList<>();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        getWeatherInfo(cityName);
    }
    private void getWeatherInfo(String cityName) {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=c93b5489521e498b9e751153241810&q=" + cityName + "&days=1&aqi=no&alerts=no";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                weatherModelArrayList.clear();
                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    mainBinding.tvTemp.setText(temperature+"°c");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(mainBinding.idIVIcon);


                    if (weatherIcon != null) {
                        Picasso.get().load("http:".concat(conditionIcon)).into(weatherIcon);
                    } else {
                        //Toast.makeText(MainActivity.this, "Error: weatherIcon is null", Toast.LENGTH_SHORT).show();
                    }

                    mainBinding.tvCondition.setText(condition);

                    if (isDay == 1) {
                        // Morning image loading
                        // Picasso.get().load("https://media.istockphoto.com/id/151694830/photo/sun-above-the-horizon.jpg?s=1024x1024&w=is&k=20&c=lHMzhw4awd8ZtsWAo101cXLbqvWT1R0JavtxSLdaQkA=").into(mainBinding.imgBackground); // Ensure this is your background ImageView
                        Picasso.get().load(R.drawable.morning).into(mainBinding.imgBackground); // Ensure this is your background ImageView
                    } else {
                        // Night image loading
                        Picasso.get().load(R.drawable.nningt).into(mainBinding.imgBackground); // Ensure this is your background ImageView
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastDay = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastDay.getJSONArray("hour");

                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");

                        weatherModelArrayList.add(new WeatherModel(time, temper, img, wind));
                    }
                    weatherAdapter = new WeatherAdapter(MainActivity.this, weatherModelArrayList);
                    mainBinding.rvWeather.setAdapter(weatherAdapter);
                    weatherAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
    private void locationAskPermissionTask() {
        if (hasLocationPermission()) {
            Toast.makeText(this, "Location Permission Already Granted", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location),
                    RC_LOCATION_PERM,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void checkLocationPermissionsAndServices() {
        if (hasLocationPermission()) {
            Toast.makeText(this, "Permission Granted and GPS Enabled", Toast.LENGTH_SHORT).show();

        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location),
                    RC_LOCATION_PERM,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private boolean hasLocationPermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == RC_LOCATION_PERM) {
            Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show();
            checkLocationPermissionsAndServices(); // Check if GPS is enabled after permission is granted
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        Log.d("TAG", "onRationaleAccepted:" + requestCode);
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        Log.d("TAG", "onRationaleDenied:" + requestCode);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Toast.makeText(this, "Returned from app settings", Toast.LENGTH_LONG).show();
        }
    }
    public void getCurrentAddress() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            // Request location updates (ensure the permission is granted before this)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnownLocation != null) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    cityName = address.getLocality();
                    mainBinding.tvCityName.setText(cityName);
                    Toast.makeText(this, cityName, Toast.LENGTH_SHORT).show();
                }
            } else {
               // Toast.makeText(this, "Unable ", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Permission not granted ", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error getting address ", Toast.LENGTH_SHORT).show();
        }
    }
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            // Handle location update
            String lat= String.valueOf(location.getLatitude());
            String lng=String.valueOf(location.getLongitude());
           // Toast.makeText(MainActivity.this, lat+lng, Toast.LENGTH_SHORT).show();
        }
    };





}