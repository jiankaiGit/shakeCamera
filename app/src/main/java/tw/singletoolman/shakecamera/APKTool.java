package tw.singletoolman.shakecamera;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by EasonChang on 2019/7/31.
 */

public class APKTool {
    static  String TAG = "ApkTool";

    public static String scanLocalInstallAppList(PackageManager packageManager) {
        String appLabel = "";
        String appPackageName = "";
        try {
            List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
            for (int i = 0; i < packageInfos.size(); i++) {
                PackageInfo packageInfo = packageInfos.get(i);
                appLabel = (String) packageManager.getApplicationLabel(packageInfo.applicationInfo);

                if ((ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags ) != 0) {
                    if(appLabel.equals("Camera") || appLabel.equals("相機") || appLabel.equals("相机")){
                        appPackageName  = packageInfo.packageName;
                    }
                }
            }
        }catch (Exception e){
            Log.e(TAG,"===============獲取APP Package name 失敗");
        }
        return appPackageName;
    }
}
