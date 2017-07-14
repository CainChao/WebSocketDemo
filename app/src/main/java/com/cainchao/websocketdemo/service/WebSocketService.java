package com.cainchao.websocketdemo.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.cainchao.websocketdemo.LogUtil;
import com.cainchao.websocketdemo.http.IWebSocketCallBack;
import com.cainchao.websocketdemo.http.WebSocketUtil;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by CainChao on 2017/7/8 0008.
 */

public class WebSocketService extends Service implements IWebSocketCallBack {

    private static final  String TAG = WebSocketService.class.getName();

    private final static int SOCKET_SERVICE_ID = 1001;

    private WebSocketUtil webSocketUtil;

    //重连次数
    private  int reConnecCount = 0;

    //超过时间
    private static final long HEART_BEAT_RATE = 8 * 1000;

    //等待时间
    private static final long SLEEPTIME = 10 * 1000;

    private long sendTime = System.currentTimeMillis();  //当你接收到服务的回应是将时间变为当前时间

    //最多重连次数
    private  int   re_network= 5;

    private Timer timer ;

    private TimerTaskFind timerTaskFind;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        LogUtil.e(TAG+"---onCreate--","onCreate");
        super.onCreate();
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(SOCKET_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
        } else {
            Intent innerIntent = new Intent(this, WebSocketService.class);
            startService(innerIntent);
            startForeground(SOCKET_SERVICE_ID, new Notification());
        }

        webSocketUtil = WebSocketUtil.newInstance();
        webSocketUtil.setWebSocketCallBack(this);
        webSocketUtil.requestNetWork();

        timer = new Timer();
        timerTaskFind = new TimerTaskFind();
        timeFind();
        LogUtil.e(TAG+"---save-life--","前台服务");

    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        LogUtil.e(TAG+"---onStartCommand---","onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private void timeFind(){
        timer.schedule(timerTaskFind,0, HEART_BEAT_RATE+SLEEPTIME);
    }

    /**
     * 定时任务
     */
    class  TimerTaskFind extends TimerTask {
        @Override
        public void run() {
            LogUtil.e(TAG+"--go in---","进入检测 reConnecCount"+reConnecCount);
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE+SLEEPTIME) {
                LogUtil.e(TAG+"--heartBeatRunnable---","心跳检测+reConnecCount"+reConnecCount);
                if(re_network < 0){//这re_network是我们后台可维护的,我在这并没有给出怎么获取的
                    timer.cancel();
                    EventBus.getDefault().post(
                            new WebSocketCallEvent(1));//超过重连次数要进行检测是客户端出问题还是后台出问题，然后给用户一个友好提示
                }else{
                    webSocketUtil.requestNetWork();
                    if(reConnecCount > re_network){
                        reConnecCount = 0;
                        EventBus.getDefault().post(
                                new WebSocketCallEvent(1));
                    }else{
                        reConnecCount++;
                    }
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != webSocketUtil){
            webSocketUtil.close();
        }
        LogUtil.e(TAG+"destroy", "Destroy");
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LogUtil.e(TAG+"---onOpen心跳---","onOpen--webSocketRun");
        webSocketUtil.sendMessage("open"); //起初客户端连上要个服务端发一个消息，表示你连上了，然后服务端会给你回应进行心跳
        sendTime = System.currentTimeMillis();
    }

    @Override
    public void onMessage(String message) {
        //这个进行心跳检测代码，根据后台发过来的message进行判断检测是否为心跳信息
        LogUtil.e(TAG+"---心跳---","webSocketRun");
        try{
            Thread.sleep(SLEEPTIME);
            webSocketUtil.sendMessage("open");
            sendTime = System.currentTimeMillis();
        }catch (WebsocketNotConnectedException e){
            e.printStackTrace();
            webSocketUtil.requestNetWork();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LogUtil.e(TAG+"--onClose--","Test---close");
    }

    @Override
    public void onError(Exception ex) {

    }

    @Override
    public void failure() {

    }
}
