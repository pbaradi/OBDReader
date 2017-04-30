package com.sjsu.obdreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.sjsu.obdreader.db.VehicleDataSource;
import com.sjsu.obdreader.model.VehicleLog;

import java.util.List;

public class NetworkBroadcastReceiver extends BroadcastReceiver {
    VehicleDataSource vehicleDataSource;
    MQTTHelper helper;


    public NetworkBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        Log.i("NETWORK", "onReceive");
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetInfo != null && activeNetInfo.isConnectedOrConnecting()) {
            ObdGatewayService.isConnected = true;
            helper = new MQTTHelper(context);
            vehicleDataSource = new VehicleDataSource(context);
//            vehicleDataSource.open();

            List<VehicleLog> vLog = vehicleDataSource.getVehicleLog();
            for (VehicleLog vehicle : vLog) {
                helper.publishData(vehicle);
            }
            vehicleDataSource.deleteData();
//            List<VehicleLog> vLogs = vehicleDataSource.getVehicleLog();
            Log.i("NET", "connected" + ObdGatewayService.isConnected);
        } else {
            ObdGatewayService.isConnected = false;
            if (vehicleDataSource != null)
                vehicleDataSource.close();
            Log.i("NET", "not connected" + ObdGatewayService.isConnected);
        }
    }
}
