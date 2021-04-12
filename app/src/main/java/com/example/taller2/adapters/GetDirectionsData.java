package com.example.taller2.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetDirectionsData extends AsyncTask<Object,String,String> {
    GoogleMap mMap;
    String url;
    LatLng start, end;

    HttpURLConnection httpURLConnection = null;
    String data = "";
    InputStream inputStream = null;

    Context c;

    public GetDirectionsData(Context c){
        this.c = c;
    }

    @Override
    protected String doInBackground(Object... params){

        mMap = (GoogleMap)params[0];
        url = (String)params[1];
        start = (LatLng)params[2];
        end = (LatLng)params[3];

        try{
            URL myurl = new URL(url);
            httpURLConnection = (HttpURLConnection)myurl.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while((line = bufferedReader.readLine()) != null)
            {
                sb.append(line);
            }
            data = sb.toString();
            bufferedReader.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    protected void onPostExecute(String s) {

        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).
                    getJSONArray("legs").getJSONObject(0).
                    getJSONArray("steps");

            int count = jsonArray.length();
            String[] polyline_array = new String[count];

            JSONObject jsonObject1;

            for(int i = 0; i < count; i++)
            {
                jsonObject1 = jsonArray.getJSONObject(i);
                String polygone = jsonObject1.getJSONObject("polyline").getString("points");
                polyline_array[i] = polygone;
            }

            int count2 = polyline_array.length;
            for(int i = 0; i < count2; i++)
            {
                PolylineOptions options2 = new PolylineOptions();
                options2.color(Color.BLUE);
                options2.width(10);
                options2.addAll(PolyUtil.decode(polyline_array[i]));

                mMap.addPolyline(options2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        super.onPostExecute(s);
    }
}
