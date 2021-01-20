package ua.com.expertsolution.chesva.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

import static ua.com.expertsolution.chesva.common.Consts.APP_SETTINGS_PREFS;
import static ua.com.expertsolution.chesva.common.Consts.TAGLOG;
import static ua.com.expertsolution.chesva.common.Consts.UI_LANG;

public class AppUtils {

    public static void setLocale(Context context) {

        String lang = SharedStorage.getString(context, APP_SETTINGS_PREFS, UI_LANG, getLocaleApp(context));

        Log.d(TAGLOG, "init locale: " + lang);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());

    }

    public static String getLocaleApp(Context cnx) {

        String lags = getCurrentLocale(cnx).getLanguage();

        switch (lags) {
            case "ru":
                break;
            case "uk":
                break;
            case "en":
                break;
            default:
                return "en";
        }
        return lags;
    }

    private static Locale getCurrentLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAGLOG, "current locale: " + context.getResources().getConfiguration().getLocales().get(0));
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            Log.d(TAGLOG, "current locale: " + context.getResources().getConfiguration().locale);
            return context.getResources().getConfiguration().locale;
        }
    }

}
