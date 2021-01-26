package tj.Appcrastination;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import java.util.HashMap;
import java.util.List;

public class BackgroundService extends JobIntentService {
    private DatabaseHelper dbHelper;
    private static final String TAG = "BackgroundService";
    private static final int JOB_ID = 1;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, BackgroundService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        dbHelper = new DatabaseHelper(this);
        List<TrackedAppInfo> trackedAppInfos = dbHelper.getAllRows();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long beginTime = calendar.getTimeInMillis();
        long endTime = beginTime + Utils.DAY_IN_MILLIS;
        HashMap<String, Integer> appUsageMap = Utils.getTimeSpent(this, null, beginTime, endTime);

        for(int i = 0; i < trackedAppInfos.size(); i++) {
            TrackedAppInfo trackedAppInfo = trackedAppInfos.get(i);
            String packageName = trackedAppInfo.getPackageName();

            if(appUsageMap.containsKey(packageName)) {
                Integer usageTime = appUsageMap.get(packageName);
                if(usageTime == null) usageTime = 0;
                int allowedTime = trackedAppInfo.getTimeAllowed();
                int isUsageExceeded = trackedAppInfo.getIsUsageExceeded();

                if(usageTime > allowedTime && isUsageExceeded == 0) {
                    try {
                        dbHelper.setIsUsageExceeded(packageName);
                        String appName = (String) getPackageManager()
                                .getApplicationLabel(getPackageManager().getApplicationInfo(packageName, 0));
                        showNotification(appName, i);
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "package name not found");
                    }
                }
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }


    private void showNotification(String appName, int id) {
        final int PRIMARY_FOREGROUND_NOTIF_SERVICE_ID = 1001;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_channel_01";
            CharSequence name =" my channel";
            String description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "notification", importance);
            mChannel.setDescription(description);
            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentTitle(appName + " usage exceeded!")
                    .setContentText("Close your app now!")
                    .setSmallIcon(R.drawable.warning)
                    .build();

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
                mNotificationManager.notify(PRIMARY_FOREGROUND_NOTIF_SERVICE_ID, notification);
            }

            startForeground(PRIMARY_FOREGROUND_NOTIF_SERVICE_ID, notification);
        }
    }
}


