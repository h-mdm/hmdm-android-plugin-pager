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

package com.hmdm.pager.receiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hmdm.MDMPushHandler;
import com.hmdm.MDMPushMessage;
import com.hmdm.MDMService;
import com.hmdm.pager.Const;
import com.hmdm.pager.R;
import com.hmdm.pager.db.DatabaseHelper;
import com.hmdm.pager.db.MessageTable;
import com.hmdm.pager.http.json.Message;
import com.hmdm.pager.service.PagerService;
import com.hmdm.pager.task.UpdateStatusTask;

import org.json.JSONObject;

public class MessageReceiver extends MDMPushHandler {
    private Context context;
    public MessageReceiver(Context context) {
        this.context = context;
    }

    private void notifyMessageDelivery(Message msg, Context context) {
        UpdateStatusTask updateStatusTask = new UpdateStatusTask(context);
        updateStatusTask.execute(msg);
    }

    private void notifyOnIncomingMessage(Message msg, Context context) {
        try {
            Intent contentIntent = new Intent(context.getApplicationContext(), StartActivityReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, contentIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Channel is already created when the pager service is bound
                builder = new NotificationCompat.Builder(context.getApplicationContext(), PagerService.CHANNEL_ID);
            } else {
                builder = new NotificationCompat.Builder(context.getApplicationContext());
            }

            @SuppressLint("NotificationTrampoline") Notification notification = builder
                    .setContentIntent(pendingIntent)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setTicker(context.getString(R.string.app_name))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setAutoCancel(true)
                    .setContentText(msg.getText())
                    .setSmallIcon( R.drawable.ic_notification ).build();

            notificationManager.notify(0, notification);
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMessageReceived(MDMPushMessage mdmPushMessage) {
        PowerManager pm = ( PowerManager )context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "com.hmdm.pager:wakelock" );
        wl.acquire();

        JSONObject rawMsg = mdmPushMessage.getData();
        Message msg = new Message(rawMsg);
        MDMService.Log.i(Const.LOG_TAG, "got message: " + msg.getText());
        msg.setStatus(Message.STATUS_DELIVERED);
        MessageTable.insert(DatabaseHelper.instance(context.getApplicationContext()).getWritableDatabase(), msg);
        notifyMessageDelivery(msg, context);
        notifyOnIncomingMessage(msg, context);
        LocalBroadcastManager.getInstance(context).
                sendBroadcast(new Intent(Const.ACTION_NEW_MESSAGE));

        wl.release();
    }
}
