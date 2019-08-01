package tw.singletoolman.shakecamera;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by EasonChang on 2019/7/31.
 */

public class SensorService extends Service implements SensorEventListener{
    private String TAG = "SensorService";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    private String cameraPackageName = "";
    private Handler  setCameraFlagHandler = new Handler();;
    private Runnable setCameraFlagRunnable;
    private boolean cameraFlag = true;
    private static boolean mIsRunning = false;
    private static int mThreshold = 11;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(getResources().getString(R.string.app_name), getResources().getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null)
                return;
            manager.createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, getResources().getString(R.string.app_name))
                    .setAutoCancel(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setOngoing(true)
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
                    .build();

            startForeground(101, notification);

        }else{
            startForeground(1,new Notification());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI, new Handler());
        cameraPackageName = intent.getStringExtra("cameraPackageName");

        setCameraFlagRunnable = new Runnable() {
            @Override
            public void run() {
                cameraFlag = true;
            }
        };
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta; // perform low-cut filter
        if(mThreshold <5){
            mThreshold = 5;
        }else if(mThreshold > 30){
            mThreshold = 30;
        }
        if (mAccel > mThreshold && cameraFlag) {
            cameraFlag = false;
            Log.e(TAG,"sensitivity: "+mThreshold);
            showNotification();
            setCameraFlagHandler.postDelayed(setCameraFlagRunnable,5000);
        }
    }

    private void showNotification() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //true為開啟，false為關閉
        boolean ifOpen = powerManager.isScreenOn();

        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
        if(ifOpen & !flag & mIsRunning){
            Log.e(TAG,"shake!!!!!!!!!");
            openCamera();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void openCamera() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(cameraPackageName);
        if(intent != null){
            startActivity(intent);
        }
    }

    public static void setRunningStatus(boolean running){
        mIsRunning = running;
    }

    public static boolean getRunningStatus(){
        return mIsRunning;
    }

    public static void setThreshold(int threshold){
        mThreshold = threshold;
    }
}
