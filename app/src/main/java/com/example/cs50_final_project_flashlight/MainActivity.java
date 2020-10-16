package com.example.cs50_final_project_flashlight;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedpreferences;
    private Switch mySwitch;
    private SeekBar mySeekBar;
    private boolean flashState;
    private CameraManager camManager;
    private int sensibility;
    private long mOnOffTimestamp;
    private static final int ON_OFF_SLOP_TIME_MS = 500;
    private long now;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedpreferences = getSharedPreferences("mySensibility", Context.MODE_PRIVATE);
        // set the preferred sensibility
        sensibility = sharedpreferences.getInt("sensibility", 4);

        mySwitch = findViewById(R.id.mySwitch);
        mySeekBar = findViewById(R.id.mySeekBar);
        mySeekBar.setProgress(sensibility);

        // check if device has flash
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            camManager.registerTorchCallback(torchCallback, null);// (callback, handler)
        } else {
            Toast.makeText(getApplicationContext(), "No flash available on your device.",
                    Toast.LENGTH_SHORT).show();
        }

        // ShakeDetector initialization
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mShakeDetector = new ShakeDetector();
            mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

                @Override
                public void onShake(int count) {
                    if (count >= sensibility) {
                        // limit on/off
                        now = System.currentTimeMillis();
                        if (mOnOffTimestamp + ON_OFF_SLOP_TIME_MS < now) {
                            mOnOffTimestamp = now;
                            changeFlashState();
                        }

                    }
                }
            });
            //register the sensor Manager Listener
            mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(getApplicationContext(), "No Accelerometer available on your device.",
                    Toast.LENGTH_SHORT).show();
        }


        mySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFlashState();
            }
        });


        mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sensibility = progress;
                sharedpreferences.edit().putInt("sensibility", progress).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    // si vas a hacer la linterna como servicio hay que quitar onResume y onPause
    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        //mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    //@Override
    //public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        //mSensorManager.unregisterListener(mShakeDetector);
        //super.onPause();
    //}


    private void changeFlashState() {
        try {
            String cameraId = camManager.getCameraIdList()[0];
            camManager.setTorchMode(cameraId, !flashState);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    // callback to check changes in the flash status
    CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeUnavailable(String cameraId) {
            super.onTorchModeUnavailable(cameraId);
        }

        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);
            flashState = enabled;
            // If the flash is activated by shaking, adjust the switch correctly.
            mySwitch.setChecked(enabled);
        }
    };

}