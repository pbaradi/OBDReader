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
    }

}
