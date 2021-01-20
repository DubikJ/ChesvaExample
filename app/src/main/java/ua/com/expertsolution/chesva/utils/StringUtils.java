package ua.com.expertsolution.chesva.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.DisplayMetrics;

import java.io.UnsupportedEncodingException;

public class StringUtils {

    public static SpannableString spannableString(String text) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        return spannableString;
    }

    public static String toBase64(String message) {
        try {
            byte[] data = message.getBytes("UTF-8");
            String base64Sms = Base64.encodeToString(data, Base64.DEFAULT);
            return base64Sms;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String fromBase64(String message) {
        try {
            byte[] data = Base64.decode(message, Base64.DEFAULT);
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getStringByIdName(Context context, String idName) {
        Resources standardResources = context.getResources();
        AssetManager assets = standardResources.getAssets();
        DisplayMetrics metrics = standardResources.getDisplayMetrics();
        Configuration config = new Configuration(standardResources.getConfiguration());
        config.locale = standardResources.getConfiguration().locale;
        Resources defaultResources = new Resources(assets, metrics, config);
        return defaultResources.getString(defaultResources.getIdentifier(idName, "string", context.getPackageName()));
    }
}
