package com.cainchao.websocketdemo.http;

import org.java_websocket.handshake.ServerHandshake;

/**
 * Author YC
 * 2017/6/28 0028.
 */

public interface IWebSocketCallBack {
    /**
     * 打开连接
     * @param handshakedata
     */
    void onOpen(ServerHandshake handshakedata);

    /**
     * 获取消息
     * @param message
     */
    void onMessage(String message);

    /**
     * 关闭连接
     * @param code
     * @param reason
     * @param remote
     */
    void onClose(int code, String reason, boolean remote);

    /**
     * 连接发送错误
     * @param ex
     */
    void onError(Exception ex);

    /**
     * 连接发送异常
     */
    void failure();

//    void sendMessage(String msg);
}
