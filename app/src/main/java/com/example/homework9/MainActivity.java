package com.example.homework9;

//Khaled Mohamed Ali

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String url = "https://www.theappsdr.com/map/route";
    private final OkHttpClient client = new OkHttpClient();
    ArrayList<Location> locations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getCoor();
    }

    public ArrayList<LatLng> LatLngLoc = new ArrayList<>();
    public double maxLat = 0;
    public double minLat = 0;
    public double maxLng = 0;
    public double minLng = 0;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        double maxLa = -90;
        double maxLn = -180;
        double minLa = 90;
        double minLn = 0;

        ArrayList<LatLng> LatLngLoc = new ArrayList<>();
        for (Location location: locations){
            LatLng loc = new LatLng(Double.parseDouble(location.latitude), Double.parseDouble(location.longitude));
            LatLngLoc.add(loc);
            if(Double.parseDouble(location.latitude) > maxLa){
                maxLa = Double.parseDouble(location.latitude);
            }
            if(Double.parseDouble(location.latitude) < minLa){
                minLa = Double.parseDouble(location.latitude);
            }

            if(Double.parseDouble(location.longitude) > maxLn){
                maxLn = Double.parseDouble(location.longitude);
            }
            if(Double.parseDouble(location.longitude) < minLn){
                minLn = Double.parseDouble(location.longitude);
            }
        }

        //Log.d("demo", "onMapReady Upper LEft: Lat:"+maxLa+"Long:"+maxLn);
        //Log.d("demo", "onMapReady Bottom Right: Lat:"+minLa+"Long:"+minLn);

        LatLng upperRight = new LatLng(maxLa, minLn);
        LatLng bottomLeft = new LatLng(minLa, maxLn);

        LatLngBounds bound = LatLngBounds.builder()
                .include(upperRight)
                .include(bottomLeft)
                .build();

        for(int i = 1; i < LatLngLoc.size(); i++){
            bound.including(LatLngLoc.get(i));
        }

        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions().clickable(true).addAll(LatLngLoc));//


        Marker mark = googleMap.addMarker(new MarkerOptions()
                .position(LatLngLoc.get(1))
                .title("Beginning"));

        Marker mark2 = googleMap.addMarker(new MarkerOptions()
                .position(LatLngLoc.get(LatLngLoc.size()-1))
                .title("End"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bound, 10));

    }

    public void getCoor(){
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(MainActivity.this, "There was an issue retrieving the data.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d("demo", "onResponse: Get is successful");
                    ResponseBody responseBody = response.body();

                    Gson gson = new Gson();

                    LocationSet locationSet = gson.fromJson(responseBody.charStream(), LocationSet.class);

                    locations = locationSet.path;

                    Log.d("demo", "onResponse: "+locations.get(0));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setCall();
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "There was an issue retrieving the data.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setCall(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
}