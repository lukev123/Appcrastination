package tj.Appcrastination;

import android.graphics.drawable.Drawable;

public class AppInfo implements Comparable<AppInfo>{
    private String appName;
    private Drawable icon;
    private String packageName;
    private boolean isTracked;
    private boolean isUsageExceeded;

    AppInfo(String appName, Drawable icon, String packageName, boolean isTracked, boolean isUsageExceeded) {
        this.appName = appName;
        this.icon = icon;
        this.packageName = packageName;
        this.isTracked = isTracked;
        this.isUsageExceeded = isUsageExceeded;
    }
    String getAppName() {
        return appName;
    }
    Drawable getIcon() {
        return icon;
    }
    String getPackageName() {
        return packageName;
    }
    boolean getIsTracked() {
        return isTracked;
    }
    boolean getIsUsageExceeded() {
        return isUsageExceeded;
    }

    @Override
    public int compareTo(AppInfo appInfo) {
        return appName.compareTo(appInfo.getAppName());
    }
}
// this class pulls the names and icons of the users applications, and displays them in a list on activity_main.xml
// it also checks if the usage time of the application is being tracked, and if that time exceeds the allotted usage time set by the user.