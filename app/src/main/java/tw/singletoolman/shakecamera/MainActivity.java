package tw.singletoolman.shakecamera;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity{
    private String cameraPackageName = "";
    private String TAG = "MainActivity";
    private EditText sensitivityEdit;
    private FloatingActionButton powerBtn;
    private SharedPreferences mSP;
    private int sensitivityDefault = 11;
    private Intent sensorService;
    private String statusMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensitivityEdit = (EditText) findViewById(R.id.sensitivity_edit);
        powerBtn = (FloatingActionButton) findViewById(R.id.power_btn);
        mSP = getSharedPreferences("USER_DATA",MODE_PRIVATE);
        sensitivityDefault = Integer.valueOf(mSP.getString("USER","11"));
        sensitivityEdit.setText(String.valueOf(sensitivityDefault));
        initCameraPackageName();
        sensorService = new Intent(MainActivity.this,SensorService.class);
        sensorService.putExtra("cameraPackageName",cameraPackageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(sensorService);
        } else {
            startService(sensorService);
        }
        SensorService.setThreshold(sensitivityDefault);

        powerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(SensorService.getRunningStatus()){
                    statusMsg = getResources().getString(R.string.already_enable);
                }else{
                    SensorService.setRunningStatus(true);
                    statusMsg = getResources().getString(R.string.enable);
                }
                Toast.makeText(MainActivity.this,statusMsg,Toast.LENGTH_SHORT).show();
            }
        });

        powerBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                SensorService.setRunningStatus(false);
                Toast.makeText(MainActivity.this,getResources().getString(R.string.disable),Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        sensitivityEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {   // 按下完成按钮，这里和上面imeOptions对应
                    Toast.makeText(MainActivity.this,getResources().getString(R.string.input_finish),Toast.LENGTH_SHORT).show();
                    mSP.edit().putString("USER",sensitivityEdit.getText().toString()).commit();
                    SensorService.setThreshold(Integer.parseInt(sensitivityEdit.getText().toString()));
                }
                return false;   //返回true，保留软键盘。false，隐藏软键盘
            }
        });
    }

    private void initCameraPackageName(){
        cameraPackageName = APKTool.scanLocalInstallAppList(MainActivity.this.getPackageManager());
    }
}
