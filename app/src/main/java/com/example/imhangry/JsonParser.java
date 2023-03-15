package com.example.imhangry;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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