package com.cainchao.websocketdemo.service;

/**
 * Author CaiChao
 * 2017/7/10 0010.
 */

public class WebSocketCallEvent {
    private int mMsg;

    public  WebSocketCallEvent(int mMsg){
        this.mMsg = mMsg;
    }

    public int getMsg() {
        return mMsg;
    }
}
