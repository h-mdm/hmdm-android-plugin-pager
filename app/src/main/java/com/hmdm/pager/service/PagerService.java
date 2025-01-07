/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2020 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmdm.pager.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hmdm.MDMService;
import com.hmdm.pager.Const;
import com.hmdm.pager.R;
import com.hmdm.pager.SettingsHelper;
import com.hmdm.pager.receiver.MessageReceiver;

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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        mdmService = MDMService.getInstance();
        mdmService.connect(this, this);
        startAsForeground();

        messageReceiver = new MessageReceiver(this);

        String[] messageTypes = {Const.PUSH_MESSAGE_TYPE};
        messageReceiver.register(messageTypes, this);
    }

    private void startAsForeground() {
        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext());
        }
        Notification notification = builder
                .setContentTitle( getString( R.string.app_name ) )
                .setTicker( getString( R.string.app_name ) )
                .setContentText( getString( R.string.notification_text ) )
                .setSmallIcon( R.drawable.ic_service ).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
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
            messageReceiver.unregister(this);
            messageReceiver = null;
        }
        super.onDestroy();
    }

}
