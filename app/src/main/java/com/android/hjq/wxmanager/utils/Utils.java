package com.android.hjq.wxmanager.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.hjq.wxmanager.MyApplication;
import com.blankj.utilcode.util.LogUtils;

import java.util.List;


public class Utils {
    public static void copy(String text){
        //获取剪贴版
        ClipboardManager clipboard = (ClipboardManager) MyApplication.context.getSystemService(Context.CLIPBOARD_SERVICE);
        //创建ClipData对象
        //第一个参数只是一个标记，随便传入。
        //第二个参数是要复制到剪贴版的内容
        ClipData clip = ClipData.newPlainText("wxguanjia", text);
        //传入clipdata对象.
        clipboard.setPrimaryClip(clip);
    }

    public static String getCopy(){
        ClipboardManager cm = (ClipboardManager) MyApplication.context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = cm.getPrimaryClip();
        ClipData.Item item = data.getItemAt(0);
        return item.getText().toString();
    }

    public static Intent getAppOpenIntentByPackageName(String packageName){
        //Activity完整名
        String mainAct = null;
        //根据包名寻找
        PackageManager pkgMag = MyApplication.context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_NEW_TASK);

        List<ResolveInfo> list = pkgMag.queryIntentActivities(intent,
                PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }

    public static Context getPackageContext(String packageName) {
        Context pkgContext = null;
        if (MyApplication.context.getPackageName().equals(packageName)) {
            pkgContext = MyApplication.context;
        } else {
            // 创建第三方应用的上下文环境
            try {
                pkgContext = MyApplication.context.createPackageContext(packageName,
                        Context.CONTEXT_IGNORE_SECURITY
                                | Context.CONTEXT_INCLUDE_CODE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return pkgContext;
    }

    public static boolean openPackage(String packageName) {
        Context pkgContext = getPackageContext(packageName);
        Intent intent = getAppOpenIntentByPackageName(packageName);
        if (pkgContext != null && intent != null) {
            pkgContext.startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * 获取运行中的线程
     * @param mContext
     */
    public static void listServices(Context mContext) {
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(100);

        if (!(serviceList.size() > 0)) {
            return;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            LogUtils.d("cao service name = " + serviceList.get(i).service.getClassName());
        }
    }

}
