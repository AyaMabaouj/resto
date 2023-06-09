package com.example.imhangry;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.audiofx.EnvironmentalReverb;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Spinner;

import com.example.imhangry.databinding.ActivityHomePageBinding;
import com.example.imhangry.databinding.ActivityMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.Manifest;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Map extends AppCompatActivity {

    //initialize variable
    Spinner spType;
    Button btFind;
    SupportMapFragment supportMapFragment;
    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    double currentLat = 0, currentLong = 0;
    private int GPS_REQUEST_CODE = 9001;
    private ActivityMapBinding binding;
    private boolean  isTrafficEnable;
    private Location location;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Assign varibale
        spType = findViewById(R.id.sp_type);
        btFind = findViewById(R.id.btn_find);
        FloatingActionButton fab = findViewById(R.id.cLo);
        ColorStateList csl = ColorStateList.valueOf(getResources().getColor(R.color.orange));
        fab.setImageTintList(csl);
        FloatingActionButton fb = findViewById(R.id.btnMapType);
        ColorStateList cs = ColorStateList.valueOf(getResources().getColor(R.color.orange));
        fb.setImageTintList(cs);
        binding.btnMapType.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.getMenuInflater().inflate(R.menu.map_type_menu, popupMenu.getMenu());


            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.btnNormal:
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;

                    case R.id.btnSatellite:
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;

                    case R.id.btnTerrain:
                        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                }
                return true;
            });

            popupMenu.show();
        });
        binding.enableTraffic.setOnClickListener(view -> {

            if (isTrafficEnable) {
                if (map != null) {
                    map.setTrafficEnabled(false);
                    isTrafficEnable = false;
                }
            } else {
                if (map != null) {
                    map.setTrafficEnabled(true);
                    isTrafficEnable = true;
                }
            }

        });


        binding.cLo.setOnClickListener(location -> getCurrentLocation());
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //initialize array of place type
        String[] placeTypeList = {"restaurant", "cafe"};

        //initialize array of place name
        String[] placeNameList = {"Restaurant", "Cafe"};


        //set adapter on spnner
        spType.setAdapter(new ArrayAdapter<>(Map.this,
                android.R.layout.simple_spinner_dropdown_item, placeNameList));


        //Initialize Fused Location Provider Client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //check permission
        if (ActivityCompat.checkSelfPermission(Map.this
                , Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            //when permission granted
            //call method
            getCurrentLocation();
        }else{
            //when permission refused
            //request permission

            ActivityCompat.requestPermissions(Map.this
                    , new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get selected position of spinner
                int i = spType.getSelectedItemPosition();
                //initialize url
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + currentLat + " , " + currentLong + //location lat lng
                        "&radius=5000" + //nearby radius
                        "&types=" + placeTypeList[i] + //place type
                        "&sensor=true" + //sensor
                        "&key=" + getResources().getString(R.string.map_key);

                //execute place task method to download json data
                new PlaceTask().execute(url);

            }
        });
    }


    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //initialize task location
            Task<Location> task = fusedLocationProviderClient.getLastLocation();

            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //when success
                    if (location != null) {
                        //when location is not null
                        //get current latitude
                        currentLat = location.getLatitude();
                        //get current longitude
                        currentLong = location.getLongitude();

                        //sync map
                        if (isGPSenable()) {
                            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                                @Override
                                public void onMapReady(GoogleMap googleMap) {
                                    //when map is ready
                                    map = googleMap;
                                    //zoom current location on map
                                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(currentLat, currentLong), 12));

                                    //reverse geocode to get the name of the current location
                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(currentLat, currentLong, 1);
                                        if (addresses != null && addresses.size() > 0) {
                                            Address address = addresses.get(0);
                                            String locationName = address.getAddressLine(0);
                                            //add location name to the map as a marker
                                            map.addMarker(new MarkerOptions()
                                                    .position(new LatLng(currentLat, currentLong))
                                                    .title(locationName).
                                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                };





            });

        }}
public boolean isGPSenable(){
    LocationManager locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);
    boolean providerEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    if(providerEnable){
        return true;
    }else {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("GPS permession")
                .setMessage("GPS is required for this app to work. please enable GPS")
                .setPositiveButton("yes",((dialogInterface,i)->{
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent,GPS_REQUEST_CODE);
                })).setCancelable(false).show();

    }
        return false;

}
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //when permission granted
                //call method
                getCurrentLocation();
            }
        }
    }

    private class PlaceTask extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... strings) {
            String data=null;
            try {
                //intialize data
                data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            //execute parser task
            new ParserTask().execute(s);
        }
    }

    private String downloadUrl(String string) throws IOException {
        //intialize url
        URL url = new URL(string);
        //intialize connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //connect connection
        connection.connect();
        //intialize input stream
        InputStream stream = connection.getInputStream();
        //intialize buffer reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        //intialize strinf buildre
        StringBuilder builder = new StringBuilder();

        //intialize string variable
        String line ="";
        while ((line = reader.readLine()) != null) {
            //append line
            builder.append(line);

        }
        //get append adata
        String data = builder.toString();
        //close reader
        reader.close();
        //return data
        return  data;
    }


    private class ParserTask extends  AsyncTask<String,Integer, List<HashMap<String,String>>>{
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            //create json parser class
            JsonParser jsonParser = new JsonParser();
            //initialize hash map list
            List<HashMap<String,String>> mapList=null;
            JSONObject object = null;
            try {
                //initialize json object
                object = new JSONObject(strings[0]);
                //parse json object
                mapList = jsonParser.parseResult(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //return map list
            return mapList;
        }


        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            //clear map
            map.clear();
            //use for loop
            for (int i=0;i<hashMaps.size();i++){
                //initialize hash map
                HashMap<String,String> hashMapList = hashMaps.get(i);

                //get latitude
                double lat =Double.parseDouble(hashMapList.get("lat"));

                //get longitude
                double lng =Double.parseDouble(hashMapList.get("lng"));

                //get name
                String name = hashMapList.get("name");
                //concat latitude and longitude
                LatLng latLng = new LatLng(lat,lng);
                //initialize marker options
                MarkerOptions options = new MarkerOptions();
                //set position
                options.position(latLng);
                //set title
                options.title(name);
               options.icon(getCustomIcon());
                //app marker on map
                map.addMarker(options);

            }
        }
    }
    private BitmapDescriptor getCustomIcon() {
        Drawable background = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_location_on_24);
        background.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.orge), android.graphics.PorterDuff.Mode.SRC_IN);
        int width = (int) (background.getIntrinsicWidth() * 1.5); // multiplier la largeur par un facteur d'échelle
        int height = (int) (background.getIntrinsicHeight() * 1.5); // multiplier la hauteur par un facteur d'échelle
        background.setBounds(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GPS_REQUEST_CODE){
            LocationManager locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean providerEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(providerEnable){
            Toast.makeText(this,"GPS is Enable", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,"GPS is not Enable", Toast.LENGTH_LONG).show();

        }
        }
    }
}