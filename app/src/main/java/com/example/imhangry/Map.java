package com.example.imhangry;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //Assign varibale
        spType = findViewById(R.id.sp_type);
        btFind = findViewById(R.id.btn_find);
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
                    //when sucess
                    if ((location != null)) {

                        //when location is not equal to null
                        //get current lattitude
                        currentLat = location.getLatitude();

                        //get current longitude
                        currentLong = location.getLongitude();

                        //sync map
                        supportMapFragment.getMapAsync(new OnMapReadyCallback(){
                            @Override
                            public  void onMapReady(GoogleMap googleMap){
                                //whe map is ready
                                map=googleMap;
                                //zoom current location on map
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(currentLat,currentLong),10
                                ));
                            }

                        });

                    }

                }
            });

        }}

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
                //app marker on map
                map.addMarker(options);

            }
        }
    }
    public class JsonParser {
        private HashMap<String, String> parserJsonObject(JSONObject object) {
            //initialize hash map
            HashMap<String, String> dataList = new HashMap<>();

            try {
                //get name from object
                String name = object.getString("name");
                //get latitude from object
                String latitude = object.getJSONObject("geometry")
                        .getJSONObject("location").getString("lat");
                //get longitude from object

                String longitude = object.getJSONObject("geometry")
                        .getJSONObject("location").getString("lng");
                //put all value in hash map
                dataList.put("name", name);
                dataList.put("lat", latitude);
                dataList.put("lng", longitude);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return dataList;
        }
        private List<HashMap<String,String>> parseJsonArray(JSONArray jsonArray){
            //initialize hesh map list
            List<HashMap<String,String>> datalist = new ArrayList<>();
            for (int i=0; i<jsonArray.length();i++){
                try {
                    //initialize hash map
                    HashMap<String,String> data = parserJsonObject((JSONObject) jsonArray.get(i));
//add data in hash map list
                    datalist.add(data);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
            //return hash map list
            return  datalist;
        }
        public  List<HashMap<String,String>> parseResult(JSONObject object){
            //initialize json array
            JSONArray jsonArray = null;
            //get result array

            try {
                jsonArray= object.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return parseJsonArray(jsonArray);
        }
    }
}