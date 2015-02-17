package org.mariotaku.twidere.extension.push.xiaomi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 1、PushMessageReceiver是个抽象类，该类继承了BroadcastReceiver。
 * 2、需要将自定义的DemoMessageReceiver注册在AndroidManifest.xml文件中 <receiver
 * android:exported="true"
 * android:name="org.mariotaku.twidere.extension.push.xiaomi.DemoMessageReceiver"> <intent-filter>
 * <action android:name="com.xiaomi.mipush.RECEIVE_MESSAGE" /> </intent-filter>
 * <intent-filter> <action android:name="com.xiaomi.mipush.ERROR" />
 * </intent-filter> </receiver>
 * 3、DemoMessageReceiver的onCommandResult方法用来接收客户端向服务器发送命令后的响应结果
 * 4、DemoMessageReceiver的onReceiveMessage方法用来接收服务器向客户端发送的消息
 * 5、onReceiveMessage和onCommandResult方法运行在非UI线程中
 *
 * @author wangkuiwei
 */
public class DemoMessageReceiver extends PushMessageReceiver {

    @Override
    public void onReceiveMessage(Context context, MiPushMessage message) {
        Log.v(DemoApplication.TAG,
                "onReceiveMessage is called. " + message.toString());
        String log = context.getString(R.string.recv_message, message.getContent());

        Message msg = Message.obtain();
        if (message.isNotified()) {
            msg.obj = log;
        }
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
    }

    @SuppressLint("SimpleDateFormat")
    public static String getSimpleDate() {
        return new SimpleDateFormat("MM-dd hh:mm:ss").format(new Date());
    }

}
