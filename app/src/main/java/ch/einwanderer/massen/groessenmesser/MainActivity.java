package ch.einwanderer.massen.groessenmesser;

import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Camera mCamera;
    private CameraPreview mPreview;
    private TextView mWinkel;

    private final float[] magneticFieldData = new float[3];
    private final float[] accelerationData = new float[3];

    private double currentRotation;
    private double lowerAngel;
    private boolean hasClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        getSupportActionBar().hide();

        mWinkel = (TextView) findViewById(R.id.winkel);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        findViewById(R.id.layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActivityClick();
            }
        });
    }

    private void onActivityClick() {

        if(!hasClicked) {
            hasClicked = true;
            lowerAngel = currentRotation;
            Toast.makeText(this, "Messen Sie jetzt den anderen Winkel.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, FormActivity.class);
            boolean reversed = lowerAngel > currentRotation;
            intent.putExtra("lower", reversed?currentRotation:lowerAngel);
            intent.putExtra("upper", reversed?lowerAngel:currentRotation);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.release();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerationData, 0, 3);
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticFieldData, 0, 3);
        }

        currentRotation = getCurrentRotationValue();
        mWinkel.setText("Winkel: " + String.format("%.1f", currentRotation));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private double getCurrentRotationValue() {
        float[] rotationMatrix = new float[16];

        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerationData, magneticFieldData)) {

            float[] orientation = new float[4];
            SensorManager.getOrientation(rotationMatrix, orientation);

            double neigung = Math.toDegrees(orientation[2]);

            return Math.abs(neigung);
        }

        return 0;
    }
}
