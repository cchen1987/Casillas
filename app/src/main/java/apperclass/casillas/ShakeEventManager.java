package apperclass.casillas;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by chao on 28/10/2016.
 */

public class ShakeEventManager implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor s;

    private static final int MOV_COUNTS = 5;
    private static final int MOV_THRESHOLD = 10;
    private static final float ALPHA = 0.8f;
    private static final int SHAKE_WINDOW_TIME_INTERVAL = 2000; // milliseconds

    // Gravity force on x,y,z axis
    private float gravity[] = new float[3];

    private int counter;
    private long firstMovTime;
    private ShakeListener listener;

    public ShakeEventManager() {
    }

    public void setListener(ShakeListener listener) {
        this.listener = listener;
    }

    public void init(Context context) {
        sensorManager = (SensorManager)  context.getSystemService(Context.SENSOR_SERVICE);
        s = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        register();
    }

    public void register() {
        sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float maxAcc = calculateMaxAcceleration(sensorEvent);
        Log.d("SwA", "Max Acc ["+maxAcc+"]");
        if (maxAcc >= MOV_THRESHOLD) {
            if (counter == 0) {
                counter++;
                firstMovTime = System.currentTimeMillis();
                Log.d("SwA", "First mov..");
            }
            else {
                long now = System.currentTimeMillis();
                if ((now - firstMovTime) < SHAKE_WINDOW_TIME_INTERVAL) {
                    counter++;
                }
                else {
                    resetAllData();
                    counter++;
                    return;
                }
                Log.d("SwA", "Mov counter ["+counter+"]");

                if (counter >= MOV_COUNTS) {
                    if (listener != null) {
                        listener.onShake();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    public void deregister()  {
        sensorManager.unregisterListener(this);
    }


    private float calculateMaxAcceleration(SensorEvent event) {
        gravity[0] = calculateGravityForce(event.values[0], 0);
        gravity[1] = calculateGravityForce(event.values[1], 1);
        gravity[2] = calculateGravityForce(event.values[2], 2);

        float accurateX = event.values[0] - gravity[0];
        float accurateY = event.values[1] - gravity[1];
        float accurateZ = event.values[2] - gravity[2];

        return Math.max(Math.max(accurateX, accurateY), accurateZ);
    }

    // Low pass filter
    private float calculateGravityForce(float currentValue, int index) {
        return  ALPHA * gravity[index] + (1 - ALPHA) * currentValue;
    }


    private void resetAllData() {
        Log.d("SwA", "Reset all data");
        counter = 0;
        firstMovTime = System.currentTimeMillis();
    }


    public static interface ShakeListener {
        public void onShake();
    }
}
