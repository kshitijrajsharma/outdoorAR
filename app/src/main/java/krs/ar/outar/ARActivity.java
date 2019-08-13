package krs.ar.outar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import krs.ar.outar.model.ARPoint;

import static android.hardware.SensorManager.*;
import static android.view.Surface.*;
import static android.view.Surface.ROTATION_180;
import static android.view.Surface.ROTATION_270;

public class ARActivity extends AppCompatActivity implements SensorEventListener, LocationListener {


    final static String TAG = "ARActivity";
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView arOverlayView;
    private Camera camera;
    private ARCamera arCamera;
    private TextView tvCurrentLocation;
    private TextView tvBearing;
    private TextView text;
    Button click, update, local;
    public static TextView data;
    public static TextView direction_text;
    public static double lat1, long1, alt1;
    public static String name1;
    private SensorManager sensorManager;
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute

    private LocationManager locationManager;
    public Location location;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;
    private float declination;
    public static String buffervalue = "200";
    public static List<ARPoint> arPoints;
    EditText ET, num;
    fetchData process;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        sayhello();
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = findViewById(R.id.camera_container_layout);
        surfaceView = findViewById(R.id.surface_view);
        tvCurrentLocation = findViewById(R.id.tv_current_location);
        tvBearing = findViewById(R.id.tv_bearing);
//        update = (Button) findViewById(R.id.updatelocation);
        local = (Button) findViewById(R.id.locallist);
        text = (TextView) findViewById(R.id.textView);
        arOverlayView = new AROverlayView(this);
//        arOverlayView.getResponse();
        click = (Button) findViewById(R.id.button);
//        data = (TextView) findViewById(R.id.fetcheddata);
        direction_text = (TextView) findViewById(R.id.direction);
        ET = (EditText) findViewById(R.id.editText);
        num=(EditText) findViewById(R.id.editText2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }


//        View v = getLayoutInflater().inflate(R.layout.activity_ar, null);
//        setContentView(v);
//        AROverlayView nac = new AROverlayView(ARActivity.this, v);
//        nac.test();

        process = new fetchData();
        process.setCompleteListener(new fetchData.onRequestCompleteListener() {
            @Override
            public void onComplete(ArrayList<ARPoint> arPoints) {
                arOverlayView.loadWithDate(arPoints);
            }
        });

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                process.execute();
                Toast.makeText(ARActivity.this, "Download Started !", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        requestCameraPermission();
        requestLocationPermission();
        registerSensors();
        initAROverlayView();
        updateLatestLocation();
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }

    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService();
        }
    }

    public void initAROverlayView() {
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }

    public void initARCameraView() {
        reloadSurfaceView();
        if (arCamera == null) {
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    //    private void initCamera() {
    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);

                Toast.makeText(this, "Camera found", Toast.LENGTH_LONG).show();
                // attempt to get a Camera instance
            } catch (Exception e) {
//                        camera = null;
                //                    camera.release();
                Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }


        cameraContainerLayout.addView(surfaceView);
//        updateLatestLocation();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(this, sensorManager
//                        .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
//                SensorManager.SENSOR_DELAY_UI);
//
//        sensorManager.registerListener(this, sensorManager
//                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


//        updateLatestLocation();
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] rotationMatrix = new float[16];
            getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);
            final int screenRotation = this.getWindowManager().getDefaultDisplay()
                    .getRotation();

            switch (screenRotation) {
                case ROTATION_90:
                    remapCoordinateSystem(rotationMatrixFromVector,
                            AXIS_Y,
                            AXIS_MINUS_X, rotationMatrix);
                    break;
                case ROTATION_270:
                    remapCoordinateSystem(rotationMatrixFromVector,
                            AXIS_MINUS_Y,
                            AXIS_X, rotationMatrix);
                    break;
                case ROTATION_180:
                    remapCoordinateSystem(rotationMatrixFromVector,
                            AXIS_MINUS_X, AXIS_MINUS_Y,
                            rotationMatrix);
                    break;
                default:
                    remapCoordinateSystem(rotationMatrixFromVector,
                            AXIS_X, AXIS_Y,
                            rotationMatrix);
                    break;
            }

            float[] projectionMatrix = arCamera.getProjectionMatrix();
            float[] rotatedProjectionMatrix = new float[16];
            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrix, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);

            //Heading
            float[] orientation = new float[3];
            getOrientation(rotatedProjectionMatrix, orientation);

//            Toast.makeText(ARActivity.this, "Orientation"+orientation[0],   Toast.LENGTH_SHORT).show();
//            double bearing = Math.toDegrees(orientation[0]);
//            double bearing = Math.toDegrees(orientation[0]) + declination;
            double bearing = (int) (Math.toDegrees(SensorManager.getOrientation(rotatedProjectionMatrix, orientation)[0]) + 360) % 360;
//            Toast.makeText(ARActivity.this, "bearing"+bearing,   Toast.LENGTH_SHORT).show();
            tvBearing.setText(String.format("Bearing: %s", bearing));
            double degree = bearing;

            direction_text.setVisibility(View.VISIBLE);

            Log.i(TAG, String.valueOf(degree));

            if (degree == 0 && degree < 45 || degree >= 315
                    && degree == 360) {
                direction_text.setText("You are: Northbound");
            }

            if (degree >= 45 && degree < 90) {
                direction_text.setText("You are: NorthEastbound");
            }

            if (degree >= 90 && degree < 135) {
                direction_text.setText("You are: Eastbound");
            }

            if (degree >= 135 && degree < 180) {
                direction_text.setText("You are: SouthEastbound");
            }

            if (degree >= 180 && degree < 225) {
                direction_text.setText("You are: SouthWestbound");
            }

            if (degree >= 225 && degree < 270) {
                direction_text.setText("You are: Westbound");
            }

            if (degree >= 270 && degree < 315) {
                direction_text.setText("You are: NorthWestbound");

            }

        }
    }

