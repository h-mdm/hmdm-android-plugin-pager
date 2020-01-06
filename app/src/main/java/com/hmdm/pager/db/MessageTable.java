package com.hmdm.pager.db;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.hmdm.pager.http.json.Message;

import java.util.LinkedList;
import java.util.List;

public class MessageTable {
    private static final String CREATE_TABLE =
            "CREATE TABLE messages (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "ts INTEGER, " +
                    "serverId INTEGER, " +
                    "message TEXT, " +
                    "status INTEGER" +
                    ")";
    private static final String SELECT_MESSAGES =
            "SELECT * FROM messages ORDER BY ts DESC";
    private static final String SELECT_COUNT =
            "SELECT COUNT(*) FROM messages";
    private static final String SELECT_UNREAD_MESSAGES =
            "SELECT * FROM messages WHERE status != 2";
    private static final String INSERT_MESSAGE =
            "INSERT OR IGNORE INTO messages(ts, serverId, message, status) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_MESSAGE_STATUS =
            "UPDATE messages SET status = ? WHERE _id = ?";
    private static final String DELETE_FROM_MESSAGES =
            "DELETE FROM messages WHERE _id = ?";
    private static final String DELETE_ALL_MESSAGES =
            "DELETE FROM messages";

    public static String getCreateTableSql() {
        return CREATE_TABLE;
    }

    public static void insert(SQLiteDatabase db, Message item) {
        try {
            db.execSQL(INSERT_MESSAGE, new String[]{
                    Long.toString(System.currentTimeMillis()),
                    Integer.toString(item.getServerId()),
                    item.getText(),
                    Integer.toString(item.getStatus())
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateStatus(SQLiteDatabase db, Message item) {
        try {
            db.execSQL(UPDATE_MESSAGE_STATUS, new String[]{
                    Integer.toString(item.getId()),
                    Integer.toString(item.getStatus())
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllItems(SQLiteDatabase db) {
        try {
            db.execSQL(DELETE_ALL_MESSAGES, new String[]{});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void delete(SQLiteDatabase db, List<Message> items) {
        db.beginTransaction();
        try {
            for (Message item : items) {
                db.execSQL(DELETE_FROM_MESSAGES, new String[]{
                        Long.toString(item.getId())
                });
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public static int count(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery(SELECT_COUNT, new String[] {});

        boolean isDataNotEmpty = cursor.moveToFirst();
        if (isDataNotEmpty) {
            return cursor.getInt(0);
        }
        return 0;
    }

    public static List<Message> select(SQLiteDatabase db, boolean unread) {
        Cursor cursor = db.rawQuery(unread ? SELECT_UNREAD_MESSAGES : SELECT_MESSAGES, new String[] {});
        List<Message> result = new LinkedList<>();

        boolean isDataNotEmpty = cursor.moveToFirst();
        while (isDataNotEmpty) {
            Message item = new Message(cursor);
            result.add(item);

            isDataNotEmpty = cursor.moveToNext();
        }
        cursor.close();

        return result;
    }
}
