package com.sjsu.obdreader.db;

import android.provider.BaseColumns;

/**
 * Created by pavanibaradi on 4/5/17.
 */
public class FeedReaderContract {
    private FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "vehicle";
        public static final String COLUMN_NAME_RPM = "rpm";
        public static final String COLUMN_NAME_SPEED = "speed";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_VEHICLE_ID = "vehicleId";
        public static final String COLUMN_NAME_FUEL_LEVEL = "fuelLevel";
        public static final String COLUMN_NAME_OIL_LEVEL = "oilLevel";
        public static final String COLUMN_NAME_TEMP = "temperature";
        public static final String COLUMN_NAME_MILES = "miles";
        public static final String COLUMN_NAME_MILEAGE = "mileage";
        public static final String COLUMN_NAME_TIRE_PRESSURE = "tirePressure";
        public static final String COLUMN_NAME_ENG_COOL_TEMP = "engineCoolantTemp";
    }

}
