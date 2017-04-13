package com.sjsu.obdreader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sjsu.obdreader.model.Vehicle;

/**
 * Created by pavanibaradi on 4/5/17.
 */
public class VehicleDataSource {

    private static final String TAG = VehicleDataSource.class.getName();

    private SQLiteDatabase database;
    private FeedReaderDbHelper dbHelper;

    public VehicleDataSource(Context context) {
        dbHelper = new FeedReaderDbHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean insert(Vehicle vehicle) {

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_RPM, vehicle.getRpm());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SPEED, vehicle.getSpeed());
        database.beginTransaction();

        long newRowId = database.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);

        database.setTransactionSuccessful();
        database.endTransaction();

        Log.i(TAG, "vehicle id" + newRowId);

        if (newRowId > 0)
            return true;
        else
            return false;
    }

}
