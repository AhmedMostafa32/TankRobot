package com.example.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DBNAME = "Login.db";
    public static final int DB_VERSION = 2; // Increment the version to trigger onUpgrade
    public static final String USERS_TABLE = "users";
    public static final String QRCODES_TABLE = "qrcodes";
    public static final String DATA = "data";

    public DBHelper(Context context) {
        super(context, DBNAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDB) {
        MyDB.execSQL("CREATE TABLE IF NOT EXISTS users (email_field TEXT PRIMARY KEY, pass_field TEXT, fullname_field TEXT)");
        MyDB.execSQL("CREATE TABLE IF NOT EXISTS qrcodes (data TEXT PRIMARY KEY)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int oldVersion, int newVersion) {
        MyDB.execSQL("DROP TABLE IF EXISTS users");
        MyDB.execSQL("DROP TABLE IF EXISTS qrcodes");
        onCreate(MyDB);
    }

    public boolean insertData(String email_field, String pass_field, String confpass_field, String fullname_field) {
        if (!pass_field.equals(confpass_field)) {
            return false; // Passwords don't match
        }

        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email_field", email_field);
        contentValues.put("pass_field", pass_field);
        contentValues.put("fullname_field", fullname_field);
        long result = MyDB.insert("users", null, contentValues);
        return result != -1;
    }

    public boolean checkUserEmail(String email_field) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("SELECT * FROM users WHERE email_field = ?", new String[]{email_field});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkUsernameEmailPassword(String pass_field, String email_field) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("SELECT * FROM users WHERE pass_field = ? AND email_field = ?", new String[]{pass_field, email_field});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean insertQRCode(String data) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("data", data);
        long result = MyDB.insert("qrcodes", null, contentValues);
        return result != -1;
    }

    public boolean isQRCodeExist(String data) {
        SQLiteDatabase MyDB = this.getReadableDatabase();
        Cursor cursor = MyDB.rawQuery("SELECT * FROM qrcodes WHERE data = ?", new String[]{data});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Cursor getAllQRCodes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM qrcodes", null);
    }

    public boolean removeQRCode(String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(QRCODES_TABLE, DATA + " = ?", new String[]{data});
        return result > 0;
    }
}