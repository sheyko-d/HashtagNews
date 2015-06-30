package com.moysof.hashtagnews.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class Util {

    public static String LOGIN_URL = "http://www.moyersoftware.com/hashtagnews/app/login.php";

    public static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * (metrics.densityDpi / 160f));
        return px;
    }

    public static Boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void Log(Object text) {
        Log.d("HashedLog", text + "");
    }

}
