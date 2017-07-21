package com.genlan.mereader.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Description
 * Author Genlan
 * Date 2017/7/18
 */

public class ShareUtil {

    private static ShareUtil sInstance;
    private static final String APP_PARAM = "PDF_demo";

    private SharedPreferences mPrefs;

    private ShareUtil(Context context){
        mPrefs = context.getSharedPreferences(APP_PARAM, Context.MODE_PRIVATE);
    }

    public static ShareUtil getInstance(){
        if (sInstance == null){
            synchronized (ShareUtil.class){
                if (sInstance == null){
                    throw new NullPointerException("Config isn't being initialized!");
                }
            }
        }
        return sInstance;
    }

    public static ShareUtil getInstance(Context context){
        if (sInstance == null) {
            synchronized (ShareUtil.class) {
                if (sInstance == null) {
                    sInstance = new ShareUtil(context);
                }
            }
        }
        return sInstance;
    }

    public boolean put(String key,String value){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, value);
        return editor.commit();
    }
    public boolean put(String key,int value){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public boolean put(String key,boolean value){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public String  getString(String key){
        return mPrefs.getString(key, "");
    }

    public int getInt(String key){
        return mPrefs.getInt(key,-1);
    }

    public boolean getBoolean(String key){
        return mPrefs.getBoolean(key,false);
    }

}