//    showMessage(String.format("%s open spaces found", pointOfInterests.size()));

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.w("DeviceOrientation", "Orientation compass unreliable");
        }
    }

    private void initLocationService() {

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            this.locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


            if (!isNetworkEnabled && !isGPSEnabled) {
                // cannot get location
                this.locationServiceAvailable = false;
            }

            this.locationServiceAvailable = true;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    updateLatestLocation();
                }
            }

            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateLatestLocation();
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());

        }
    }

    private void updateLatestLocation() {
        if (arOverlayView != null && location != null) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            arOverlayView.updateCurrentLocation(location);
            tvCurrentLocation.setText(String.format("Lattitude: %s \nLongitude: %s \nAltitude: %s \nAccuracy: %s m\n",
                    location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy()));


        }
    }

    public void updatelocation(View view) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        updateLatestLocation();
        Toast.makeText(ARActivity.this, "Location Changed !", Toast.LENGTH_SHORT).show();
//        arOverlayView.AROverlay(arPoints);
    }


    @Override
    public void onLocationChanged(Location location) {

        updateLatestLocation();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void fromjson(View view) {
//        Intent intent=new Intent(this, SampleActivity.class);
//        super.startActivity(intent);
        Toast.makeText(this, "Download Started !", Toast.LENGTH_SHORT).show();
    }

    public void localdata(View view) {
        try {
            SurveyDBHelper surveyDBHelper = new SurveyDBHelper(ARActivity.this);
            SQLiteDatabase db = surveyDBHelper.getReadableDatabase();
//            cursor=db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            Cursor cursor = surveyDBHelper.getSurveyData(db);
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String name = cursor.getString(1);
                    String lats = cursor.getString(2);
                    Double lat = Double.parseDouble(lats);
                    String lons = cursor.getString(2);
                    Double lon = Double.parseDouble(lons);
//                    text.setText("");
                    text.append("Name: " + name + "\n");
                    text.append("Lattitude:" + lats + "\n");
                    text.append("Longitude:" + lons + "\n");
                    cursor.moveToNext();
                }

                //Get the data

//                add(new ARPoint(name, lat, lon, 0));

            }
            CursorAdapter ca = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_1,
                    cursor,
                    new String[]{SurveyDBHelper.SURVEY_TABLE_NAME_COLUMN},
                    new int[]{android.R.id.text1},
                    0);
        } catch (SQLiteException e) {
            Log.v(TAG, "Exception ");
        }
    }

    public void setName(String newName) {
        this.buffervalue = newName;
    }
    public   void bufferlocation(View view){
        buffervalue = num.getText().toString();
        if(buffervalue==null){
            Toast.makeText(ARActivity.this, "Please Enter Buffer Radius", Toast.LENGTH_SHORT).show();
        }
        arOverlayView.updateBufferValue(Float.parseFloat(buffervalue));
        Toast.makeText(ARActivity.this, "Buffer radius :"+buffervalue, Toast.LENGTH_SHORT).show();


    }

    public void grablocation(View view) {
        new SurveyDBAsyncTask().execute("");
        Toast.makeText(ARActivity.this, "submitted  successfully", Toast.LENGTH_SHORT).show();

    }


    public void sayhello() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                    update.performClick();
                Toast.makeText(ARActivity.this, "Wait For GPS to fix...", Toast.LENGTH_SHORT).show();
            }
        }, 8000);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.v(TAG, "check for datarefresh:");
//
    }

    private class SurveyDBAsyncTask extends AsyncTask<String, Void, Long> {

        ContentValues cv;

        @Override
        protected void onPreExecute() {
            Log.v(TAG, "Data entered by the user is " + ET.getText() + " " + location.getLatitude() + " " + location.getLongitude());

            cv = new ContentValues();
            cv.put(SurveyDBHelper.SURVEY_TABLE_NAME_COLUMN, ET.getText().toString());
            cv.put(SurveyDBHelper.SURVEY_TABLE_EMAIL_COLUMN, location.getLatitude());
            cv.put(SurveyDBHelper.SURVEY_TABLE_AGE_COLUMN, location.getLongitude());


            super.onPreExecute();
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Long doInBackground(String... params) {
            long id = 0;
            Log.v(TAG, "In doInBackground");
            try {
                SurveyDBHelper surveyDBHelper = new SurveyDBHelper(ARActivity.this);
                SQLiteDatabase db = surveyDBHelper.getReadableDatabase();

                id = db.insert(SurveyDBHelper.SURVEY_TABLE, null, cv);

                db.close();
            } catch (SQLiteException e) {
                Log.v(TAG, "Exception " + e.getMessage());
            }
            return id;
        }

        @Override
        protected void onPostExecute(Long id) {
            super.onPostExecute(id);
            Log.v(TAG, "In onPostExecute and id is " + id);
            Toast.makeText(getApplicationContext(), "Insert Success" + id, Toast.LENGTH_SHORT).show();
        }
    }


}
