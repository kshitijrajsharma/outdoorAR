package krs.ar.outar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import krs.ar.outar.helper.LocationHelper;
import krs.ar.outar.model.ARPoint;


public class AROverlayView extends View {

    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    public List<ARPoint> arPoints = new ArrayList<>();
    float value = 200;

    public AROverlayView(Context context) {
        super(context);
        this.context = context;
//        arPoints = new ArrayList<ARPoint>() {{
//            String[] name;
//            name = new String[]{"football ground", "goal post", "hostelgate", "girlshostel", "Newparking", "Civil Building", "RCC", "Electronics&Computer", "Mechanical Dep", "Canteen"};
//            double[] lat = {28.2544, 28.2536, 28.2532, 28.2544, 28.2554, 28.254, 28.2552, 28.2546, 28.2543, 28.2538};
//            double[] lon = {83.9752, 83.9759, 83.9758, 83.9783, 83.9772, 83.9766, 83.9766, 83.9764, 83.9774, 83.9777};
//            double[] alt = {909.832, 906.697, 906.846, 918.702, 916.00, 915.144, 915.104, 914.078, 913.271, 913.902};
//            for (int i = 0; i < name.length; i++) {
//                add(new ARPoint(name[i], lat[i], lon[i], alt[i]));
//            }
//        }};
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }

        final int radius = 15;


        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint newpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint drawpaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        drawpaint.setAntiAlias(true);
        drawpaint.setStrokeWidth(6f);
        drawpaint.setColor(Color.BLACK);
        drawpaint.setStyle(Paint.Style.STROKE);
        drawpaint.setStrokeJoin(Paint.Join.ROUND);

        paint.setStyle(Paint.Style.FILL);
        newpaint.setColor(Color.CYAN);
        newpaint.setTextSize(60);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(35);

        for (int i = 0; i < arPoints.size(); i++) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            float dist = currentLocation.distanceTo(arPoints.get(i).getLocation());
            String distance = "" + dist + "m";
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
//            ARActivity obj= new ARActivity();
//            float value= Integer.parseInt(obj.buffervalue);

//            float value1=Float.valueOf(ET.getText().toString());
//            if(value1>10){
//                value=value1;
//            }
            if (dist < value) {
                if (cameraCoordinateVector[2] < 0) {
                    float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                    float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();

                    canvas.drawCircle(x, y, radius, paint);
                    canvas.drawText(arPoints.get(i).getName(), x - (30 * arPoints.get(i).getName().length() / 2), y - 80, paint);
                    canvas.drawText(distance, x - 90, y + 80, paint);


//                    canvas.drawText("You have Arrived " ,canvas.getWidth()/4, canvas.getHeight(), newpaint);
                }
            }
//            if(dist<50){
//                canvas.drawLine(currentLocation.getLatitude(),currentLocation.getLongitude(),arPoints.get(i).getLocation().getLatitude(), arPoints.get(i).getLocation().getLongitude(), paint);
//            }

            double error = currentLocation.getLatitude() - arPoints.get(i).getLocation().getLatitude();
//            float starty=(float )currentLocation.getLatitude();
//            float startx=(float )currentLocation.getLongitude();
//            float endy=(float) arPoints.get(1).getLocation().getLatitude();
//            float endx=(float)arPoints.get(1).getLocation().getLongitude();

            float obj = (currentLocation).bearingTo(arPoints.get(1).getLocation());
//            canvas.drawLine(startx,starty,endx,endy, newpaint);
            canvas.drawText("i am from " + obj, canvas.getWidth() / 4, canvas.getHeight() - 50, newpaint);
            if (error < 0) {
                error = -error;
            }
            if (error < 0.00001) {
//                paint.setColor(Color.BLUE);
                canvas.drawText("You have Arrived at " + arPoints.get(i).getName(), canvas.getWidth() / 4, canvas.getHeight() - 50, newpaint);
            }

        }
    }

    public void loadWithDate(ArrayList<ARPoint> response) {
        arPoints.clear();
        arPoints.addAll(response);
        Log.i("SceneForm", response.size() + " count");
    }

    public void updateBufferValue(float buffervalue) {
        this.value = buffervalue;
    }
}
