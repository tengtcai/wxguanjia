package com.android.hjq.wxmanager.utils;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.android.hjq.wxmanager.service.MyAccessibilityService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AccessibilityHelper {
    /**
     * 判断辅助服务是否正在运行
     */
    public static boolean isAccessibilitySettingsOn(Activity context) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 打开辅助服务的设置
     */
    public static void openAccessibilityServiceSettings(Activity context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 自动点击按钮
     *
     * @param service
     * @param nodeText 按钮文本(Button)
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void handleEvent(AccessibilityService service, String nodeText) {
        List<AccessibilityNodeInfo> unintall_nodes = service.getRootInActiveWindow().findAccessibilityNodeInfosByText(nodeText);
        if (unintall_nodes != null && !unintall_nodes.isEmpty()) {
            AccessibilityNodeInfo node;
            for (int i = 0; i < unintall_nodes.size(); i++) {
                node = unintall_nodes.get(i);
                if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    /**
     * 在当前页面查找文字内容并点击
     *
     * @param text
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void findTextAndClick(AccessibilityService accessibilityService, String text) {
        AccessibilityNodeInfo accessibilityNodeInfo = accessibilityService.getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null && (text.equals(nodeInfo.getText()) || text.equals(nodeInfo.getContentDescription()))) {
                    performClick(nodeInfo);
                    break;
                }
            }
        }
    }


    /**
     * 获取text
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getNodeText(AccessibilityService accessibilityService,String id) {
        List<AccessibilityNodeInfo> unintall_nodes = accessibilityService.getRootInActiveWindow().findAccessibilityNodeInfosByViewId(id);
        if (unintall_nodes != null && !unintall_nodes.isEmpty()) {
            return unintall_nodes.get(0).getText().toString().trim();
        }
        return null;
    }

    /**
     * 获取text
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getNodeText(AccessibilityNodeInfo nodeInfo, String id) {
        List<AccessibilityNodeInfo> unintall_nodes = nodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (unintall_nodes != null && !unintall_nodes.isEmpty()) {
            return unintall_nodes.get(0).getText().toString().trim();
        }
        return null;
    }

    //通过id查找
    public static AccessibilityNodeInfo findNodeInfosById(AccessibilityNodeInfo nodeInfo, String resId) {
        if (nodeInfo == null) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    //通过id查找
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static AccessibilityNodeInfo findNodeInfosById(AccessibilityService service,String resId) {
        if (service.getRootInActiveWindow() != null) {
            List<AccessibilityNodeInfo> list = service.getRootInActiveWindow().findAccessibilityNodeInfosByViewId(resId);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    //通过id查找
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static List<AccessibilityNodeInfo> findNodeListInfosById(AccessibilityService service,String resId) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        if (service.getRootInActiveWindow() != null) {
            List<AccessibilityNodeInfo> list = service.getRootInActiveWindow().findAccessibilityNodeInfosByViewId(resId);
            if (list != null) {
                result.addAll(list);
            }
        }
        return result;
    }

    //通过id查找 ,第i个组件
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static AccessibilityNodeInfo findNodeInfosById(AccessibilityService service,String resId, int index) {
        List<AccessibilityNodeInfo> list = service.getRootInActiveWindow().findAccessibilityNodeInfosByViewId(resId);
        if (list != null && list.size() > index) {
            return list.get(index);
        }
        return null;
    }

    //返回指定位置的node
    public static AccessibilityNodeInfo findNodeInfosByIdAndPosition(AccessibilityNodeInfo nodeInfo, String resId, int position) {
        if (nodeInfo == null) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
            for (int i = 0; i < list.size(); i++) {
                if (i == position) {
                    return list.get(i);
                }
            }
        }
        return null;
    }

    //通过某个文本查找
    public static AccessibilityNodeInfo findNodeInfosByText(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo == null) return null;
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    //通过某个文本查找
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static AccessibilityNodeInfo findNodeInfosByText(AccessibilityService service,String text) {
        if (service.getRootInActiveWindow() != null) {
            List<AccessibilityNodeInfo> list = service.getRootInActiveWindow().findAccessibilityNodeInfosByText(text);
            if (list == null || list.isEmpty()) {
                return null;
            }
            return list.get(0);
        }
        return null;
    }

    //通过ClassName查找
    public static AccessibilityNodeInfo findNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        for (int i = 0; nodeInfo != null && i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            if (node != null) {
                if (className.equals(node.getClassName())) {
                    return node;
                } else if (node.getChildCount() > 0) {
                    findNodeInfosByClassName(node, className);
                }
            }
        }
        return null;
    }

    //通过ClassName查找
    public static List<AccessibilityNodeInfo> findNodeInfoListByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (TextUtils.isEmpty(className)) {
            return Collections.EMPTY_LIST;
        }
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        for (int i = 0; nodeInfo != null && i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            if (node != null && className.equals(node.getClassName())) {
                result.add(node);
            }
        }
        return result;
    }

    //通过ClassName查找
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static AccessibilityNodeInfo findNodeInfosByClassName(AccessibilityService service,String className) {
        return findNodeInfosByClassName(service.getRootInActiveWindow(), className);
    }

    /**
     * 找父组件
     */
    public static AccessibilityNodeInfo findParentNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (nodeInfo == null) {
            return null;
        }
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        if (className.equals(nodeInfo.getClassName())) {
            return nodeInfo;
        }
        return findParentNodeInfosByClassName(nodeInfo.getParent(), className);
    }

    private static final Field sSourceNodeField;

    static {
        Field field = null;
        try {
            field = AccessibilityNodeInfo.class.getDeclaredField("mSourceNodeId");
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sSourceNodeField = field;
    }

    public static long getSourceNodeId(AccessibilityNodeInfo nodeInfo) {
        if (sSourceNodeField == null) {
            return -1;
        }
        try {
            return sSourceNodeField.getLong(nodeInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getViewIdResourceName(AccessibilityNodeInfo nodeInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return nodeInfo.getViewIdResourceName();
        }
        return null;
    }

    //返回HOME界面
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void performHome(AccessibilityService service) {
        if (service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    //返回
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void performBack(AccessibilityService service) {
        if (service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /**
     * 点击事件
     */
    public static void performClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        if (nodeInfo.isClickable()) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            performClick(nodeInfo.getParent());
        }
    }

    /**
     * 点击事件
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void performClick(AccessibilityService service,String id) {
        performClick(findNodeInfosById(service,id));
    }

    //长按事件
    public static void performLongClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
    }

    //move 事件
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void performMoveDown(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        nodeInfo.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.getId());
    }


    //ACTION_SCROLL_FORWARD 事件
    public static boolean perform_scroll_forward(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    //ACTION_SCROLL_BACKWARD 后退事件
    public static boolean perform_scroll_backward(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }

    //粘贴
    @TargetApi(18)
    public static void performPaste(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }

    //设置editview text
    @TargetApi(21)
    public static void performSetText(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo == null) {
            return;
        }
        CharSequence className = nodeInfo.getClassName();
        if ("android.widget.EditText".equals(className)) {//||"android.widget.TextView".equals(className)
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo
                    .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
    }

    /**
     *向微信对话输入框中填充数据
     * @param accessibilityService
     * @param id
     * @param content
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean findViewByIdAndPasteContent(AccessibilityService accessibilityService, String id, String content) {
        AccessibilityNodeInfo rootNode = accessibilityService.getRootInActiveWindow();
        if (rootNode != null) {
            List<AccessibilityNodeInfo> editInfo = rootNode.findAccessibilityNodeInfosByViewId(id);
            if (editInfo != null && !editInfo.isEmpty()) {
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, content);
                editInfo.get(0).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * 通过id查找对应的文字
     * @param accessibilityService
     * @param id
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String findTextById(AccessibilityService accessibilityService, String id) {
        AccessibilityNodeInfo rootInfo = accessibilityService.getRootInActiveWindow();
        if (rootInfo != null) {
            List<AccessibilityNodeInfo> userNames = rootInfo.findAccessibilityNodeInfosByViewId(id);
            if (userNames != null && userNames.size() > 0) {
                String name = userNames.get(0).getText().toString();
                return name;
            }
        }
        return null;
    }

}

