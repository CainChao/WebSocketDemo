package com.cainchao.websocketdemo.http;

import android.content.Context;
import android.os.Handler;

import com.cainchao.websocketdemo.LogUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author CainChao
 * 2017/7/12 0028.
 */

public class WebSocketUtil {
    private static final String TAG = WebSocketUtil.class.getSimpleName();

    private WebSocketClient client;// 连接客户端

    private IWebSocketCallBack callBack;

    private static final String WEBSOCKET_BASE_URL ="ws://192.168.199.133:9300/client/";
   private   WebSocketUtil(){
    }

    private static class SingletonHolder{
        public static WebSocketUtil instance = new WebSocketUtil();
    }

    public static WebSocketUtil newInstance(){
        return SingletonHolder.instance;
    }

    public void setWebSocketCallBack(IWebSocketCallBack callBack){
        this.callBack = callBack;
    }

   public void requestNetWork(){
      String  webSocketUrl = WEBSOCKET_BASE_URL;//参数直接加在这后面，这个链接是不通的
       if(client != null){
           client.close();
           client = null;
       }

       if(client == null) {
           try {
               client = new WebSocketClient(new URI(webSocketUrl), new Draft_17()) {
                   @Override
                   public void onOpen(ServerHandshake handshakedata) {
                       LogUtil.e(TAG + "lal-open", "已经连接到服务器【" + getURI() + "】");
                       callBack.onOpen(handshakedata);
                   }

                   @Override
                   public void onMessage(String message) {
                       LogUtil.e(TAG + "lal-message", "获取到服务器信息【" + message + "】");
                       callBack.onMessage(message);

                   }

                   @Override
                   public void onClose(int code, String reason, boolean remote) {
                       LogUtil.e(TAG + "lal-close", "断开服务器连接【" + getURI() + "，状态码： " + code + "，断开原因：" + reason + "】");
                      // callBack.onClose(code, reason, remote);
                   }

                   @Override
                   public void onError(Exception ex) {
                       LogUtil.e(TAG + "lal-error", "连接发生了异常【异常原因：" + ex + "】");
                   }
               };
           } catch (URISyntaxException e) {
               callBack.failure();
               LogUtil.e("--TAG--", e.toString());
               e.printStackTrace();
           }
       }
       client.connect();
   }

    /**
     * 关闭连接
     */
   public void close(){
       if(null != client){
           client.close();
       }
   }

   public void sendMessage(String msg)  throws WebsocketNotConnectedException {
       if(null != client){
           client.send(msg);
       }
   }
}
