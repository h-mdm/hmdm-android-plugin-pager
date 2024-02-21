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

package com.hmdm.pager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hmdm.MDMException;
import com.hmdm.MDMService;
import com.hmdm.pager.db.DatabaseHelper;
import com.hmdm.pager.db.MessageTable;
import com.hmdm.pager.http.json.Message;
import com.hmdm.pager.service.PagerService;
import com.hmdm.pager.task.UpdateStatusTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MDMService.ResultHandler {

    private SettingsHelper settings;

    private MDMService mdmService;
    private boolean mdmConnected = false;

    private TextView emptyTextView;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Const.ACTION_NEW_MESSAGE)) {
                adapter.updateMessages();
                adapter.notifyDataSetChanged();
                updateItemState();
                notifyMessagesRead();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emptyTextView = findViewById(R.id.empty_view);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MessageAdapter(this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        settings = SettingsHelper.getInstance(this);
        mdmService = MDMService.getInstance();

        Intent intent = new Intent(this, PagerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_purge) {
            MessageTable.deleteAllItems(DatabaseHelper.instance(this).getWritableDatabase());
            Toast.makeText(this, R.string.messages_removed, Toast.LENGTH_LONG).show();
            updateItemState();
            adapter.updateMessages();
            adapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mdmConnected) {
            queryMdm();
        } else {
            mdmService.connect(this, this);
        }
        updateItemState();

        adapter.updateMessages();
        adapter.notifyDataSetChanged();

        LocalBroadcastManager.getInstance(this).registerReceiver(newMessageReceiver,
                new IntentFilter(Const.ACTION_NEW_MESSAGE));

        notifyMessagesRead();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(newMessageReceiver);
    }

    private void updateItemState() {
        if (MessageTable.count(DatabaseHelper.instance(this).getReadableDatabase()) > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    private void notifyMessagesRead() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                List<Message> unreadMessages = MessageTable.select(
                        DatabaseHelper.instance(MainActivity.this).getReadableDatabase(), true);
                for (Message message : unreadMessages) {
                    message.setStatus(Message.STATUS_READ);
                    if (UpdateStatusTask.updateStatus(message, MainActivity.this) == Const.TASK_SUCCESS) {
                        MessageTable.updateStatus(
                                DatabaseHelper.instance(MainActivity.this).getWritableDatabase(), message);
                    }
                }

                return null;
            }
        }.execute();
    }

    private void queryMdm() {
        Bundle data = null;
        try {
            data = MDMService.getInstance().queryConfig();
        } catch (MDMException e) {
            Toast.makeText(this, R.string.mdm_connect_error, Toast.LENGTH_LONG).show();
            return;
        }

        String[] keys = {
                MDMService.KEY_SERVER_HOST,
                MDMService.KEY_SECONDARY_SERVER_HOST,
                MDMService.KEY_SERVER_PATH,
                MDMService.KEY_DEVICE_ID
        };

        for (String key : keys) {
            String value = data.getString(key);
            settings.setString(key, value);
        }
    }

    @Override
    public void onMDMConnected() {
        mdmConnected = true;
        MDMService.Log.i(Const.LOG_TAG, "activity connected to Headwind MDM");

        queryMdm();
    }

    @Override
    public void onMDMDisconnected() {
        mdmConnected = false;

        // Reconnect (this could be after crash of Headwind MDM!)
        MDMService.Log.i(Const.LOG_TAG, "activity disconnected from Headwind MDM");
        new Handler().postDelayed(new MDMReconnectRunnable(), Const.HMDM_RECONNECT_DELAY_FIRST);
    }

    public class MDMReconnectRunnable implements Runnable {
        @Override
        public void run() {
            if (!mdmService.connect(MainActivity.this, MainActivity.this)) {
                // Retry in 1 minute
                MDMService.Log.i(Const.LOG_TAG, "Failed to connect to Headwind MDM, scheduling connection");
                new Handler().postDelayed(this, Const.HMDM_RECONNECT_DELAY_NEXT);
            }
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewDate;
        public TextView textViewMessage;
        public MessageViewHolder(View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
        }
    }

    public static class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
        private List<Message> messages;
        private Context context;

        private DateFormat timeFormat = new SimpleDateFormat( "dd.MM.yy HH:mm" );

        public MessageAdapter(Context context) {
            this.context = context;
            updateMessages();
        }

        public void updateMessages() {
            messages = MessageTable.select(DatabaseHelper.instance(context.getApplicationContext()).getReadableDatabase(), false);
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_message_item, parent, false);
            MessageViewHolder vh = new MessageViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            Message message = messages.get(position);
            holder.textViewDate.setText(timeFormat.format(new Date(message.getTs())));
            holder.textViewMessage.setText(message.getText());
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }
}
