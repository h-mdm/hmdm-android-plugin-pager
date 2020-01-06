package com.hmdm.pager.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hmdm.MDMService;
import com.hmdm.pager.Const;
import com.hmdm.pager.receiver.MessageReceiver;
import com.hmdm.pager.R;
import com.hmdm.pager.SettingsHelper;

public class PagerService extends Service implements MDMService.ResultHandler {

    private SettingsHelper settings;
    private MDMService mdmService;
    private MessageReceiver messageReceiver;

    public static final int NOTIFICATION_ID = 111;
    public static final String CHANNEL_ID = "com.hmdm.pager";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onMDMConnected() {
        // Great, we're connected!
        MDMService.Log.i(Const.LOG_TAG, "service connected to Headwind MDM");
    }

    @Override
    public void onMDMDisconnected() {
        // Reconnect (this could be after crash of Headwind MDM!)
        MDMService.Log.i(Const.LOG_TAG, "service disconnected from Headwind MDM");
        new Handler().postDelayed(new MDMReconnectRunnable(), Const.HMDM_RECONNECT_DELAY_FIRST);
    }

    public class MDMReconnectRunnable implements Runnable {
        @Override
        public void run() {
            if (!mdmService.connect(PagerService.this, PagerService.this)) {
                // Retry in 1 minute
                MDMService.Log.i(Const.LOG_TAG, "Failed to connect to Headwind MDM, scheduling connection");
                new Handler().postDelayed(this, Const.HMDM_RECONNECT_DELAY_NEXT);
            }
        }
    }

    @Override
    public void onCreate() {
        mdmService = MDMService.getInstance();
        mdmService.connect(this, this);
        startAsForeground();

        messageReceiver = new MessageReceiver();
        IntentFilter intentFilter = new IntentFilter(Const.INTENT_PUSH_NOTIFICATION_TYPE);
        if(intentFilter != null)
        {
            registerReceiver(messageReceiver, intentFilter);
        }
    }

    private void startAsForeground() {
        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder( this );
        }
        Notification notification = builder
                .setContentTitle( getString( R.string.app_name ) )
                .setTicker( getString( R.string.app_name ) )
                .setContentText( getString( R.string.notification_text ) )
                .setSmallIcon( R.drawable.ic_service ).build();

        startForeground(NOTIFICATION_ID, notification );
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId) {
        settings = SettingsHelper.getInstance(this);

        MDMService.Log.i(Const.LOG_TAG, "PagerService started");

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (messageReceiver != null) {
            unregisterReceiver(messageReceiver);
        }
        super.onDestroy();
    }

}
