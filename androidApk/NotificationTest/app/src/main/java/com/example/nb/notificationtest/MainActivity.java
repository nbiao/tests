package com.example.nb.notificationtest;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {
    private Button sendNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendNotice = (Button) findViewById(R.id.send_notice);
        sendNotice.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_notice:
                NotificationManager manager = (NotificationManager)
                        getSystemService(NOTIFICATION_SERVICE);

                Intent intent = new Intent(this, NotificationActivity.class);
                PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                long[] vibrates = {0, 1000, 1000, 1000};
                Notification notification = new Notification.Builder(this)
                        .setContentTitle("This is content title")
                        .setContentText("This is content text")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pi) //设置通知栏点击意图
                        .setLargeIcon(icon)
                        .setTicker("This is ticker text")
                        .setWhen(System.currentTimeMillis())//
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setVibrate(vibrates)
                        .getNotification();

                manager.notify(1, notification);
                break;
            default:
                break;
        }
    }
}

