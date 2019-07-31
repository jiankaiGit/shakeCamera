package tw.singletoolman.shakecamera;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

public class MainActivity extends AppCompatActivity{
    private String cameraPackageName = "";
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCameraPackageName();
        Intent sensorService = new Intent(this,SensorService.class);
        sensorService.putExtra("cameraPackageName",cameraPackageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(sensorService);
        } else {
            startService(sensorService);
        }

    }

    private void initCameraPackageName(){
        cameraPackageName = APKTool.scanLocalInstallAppList(MainActivity.this.getPackageManager());
    }
}
