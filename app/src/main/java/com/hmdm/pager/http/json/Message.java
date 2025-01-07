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

package com.hmdm.pager.http.json;

import android.annotation.SuppressLint;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private long ts;
    private int id;
    private int serverId;
    private String text;
    private int status;

    public static final int STATUS_SENT = 0;
    public static final int STATUS_DELIVERED = 1;
    public static final int STATUS_READ = 2;

    private void initDefaults() {
        ts = System.currentTimeMillis();
        id = 0;
        serverId = 0;
        text = "";
        status = 0;
    }

    public Message() {
        initDefaults();
    }

    @SuppressLint("Range")
    public Message(Cursor cursor) {
        setId(cursor.getInt(cursor.getColumnIndex("_id")));
        setTs(cursor.getLong(cursor.getColumnIndex("ts")));
        setServerId(cursor.getInt(cursor.getColumnIndex("serverId")));
        setText(cursor.getString(cursor.getColumnIndex("message")));
        setStatus(cursor.getInt(cursor.getColumnIndex("status")));
    }

    public Message(JSONObject jsonObject) {
        initDefaults();
        try {
            serverId = jsonObject.getInt("id");
            text = jsonObject.getString("text");
        } catch (JSONException e) {
        }
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
