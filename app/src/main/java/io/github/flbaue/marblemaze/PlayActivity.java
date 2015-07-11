package io.github.flbaue.marblemaze;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

public class PlayActivity extends Activity implements SensorEventListener {

    public static final int LEVEL_1 = 1;
    private static final String LEVEL = "LEVEL";

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private PlayView mPlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        mPlayView = (PlayView)findViewById(R.id.play);
    }

    public static Intent getIntent(Context context, int level) {
        Intent intent = new Intent(context, PlayActivity.class);
        intent.putExtra(LEVEL, level);
        return intent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mPlayView.updateMovement(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
