package com.sjsu.obdreader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sjsu.obdreader.model.VehicleLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pavanibaradi on 4/5/17.
 */
public class VehicleDataSource {

    private static final String TAG = VehicleDataSource.class.getName();
    private static final String SQL_FETCH_VEHICLE = "select * from " + FeedReaderContract.FeedEntry.TABLE_NAME;
    private static final String SQL_DELETE_VEHICLE_LOG = "delete from " + FeedReaderContract.FeedEntry.TABLE_NAME;
    private SQLiteDatabase database;
    private FeedReaderDbHelper dbHelper;
    private Cursor c;

    public VehicleDataSource(Context context) {
        dbHelper = new FeedReaderDbHelper(context);
    }

    public void close() {
        dbHelper.close();
        database = null;
    }

    public boolean insert(VehicleLog vehicleLog) {
        if (database == null)
            database = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_RPM, vehicleLog.getRpm());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SPEED, vehicleLog.getSpeed());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_LATITUDE, vehicleLog.getLatitude());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_LONGITUDE, vehicleLog.getLongitude());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_ENG_COOL_TEMP, vehicleLog.getEngineCoolantTemp());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_FUEL_LEVEL, vehicleLog.getFuelLevel());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_MILEAGE, vehicleLog.getMileage());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_MILES, vehicleLog.getMiles());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_OIL_LEVEL, vehicleLog.getOilLevel());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TEMP, vehicleLog.getTemperature());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP, vehicleLog.getTimestamp());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TIRE_PRESSURE, vehicleLog.getTirePressure());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_VEHICLE_ID, vehicleLog.getVehicleId());
        database.beginTransaction();

        long newRowId = database.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);

        database.setTransactionSuccessful();
        database.endTransaction();

        Log.i(TAG, "vehicleLog id" + newRowId);

        if (newRowId > 0)
            return true;
        else
            return false;
    }

    public List<VehicleLog> getVehicleLog() {
        List<VehicleLog> vLog = new ArrayList<>();
        if (database == null)
           database = dbHelper.getReadableDatabase();
        database.beginTransaction();
        c = database.rawQuery(SQL_FETCH_VEHICLE, null);
        c.moveToFirst();
        while (!c.isLast()) {
            VehicleLog v = new VehicleLog(c.getString(1),c.getString(2),c.getDouble(3),c.getDouble(4), c.getString(5), c.getString(6), c.getString(7), c.getString(8), c.getString(9),c.getString(10),c.getString(11),c.getString(12), c.getString(13));
            vLog.add(v);
            c.moveToNext();
        }
        if(c.isLast()) {
            VehicleLog v = new VehicleLog(c.getString(1),c.getString(2),c.getDouble(3),c.getDouble(4), c.getString(5), c.getString(6), c.getString(7), c.getString(8), c.getString(9),c.getString(10),c.getString(11),c.getString(12), c.getString(13));
            vLog.add(v);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        c.close();
        close();
        return vLog;
    }

    public void deleteData() {
        if (database == null)
            database = dbHelper.getWritableDatabase();
        database.beginTransaction();
        database.execSQL(SQL_DELETE_VEHICLE_LOG);
        database.setTransactionSuccessful();
        database.endTransaction();
        close();
    }


}
