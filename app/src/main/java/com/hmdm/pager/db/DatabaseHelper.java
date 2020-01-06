package com.hmdm.pager.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "hmdm.pager.sqlite";

    private static DatabaseHelper sInstance;

    private DatabaseHelper( Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper instance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(MessageTable.getCreateTableSql());
            db.setTransactionSuccessful();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
/*        db.beginTransaction();
        try {
            if (oldVersion < 2 && newVersion >= 2) {
                ...
            }
            db.setTransactionSuccessful();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
        } */
    }
}

