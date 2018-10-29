package nss.mobile.video.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * 用于存储 获取 存放数据；
 * Created by Administrator on 2017-2-9.
 */
public class PreferencesUtils {
    private static final String FILE_NAME = "saveKey";

    public static void saveString(Context context, String key, String value) {
        saveString(context,FILE_NAME,key,value);
    }

    public static void saveString(Context context,String fileName, String key, String value) {
        SharedPreferences sp =context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }


    public static String queryString(Context context, String key) {
        String value  = queryString(context,FILE_NAME,key);
        return value;
    }

    public static void saveInt(Context context, String key, int value) {
        saveInt(context,FILE_NAME,key,value);
    }

    public static void saveInt(Context context,String fileName, String key, int value) {
        SharedPreferences sp =context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt(key, value);
        edit.commit();
    }


    public static int queryInt(Context context, String key,int normal) {
        int value  = queryInt(context,FILE_NAME,key,normal);
        return value;
    }

    public static int queryInt(Context context, String fileName, String key,int normal) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        int value = sp.getInt(key, normal);
        return value;
    }

    public static void saveFloat(Context context,String fileName, String key, float value) {
        SharedPreferences sp =context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putFloat(key, value);
        edit.commit();
    }

    public static float queryFloat(Context context, String key,float normal) {
        float value  = queryFloat(context,FILE_NAME,key,normal);
        return value;
    }

    private static float queryFloat(Context context, String fileName, String key,float normal) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        float value = sp.getFloat(key, normal);
        return value;
    }

    public static void saveLong(Context context, String key, long value) {
        saveLong(context,FILE_NAME,key,value);
    }

    public static void saveLong(Context context,String fileName, String key, long value) {
        SharedPreferences sp =context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    public static long queryLong(Context context, String key,long normal) {
        long value  = queryLong(context,FILE_NAME,key,normal);
        return value;
    }

    private static long queryLong(Context context, String fileName, String key,long normal) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        long value = sp.getLong(key, normal);
        return value;
    }

    public static String queryString(Context context,String fileName, String key) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        String value = sp.getString(key, "");
        return value;
    }
    public static boolean queryBoolean(Context context,String key,boolean normal) {

        boolean value =  queryBoolean(context,FILE_NAME,key,normal);
        return value;
    }
    public static boolean queryBoolean(Context context,String fileName,String key,boolean normal) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        boolean value = sp.getBoolean(key, normal);
        return value;
    }
    public static void saveBoolean(Context context,String key, boolean value) {
        saveBoolean(context,FILE_NAME,key,value);
    }
    public static void saveBoolean(Context context,String fileName,String key, boolean value) {
        SharedPreferences sp =context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static void deleteKey(Context context,String key) {
        deleteKey(context, FILE_NAME,key);
    }
    
    public static void deleteKey(Context context,String fileName,String key) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.remove(key);
        edit.commit();
    }


    public static void deleteFile(Context context){
        deleteFile(context,FILE_NAME);

    }
    public static void deleteFile(Context context, String fileName) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        sp.edit().clear().commit();
    }



}
