package com.android.hjq.wxmanager.utils;

import android.text.TextUtils;

import com.android.hjq.wxmanager.model.QunInfo;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * SharedPreferences使用工具类
 */
public class SpUtils {
    static SPUtils mSPUtils = SPUtils.getInstance(Utils.getApp().getPackageName());

    public static boolean isFirstStart() {
        return mSPUtils.getBoolean("FirstStart", false);
    }

    public static boolean isDestory(){
        return mSPUtils.getBoolean("isDestory",false);
    }

    public static void setFirstStart(boolean isFirst){
        mSPUtils.put("FirstStart",isFirst);
    }

    public static void setDestory(boolean isDestory){
        mSPUtils.put("isDestory",isDestory);
    }

    public  static void saveBiaoQian(String biaoqian){
        mSPUtils.put("biaoqian",biaoqian);
    }

    public static String getBiaoQian(){
        return mSPUtils.getString("biaoqian");
    }

    public static void saveType(int type){
        mSPUtils.put("type",type);
    }

    public static int getType(){
        return mSPUtils.getInt("type");
    }

    public static void saveImage(String img){
        mSPUtils.put("image",img);
    }
    public static String getImage(){
        return mSPUtils.getString("image");
    }

    public static void saveSay(List<String> list){
        mSPUtils.put("say_hello",new Gson().toJson(list));
    }

    public static List<String> getSay(){
        String str = mSPUtils.getString("say_hello");
        List<String> strings = new Gson().fromJson(str,new TypeToken<List<String>>(){}.getType());
        if(strings == null){
            strings = new ArrayList<>();
        }
        return strings;
    }

    public static void saveQunList(List<QunInfo> list){
        mSPUtils.put("qun_list",new Gson().toJson(list));
    }

    public static List<QunInfo> getQunList(){
        String str = mSPUtils.getString("qun_list");
        List<QunInfo> qunInfos = new Gson().fromJson(str,new TypeToken<List<QunInfo>>(){}.getType());
        if(qunInfos == null){
            qunInfos = new ArrayList<>();
        }
        return qunInfos;
    }

    public static void saveSelectQunList(List<QunInfo> list){
        mSPUtils.put("select_qun_list",new Gson().toJson(list));
    }

    public static List<QunInfo> getSelectQunList(){
        String str = mSPUtils.getString("select_qun_list");
        List<QunInfo> qunInfos = new Gson().fromJson(str,new TypeToken<List<QunInfo>>(){}.getType());
        if(qunInfos == null){
            qunInfos = new ArrayList<>();
        }
        return qunInfos;
    }

    public static void saveIsTimeTask(boolean isTimeTask){
        mSPUtils.put("isTimeTask",isTimeTask);
    }

    public static boolean getIsTimeTask(){
        return mSPUtils.getBoolean("isTimeTask");
    }
}
