package com.android.hjq.wxmanager.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.android.hjq.wxmanager.activity.MainActivity;
import com.android.hjq.wxmanager.model.QunInfo;
import com.android.hjq.wxmanager.utils.AccessibilityHelper;
import com.android.hjq.wxmanager.utils.SpUtils;
import com.android.hjq.wxmanager.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "HJQresult";
    private List<String> allNameList = new ArrayList<>();
    private List<String> bQList;//标签数组
    private List<String> bQAllList = new ArrayList<>();//标签下所有好友
    private List<String> qunList = new ArrayList<>();//群列表
    private List<String> imgList = new ArrayList<>();//记录浏览过的图片
    private boolean unScrollToBottom = true;//未滚动到底部
    private boolean unBQScrollToBottom = true;//标签未滚动到底部
    private boolean unBQALLScrollToBottom = true;//标签下所有好友未滚动到底部
    private boolean unQunScrollToBottom = true;//群列表未滚动到底部
    private boolean unScrollToBottomPic = true;//图片列表没有滑动到底部

    private boolean isSend = false;//是否发送消息

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
//        Log.e(TAG,event.getClassName().toString());
        if(SpUtils.isDestory()){
            return;
        }
        int eventType = event.getEventType();
        int type = SpUtils.getType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_SCROLLED: //界面滚动
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED://窗口状态发生改变
                //通过 event.getClassName().toString()可以获取当前Activity的类名,当前类名如果是
                // “com.tencent.mm.ui.LauncherUI”，说明微信已经来到进入到了首页
                if(event.getClassName().toString().equals("com.tencent.mm.ui.LauncherUI")){//进入微信首页
                    AccessibilityHelper.findTextAndClick(this, "通讯录");
                    try {
                        Thread.sleep(200);//不睡眠会出问题，具体原因我也不知道(应该是界面滚动需要时间)
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    switch (type){
                        case 1://发送给所有好友
                            try {
                                if(SpUtils.isFirstStart()) {
                                    AccessibilityHelper.findTextAndClick(this, "通讯录");
                                    //再次点击通讯录，确保通讯录列表移动到了顶部
                                    allNameList.clear();
                                    unScrollToBottom = true;
                                    Thread.sleep(200);//不睡眠会出问题，具体原因我也不知道(应该是界面滚动需要时间)
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            SpUtils.setFirstStart(false);
                            //遍历通讯录联系人列表，查找联系人
                            TraversalAndFindContacts();
                            break;
                        case 2: //发送给指定标签
                            AccessibilityHelper.findTextAndClick(this, "通讯录");
                            try {
                                Thread.sleep(200);//不睡眠会出问题，具体原因我也不知道(应该是界面滚动需要时间)
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //找到标签按钮并点击
                            AccessibilityNodeInfo nodeInfo = AccessibilityHelper.findNodeInfosById(this,"com.tencent.mm:id/nc",1);
                            if(nodeInfo != null){
                                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                            break;
                        case 3://群发
                        case 4://检测群
                            //点击首页的更多功能按钮实现群聊
//                            AccessibilityNodeInfo nodeInfo1 = AccessibilityHelper.findNodeInfosById(this,"com.tencent.mm:id/jb",1);
//                            if(nodeInfo1 != null){
//                                nodeInfo1.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);//点击更多功能按钮
//                            }
                            //点击通讯录里面的群聊按钮实现群聊
                            AccessibilityHelper.findTextAndClick(this, "通讯录");
                            try {
                                Thread.sleep(200);//不睡眠会出问题，具体原因我也不知道(应该是界面滚动需要时间)
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //找到标签按钮并点击
                            AccessibilityNodeInfo nodeInfo1 = AccessibilityHelper.findNodeInfosById(this,"com.tencent.mm:id/nc",0);
                            if(nodeInfo1 != null){
                                nodeInfo1.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                            break;
                    }
                } else if(event.getClassName().toString().equals("com.tencent.mm.plugin.profile.ui.ContactInfoUI")){//好友信息页面
                    List<AccessibilityNodeInfo> contactUiList = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b59");
                    if(contactUiList != null && contactUiList.size()>0) {
                        isSend = true;
                        AccessibilityHelper.performClick(contactUiList.get(0));//点击发送消息按钮
                    }
                }else if(event.getClassName().toString().equals("com.tencent.mm.ui.chatting.ChattingUI")){//聊天列表
                    Log.e(TAG, isSend+"   "+AccessibilityHelper.findTextById(this,"com.tencent.mm:id/ko"));
                    if(isSend) {
//                        if(AccessibilityHelper.findTextById(this,"com.tencent.mm:id/ko").equals("测试(3)")) {
                            sendMessage();
//                        }else {
//                            AccessibilityHelper.performBack(this);
//                        }
                    }else {
                        AccessibilityHelper.performBack(this);
                    }
                }else if(event.getClassName().toString().equals("com.tencent.mm.plugin.label.ui.ContactLabelManagerUI")){//微信标签页面
                    String biaoqian = SpUtils.getBiaoQian();
                    unBQScrollToBottom = true;
                    if (bQList == null) {
                        bQList = new ArrayList<>();
                        if (bQList.size() == 0 && !TextUtils.isEmpty(biaoqian)) {//给指定标签发送消息
                            String[] strings = biaoqian.split("，");
                            for (int i = 0; i < strings.length; i++) {
                                bQList.add(strings[i]);
                            }
                        }
                    }
                    findBiaoQian();
                }else if(event.getClassName().toString().equals("com.tencent.mm.plugin.label.ui.ContactLabelEditUI")){//标签详情
                    //遍历所有用户然后循环点击发送消息
                    unBQALLScrollToBottom = true;
                    findBiaoQianAllFriends();
                }else if(event.getClassName().toString().equals("android.widget.FrameLayout")){//点击发起群聊
                    AccessibilityNodeInfo nodeInfo2 = AccessibilityHelper.findNodeInfosById(this, "com.tencent.mm:id/l_", 0);
                    if (nodeInfo2 != null) {
                        nodeInfo2.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }else if(event.getClassName().toString().equals("com.tencent.mm.ui.contact.SelectContactUI")){//点击选择一个群
                    AccessibilityNodeInfo nodeInfo = AccessibilityHelper.findNodeInfosById(this,"com.tencent.mm:id/b14",0);
                    if(nodeInfo != null){
                        nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }else if(event.getClassName().toString().equals("com.tencent.mm.ui.contact.GroupCardSelectUI")){//群列表
                    unQunScrollToBottom = true;
                    findQunList();
                }else if(event.getClassName().toString().equals("com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI")){ //选择图片页面
                    unScrollToBottomPic = true;
                    sendPic();
                }else if(event.getClassName().toString().equals("com.tencent.mm.ui.contact.ChatroomContactUI")){//通讯录群聊标签页面
                    unQunScrollToBottom = true;
                    if(type == 4){
                        checkQunList();
                    }else{
                        findTongXunQunLiao();
                    }
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {
    }

    /**
     * 从头至尾遍历寻找联系人
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void TraversalAndFindContacts() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> listview = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nx");
        //没有滚动到了底部
        if (listview != null && !listview.isEmpty()) {
            while (unScrollToBottom) {
                List<AccessibilityNodeInfo> nameList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/o1");
                List<AccessibilityNodeInfo> itemList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nx");
                for(int i = 0;i<nameList.size();i++){
                    String name = nameList.get(i).getText().toString();
                    if(!allNameList.contains(name) && !name.equals("微信团队") && !name.equals("文件传输助手")){
                        allNameList.add(name);
                        if(itemList.get(i).getParent() != null){
                            itemList.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            return;
                        }
                    }
                }
                unScrollToBottom = listview.get(0).getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                //必须等待，因为需要等待滚动操作完成
                try {
                    Thread.sleep(260);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        SpUtils.setDestory(true);//结束无障碍
        stop();
    }

    /**
     * 发送消息
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sendMessage(){
        //当前页面可能处于发送语音状态，需要切换成发送文本状态
        List<AccessibilityNodeInfo> unintall_nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText("发送");
        if (unintall_nodes != null && !unintall_nodes.isEmpty()) {
            AccessibilityHelper.performClick(this,"com.tencent.mm:id/amg");
        }
        AccessibilityHelper.findViewByIdAndPasteContent(this,"com.tencent.mm:id/ami",Utils.getCopy());
        AccessibilityHelper.performClick(this,"com.tencent.mm:id/amp");
        if(!TextUtils.isEmpty(SpUtils.getImage())){
            AccessibilityNodeInfo nodeInfo = AccessibilityHelper.findNodeInfosById(this,"com.tencent.mm:id/amo");
            if(nodeInfo != null){
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            AccessibilityNodeInfo nodeInfo1 = AccessibilityHelper.findNodeInfosById(this,"com.tencent.mm:id/xj",0);
            if(nodeInfo1 != null){
                nodeInfo1.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 遍历所有的标签
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void findBiaoQian(){
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> listview = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b41");
        //没有滚动到了底部
        if (listview != null && !listview.isEmpty()) {
            while (unBQScrollToBottom) {
                List<AccessibilityNodeInfo> nameList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b42");
                List<AccessibilityNodeInfo> itemList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b41");
                for(int i = 0;i<nameList.size();i++){
                    String name = nameList.get(i).getText().toString();
                    if(bQList.contains(name)){
                        if(itemList.get(i).getParent() != null){
                            itemList.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            return;
                        }
                    }
                }
                unBQScrollToBottom = listview.get(0).getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                //必须等待，因为需要等待滚动操作完成
                try {
                    Thread.sleep(260);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e("HJQresult","发送完毕");
        bQList = null;
        SpUtils.setDestory(true);//结束无障碍
        stop();
    }

    private void stop(){
        Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_TAG);
        intent.putExtra("type",1);
        sendBroadcast(intent);
    }

    /**
     * 遍历某个标签下的所有好友
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void findBiaoQianAllFriends(){
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> listview = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/e42");
        //没有滚动到了底部
        if (listview != null && !listview.isEmpty()) {
            while (unBQALLScrollToBottom) {
                List<AccessibilityNodeInfo> nameList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/e42");
                for(int i = 0;i<nameList.size();i++){
                    String name = nameList.get(i).getText().toString();
                    if(!bQAllList.contains(name)){
                        bQAllList.add(name);
                        isSend = true;
                        nameList.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return;
                    }
                }
                unBQALLScrollToBottom = listview.get(0).getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                //必须等待，因为需要等待滚动操作完成
                try {
                    Thread.sleep(260);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if(bQAllList!=null){
            bQAllList.clear();
            bQList.remove(0);
        }
        AccessibilityNodeInfo nodeInfo = AccessibilityHelper.findNodeInfosById(this,"com.tencent.mm:id/kw");
        if(nodeInfo != null){
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 遍历所有的群
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void findQunList(){
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> listview = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cac");
        //没有滚动到了底部
        if (listview != null && !listview.isEmpty()) {
            while (unQunScrollToBottom) {
                List<AccessibilityNodeInfo> nameList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cad");
                List<AccessibilityNodeInfo> itemList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cac");
                for(int i = 0;i<nameList.size();i++){
                    String name = nameList.get(i).getText().toString();
                    if(!qunList.contains(name)){
                        qunList.add(name);
                        if(itemList.get(i).getParent() != null){
                            isSend = true;
                            itemList.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            return;
                        }
                    }
                }
                unQunScrollToBottom = listview.get(0).getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                //必须等待，因为需要等待滚动操作完成
                try {
                    Thread.sleep(260);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e("HJQresult","发送完毕");
        if(qunList != null) {
            qunList.clear();
        }
        SpUtils.setDestory(true);//结束无障碍
        stop();
    }

    /**
     * 通讯录群聊标签
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void findTongXunQunLiao(){
        List<QunInfo> qunInfos = SpUtils.getSelectQunList();
        List<String> qunInfoList = new ArrayList<>();
        for(int i = 0;i<qunInfos.size();i++){
            qunInfoList.add(qunInfos.get(i).getTitle());
        }
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> listview = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nj");
        //没有滚动到了底部
        if (listview != null && !listview.isEmpty()) {
            while (unQunScrollToBottom) {
                List<AccessibilityNodeInfo> nameList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nr");
                List<AccessibilityNodeInfo> itemList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nj");
                for(int i = 0;i<nameList.size();i++){
                    String name = nameList.get(i).getText().toString();
                    if(!qunList.contains(name) && qunInfoList.contains(name)){
                        qunList.add(name);
                        if(itemList.get(i).getParent() != null){
                            isSend = true;
                            itemList.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            return;
                        }
                    }
                }
                unQunScrollToBottom = listview.get(0).getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                //必须等待，因为需要等待滚动操作完成
                try {
                    Thread.sleep(260);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e("HJQresult","发送完毕");
        if(qunList != null) {
            qunList.clear();
        }
        SpUtils.setDestory(true);//结束无障碍
        stop();
    }

    /**
     * 查找群
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void checkQunList(){
        List<QunInfo> qunInfos = new ArrayList<>();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> listview = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nj");
        //没有滚动到了底部
        if (listview != null && !listview.isEmpty()) {
            while (unQunScrollToBottom) {
                List<AccessibilityNodeInfo> nameList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nr");
                for(int i = 0;i<nameList.size();i++){
                    String name = nameList.get(i).getText().toString();
                    QunInfo qunInfo = new QunInfo();
                    if(!qunInfos.contains(name)){
                        qunInfo.setTitle(name);
                        qunInfo.setSelect(false);
                        qunInfos.add(qunInfo);
                    }
                }
                unQunScrollToBottom = listview.get(0).getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                //必须等待，因为需要等待滚动操作完成
                try {
                    Thread.sleep(260);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        SpUtils.saveQunList(qunInfos);
        SpUtils.setDestory(true);//结束无障碍
        Utils.openPackage("com.android.hjq.wxmanager");
        stop();
    }

    /***
     * 发送照片
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sendPic(){
        if(imgList != null){
            imgList.clear();
        }
        String[] strings = SpUtils.getImage().split("，");
        List<String> selectImg = new ArrayList<>();
        for(int i = 0;i<strings.length;i++){
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            List<AccessibilityNodeInfo> listview = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ih");
            if (listview != null && !listview.isEmpty()) {
                while (unScrollToBottomPic) {
                    List<AccessibilityNodeInfo> nameList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/akx");
                    List<AccessibilityNodeInfo> itemList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bpz");
                    for(int j = 0;j<nameList.size();j++) {
                        String str = nameList.get(j).getContentDescription().toString().split(",")[0];
                        String position = str.substring(3,str.length());
                        if(!imgList.contains(position)) {
                            imgList.add(position);
                            if (strings[i].equals(position)) {
                                selectImg.add(position);
                                if (itemList.get(j) != null) {
                                    itemList.get(j).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    break;
                                }
                            }
                        }
                    }
                    if(selectImg.size() == strings.length){
                        unScrollToBottomPic = false;
                    }else {
                        unScrollToBottomPic = listview.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        try {
                            Thread.sleep(260);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        AccessibilityNodeInfo nodeInfo = AccessibilityHelper.findNodeInfosById(this,"com.tencent.mm:id/ki");
        if(nodeInfo != null){
            isSend= false;
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);//发送图片
        }
    }
}
