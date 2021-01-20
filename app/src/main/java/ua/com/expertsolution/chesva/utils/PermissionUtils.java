package ua.com.expertsolution.chesva.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static ua.com.expertsolution.chesva.common.Consts.TAGLOG;

public class PermissionUtils {

    public static Boolean checkPermissions(Activity activity, int requestPermissions) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] ungrantedPermissions = requiredPermissionsStillNeeded(activity);
            if (ungrantedPermissions.length == 0) {
                return false;
            } else {
                ActivityCompat.requestPermissions(activity, ungrantedPermissions, requestPermissions);
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static String[] requiredPermissionsStillNeeded(Context mContext) {

        Set<String> permissions = new HashSet<String>();
        for (String permission : getRequiredPermissions(mContext)) {
            permissions.add(permission);
        }
        for (Iterator<String> i = permissions.iterator(); i.hasNext();) {
            String permission = i.next();
            if (ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAGLOG, "Permission: " + permission + " already granted.");
                i.remove();
            } else {
                Log.d(TAGLOG, "Permission: " + permission + " not yet granted.");
            }
        }
        return permissions.toArray(new String[permissions.size()]);
    }

    public static String[] getRequiredPermissions(Context mContext) {
        String[] permissions = null;
        try {
            permissions = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (permissions == null) {
            return new String[0];
        } else {
            return permissions.clone();
        }
    }
}
