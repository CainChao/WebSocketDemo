package com.cainchao.websocketdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cainchao.websocketdemo.service.WebSocketCallEvent;
import com.cainchao.websocketdemo.service.WebSocketService;
import com.cainchao.websocketdemo.utils.NetworkDetectionUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private static final  String TAG = MainActivity.class.getName();

    private  AlertDialog.Builder titledialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        titledialog = new AlertDialog.Builder(this);

        Intent webSocketService  = new Intent(MainActivity.this,WebSocketService.class);
        startService(webSocketService);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WebSocketCallEvent event) {
        String msg = "eventBust" + event.getMsg();
        LogUtil.e(TAG+"---eventBust---",msg);

        if(NetworkDetectionUtils.checkNetWorkStatus(getApplicationContext()) == false){
            showTitledialog("网络出现异常，是否去设置网络");
            titledialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                    startActivity(intent);
                }
            });
            titledialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }else{
            showTitledialog("服务器网络出现异常，请尽快联系开发人员");
            titledialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        titledialog.create().show();
    }

    //显示提示框
    private void showTitledialog(String msg){
        titledialog.setTitle("提示");
        titledialog.setMessage(msg);

    }

}
