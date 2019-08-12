package krs.ar.outar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class SampleActivity extends Activity {
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        textView = findViewById(R.id.tv);

        getResponse();
    }
    private void getResponse(){
        int cacheSize = 10 * 1024 * 1024; // 10 MB
        Cache cache = new Cache(getCacheDir(), cacheSize);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                .build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ApiInterface.JSONURL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create());

        Retrofit retrofit = builder.build();

//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(ApiInterface.JSONURL)
//                .addConverterFactory(ScalarsConverterFactory.create())
//                .build();

        ApiInterface api = retrofit.create(ApiInterface.class);

        Call<String> call = api.getString();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.i("Response string", response.body().toString());
                //Toast.makeText()
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.i("onSuccess", response.body().toString());

                        String jsonresponse = response.body().toString();
                        writeTv(jsonresponse);

                    } else {
                        Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
//
      private void writeTv(String response){

        try {
            //getting the whole json object from the response
            JSONObject obj = new JSONObject(response);
            if(obj.optString("status").equals("true")){
                ArrayList<RetroModel> retroModelArrayList = new ArrayList<>();
                JSONArray dataArray  = obj.getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {

                    RetroModel retroModel = new RetroModel();
                    JSONObject dataobj = dataArray.getJSONObject(i);

                    retroModel.setid(dataobj.getString("id"));
                    retroModel.setName(dataobj.getString("name"));
                    retroModel.setCountry(dataobj.getString("country"));
                    retroModel.setCity(dataobj.getString("city"));

                    retroModelArrayList.add(retroModel);

                }

                for (int j = 0; j < retroModelArrayList.size(); j++){
                    String  id = retroModelArrayList.get(j).getid();
                    String name= retroModelArrayList.get(j).getName();

                    textView.setText(textView.getText()+ retroModelArrayList.get(j).getid()+ " "+ retroModelArrayList.get(j).getName()
                            + " "+ retroModelArrayList.get(j).getCountry()+ " "+retroModelArrayList.get(j).getCity()+" \n");
                }

            }else {
                Toast.makeText(SampleActivity.this, obj.optString("message")+"", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

//    public static ArrayList<PointOfInterest> getData() throws JSONException {
//        JSONArray jsonArray = new JSONArray(data);
//        ArrayList<PointOfInterest> pointOfInterests = new ArrayList<>();
//        PointOfInterest pointOfInterest = null;
//        for (int i = 0; i < jsonArray.length(); i++) {
//            JSONObject obj = jsonArray.getJSONObject(i);
//            String label = obj.getString("label");
//            double lat = obj.getDouble("lat");
//            double lon = obj.getDouble("lon");
//
//            pointOfInterest = new PointOfInterest(lat, lon, label);
//            pointOfInterests.add(pointOfInterest);
//        }
//
//        return pointOfInterests;
//    }
//
//
//    public static ArrayList<PointOfInterest> getDataNew() throws JSONException {
//        JSONArray jsonArray = new JSONArray(Utils.data);
//        ArrayList<PointOfInterest> pointOfInterests = new ArrayList<>();
//        PointOfInterest pointOfInterest = null;
//        for (int i = 0; i < jsonArray.length(); i++) {
//
//            JSONObject obj = jsonArray.getJSONObject(i);
//            String label = obj.getString("name");
//            double lat = obj.getDouble("lat");
//            double lon = obj.getDouble("long");
//
//            pointOfInterest = new PointOfInterest(lat, lon, label);
//            pointOfInterests.add(pointOfInterest);
//
//        }
//
//        return pointOfInterests;
//
//
//    }

}
