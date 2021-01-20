package ua.com.expertsolution.chesva.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import static ua.com.expertsolution.chesva.common.Consts.TAGLOG;

@SuppressLint("LongLogTag")
public class SharedStorage {
    private static final String FAILE_CACHE_NAME = "etrfghg-115-000-mmmedfsdf-gdkgf-876";
    private static final int MODE_STORAGE = Context.MODE_MULTI_PROCESS;


    private SharedStorage() {
    }

    private static SharedPreferences getSharedPreferences(Context mContext, String type){
        return mContext.getSharedPreferences(type, MODE_STORAGE);
    }

    public static String getString(Context mContext, String type, String key, String defValue) {
        String result = defValue;
        try {
            result = getSharedPreferences(mContext, type).getString(key, defValue);
        } catch (ClassCastException e) {
            e.printStackTrace();
            Log.e(TAGLOG+"_SharedStorage", key +": "+ e.getMessage());
        }
        return result;
    }

    public static boolean getBoolean(Context mContext, String type, String key, boolean defValue) {
        boolean result = defValue;
        try {
            result = getSharedPreferences(mContext, type).getBoolean(key, defValue);
        } catch (ClassCastException e) {
            e.printStackTrace();
            Log.e(TAGLOG+"_SharedStorage", key +": "+ e.getMessage());
        }
        return result;
    }

    public static int getInteger(Context mContext, String type, String key, int defValue) {
        int result = defValue;
        try {
            result = getSharedPreferences(mContext, type).getInt(key, defValue);
        } catch (ClassCastException e) {
            e.printStackTrace();
            Log.e(TAGLOG+"_SharedStorage", key +": "+ e.getMessage());
        }
        return result;
    }

    public static long getLong(Context mContext, String type, String key, long defValue) {
        long result = defValue;
        try {
            result = getSharedPreferences(mContext, type).getLong(key, defValue);
        } catch (ClassCastException e) {
            e.printStackTrace();
            Log.e(TAGLOG+"_SharedStorage", key +": "+ e.getMessage());
        }
        return result;
    }

    public static double getDouble(Context mContext, String type, String key, double defValue) {
        double result = defValue;
        try {
            result = Double.longBitsToDouble(getLong(mContext, type, key, Double.doubleToLongBits(defValue)));
        } catch (ClassCastException e) {
            e.printStackTrace();
            Log.e(TAGLOG+"_SharedStorage", key +": "+ e.getMessage());
        }
        return result;
    }

    public static void setString(Context mContext, String type, String key, String value) {
        Editor editor = getSharedPreferences(mContext, type).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setBoolean(Context mContext, String type, String key, Boolean value) {
        Editor editor = getSharedPreferences(mContext, type).edit();
        editor.putBoolean(key, value == null ? false : value.booleanValue());
        editor.commit();
    }

    public static void setInteger(Context mContext, String type, String key, int value) {
        Editor editor = getSharedPreferences(mContext, type).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setLong(Context mContext, String type, String key, long value) {
        Editor editor = getSharedPreferences(mContext, type).edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void setDouble(Context mContext, String type, String key, double value) {
        Editor editor = getSharedPreferences(mContext, type).edit();
        editor.putLong(key, Double.doubleToRawLongBits(value));
        editor.commit();
    }

    public static void clearCache(Context mContext, String type){
        Editor editor = getSharedPreferences(mContext, type).edit();
        editor.clear().commit();
    }
}
