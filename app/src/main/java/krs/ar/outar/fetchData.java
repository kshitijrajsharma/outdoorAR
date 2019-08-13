package krs.ar.outar;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import krs.ar.outar.model.ARPoint;
import krs.ar.outar.model.PointOfInterest;


public class fetchData extends AsyncTask<Void,Void,Void> {
//    ArrayList<PointOfInterest> pointOfInterests = new ArrayList<>();
//    PointOfInterest pointOfInterest = null;
    public static String data ="";
    public static String id;
    public  static String name;
    public static Double lat;
    public static Double lon;
    public static Double alt;
    public static String dataParsed = "";
    String singleParsed ="";
    @Override
    protected Void doInBackground(Void... voids) {

        try {
            URL url = new URL("https://api.myjson.com/bins/vnkq3");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while(line != null){
                line = bufferedReader.readLine();
                data = data + line;
            }

            JSONArray JA = new JSONArray(data);
            for(int i =0 ;i <JA.length(); i++){
                JSONObject JO = (JSONObject) JA.get(i);
//                id=JO.getString("id");
//                name= JO.getString(("name"));
//                lat= JO.getDouble(("lat"));
//                lon=JO.getDouble("long");
//                alt=JO.getDouble("alt");
//                pointOfInterest = new PointOfInterest(name,lat,lon,alt);
//                pointOfInterests.add(pointOfInterest);

                singleParsed =  "ID:" + JO.get("id") + "\n"+
                        "Name:" + JO.get("name") + "\n"+
                        "Lat:" + JO.get("lat") + "\n"+
                        "Lon:" + JO.get("lon") + "\n"+
                        "Altitude:" + JO.get("alt") + "\n";

                dataParsed = dataParsed + singleParsed +"\n" ;


            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
    public void appendData(String text)
    {
//
        File myFile = new File("storage/emulated/0/amap/myfile.json");

        if (!myFile.exists())
        {
            try
            {
                myFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(myFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    protected void onPostExecute(Void aVoid) {

        super.onPostExecute(aVoid);
        appendData(dataParsed);
        ARActivity.data.setText(dataParsed);


    }
}
