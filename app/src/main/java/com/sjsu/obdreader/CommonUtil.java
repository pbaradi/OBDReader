package com.sjsu.obdreader;


import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by pavanibaradi on 4/6/17.
 */
public class CommonUtil {

    public static String getCurrentDate(){
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        return formattedDate;
    }

}
