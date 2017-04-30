package com.sjsu.obdreader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by pavanibaradi on 4/5/17.
 */
public class FeedReaderDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedReaderContract.FeedEntry.TABLE_NAME + " (" +
                    FeedReaderContract.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_RPM + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_SPEED + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_LATITUDE + " REAL," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_LONGITUDE + " REAL," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_ENG_COOL_TEMP + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_FUEL_LEVEL + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_MILEAGE + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_MILES + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_OIL_LEVEL + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_TEMP + " DATETIME," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_TIRE_PRESSURE + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_VEHICLE_ID + " TEXT" +
                    ")";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedReaderContract.FeedEntry.TABLE_NAME;
    private SQLiteDatabase database;


    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public SQLiteDatabase getWriteDatabase() {
        if (database == null)
            database = getWritableDatabase();
        return database;
    }

    public SQLiteDatabase getReadDatabase(){
        database = getReadableDatabase();
        return database;
    }

    @Override
    public synchronized void close() {
        super.close();
        if (database != null)
            database.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }


}
