package com.plant.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //تعريف المتغيرات
    private RelativeLayout home;
    private ProgressBar loading;
    private TextView cityName,temp,condation;
    private TextInputEditText cityEdit;
    private ImageView back,icon,search;
    private RecyclerView recyclerViewWeather;
    private ArrayList<WeatherModel> weatherModels;
    private WeatherAdapter weatherAdapter;
    private LocationManager locationManager;
    private int P_code=1;
    private String CityName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        //تهيئة المتغيرات وربطها مع العناصر بملف ال xml
         home = findViewById(R.id.RLHome);
         loading = findViewById(R.id.PBLoading);
         cityName = findViewById(R.id.cityName);
         temp = findViewById(R.id.Temp);
         condation = findViewById(R.id.Condition);;
         cityEdit = findViewById(R.id.editCity);
         back = findViewById(R.id.Back);
         icon = findViewById(R.id.Icon);
         search = findViewById(R.id.Search);
         recyclerViewWeather = findViewById(R.id.recyclerViewWeather);
         weatherModels=new ArrayList<>();
         weatherAdapter=new WeatherAdapter(this, weatherModels);
         recyclerViewWeather.setAdapter(weatherAdapter);

         locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
             ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},P_code);
         }
         Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        CityName = getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(CityName);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city= cityEdit.getText().toString();
                if(city.isEmpty())
                    cityEdit.setError("يجب ادخال اسم مدينتك");
                else{
                    cityName.setText(CityName);
                    getWeatherInfo(city);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==P_code){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "تم الحصول على الاذونات ..", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "يُرجى السماح لنا بالوصول لموقعك", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName="لا توجد";
        Geocoder gcd=new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses=gcd.getFromLocation(longitude, latitude, 10);
            for(Address address:addresses){
                if(address!=null){
                    String city=address.getLocality();
                    if(city!=null && !city.equals("")){
                        cityName=city;
                    }else{
                        Log.d("TAG", "لم نجد المدينة");
                        Toast.makeText(this, "لم يتم ايجاد مدينتك", Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cityName;
    }
    private void getWeatherInfo(String cityname){
        String url="http://api.weatherapi.com/v1/forecast.json?key=2ad016a30bf74711a84212912231907&q="+cityname+"&days=1&aqi=yes&alerts=yes";
        cityName.setText(cityname);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loading.setVisibility(View.GONE);
                home.setVisibility(View.VISIBLE);
                weatherModels.clear();
                try {
                    String temp2=response.getJSONObject("current").getString("temp_c");
                    temp.setText(temp2+"  ℃");
                    int isDay=response.getJSONObject("current").getInt("is_day");
                    String conditionText=response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon=response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    String conditionCode=response.getJSONObject("current").getJSONObject("condition").getString("code");
                    Picasso.get().load("http:".concat(conditionIcon)).into(icon);
                    condation.setText(conditionText);
                    if(isDay==1){
                        Picasso.get().load("https://i.pinimg.com/564x/a4/b1/9d/a4b19ddfb466d9f8aba21493af093ed5.jpg").into(back);
                    }else{
                        Picasso.get().load("https://i.pinimg.com/564x/16/d0/cc/16d0cca6ab893c58427005ad4e4018cd.jpg").into(back);
                   }
                    JSONObject forcastObject=response.getJSONObject("forecast");
                    JSONObject forcastDay = forcastObject.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray=forcastDay.getJSONArray("hour");
                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObject= hourArray.getJSONObject(i);
                        String time=hourObject.getString("time");
                        String temper=hourObject.getString("temp_c");
                        String img=hourObject.getJSONObject("condition").getString("icon");
                        String wind=hourObject.getString("wind_kph");
                        weatherModels.add(new WeatherModel(time, temper, img, wind));
                    }
                    weatherAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getBaseContext(), "الرجاء ادخال اسم مدينة صالح", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}