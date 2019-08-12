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


public class fetchData extends AsyncTask<Void,Void,Void> {
    public static String data ="";
    public  static String name;
    public static String lat;
    public static String lon;
    public static String alt;
    public static String dataParsed = "";
    String singleParsed ="";
    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL("https://api.myjson.com/bins/6dc4d");
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

                name= JO.getString(("name"));
                lat= JO.getString(("lat"));
                lon=JO.getString("long");
                alt=JO.getString("country");

//                singleParsed =  "Name:" + JO.get("name") + "\n"+
//                        "Password:" + JO.get("password") + "\n"+
//                        "Contact:" + JO.get("contact") + "\n"+
//                        "Country:" + JO.get("country") + "\n";

//                dataParsed = dataParsed + singleParsed +"\n" ;


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
        appendData(name);
        ARActivity.data.setText(this.name);


    }
}
