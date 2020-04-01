package com.example.save_location;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "locationtracking";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "location";

    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";

    public DatabaseHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_students_table = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY, %s TEXT, %s REAL, %s REAL)", TABLE_NAME, KEY_ID, KEY_TIME, KEY_LONGITUDE, KEY_LATITUDE);
        db.execSQL(create_students_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, location.getTime());
        values.put(KEY_LONGITUDE, location.getLongitude());
        values.put(KEY_LATITUDE, location.getLatitude());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<Location> getAllLocations() {
        List<Location>  studentList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        while(cursor.isAfterLast() == false) {
            Location student = new Location( cursor.getString(1), cursor.getFloat(2), cursor.getFloat(3));
            studentList.add(student);
            cursor.moveToNext();
        }
        return studentList;
    }

    public void deleteAllLocation() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }
}
