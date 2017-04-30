package com.sjsu.obdreader;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.github.pires.obd.commands.SpeedObdCommand;
import com.github.pires.obd.commands.control.DistanceTraveledSinceCodesClearedObdCommand;
import com.github.pires.obd.commands.engine.EngineFuelRate;
import com.github.pires.obd.commands.engine.EngineOilTempObdCommand;
import com.github.pires.obd.commands.engine.EngineRPMObdCommand;
import com.github.pires.obd.commands.engine.EngineRuntimeObdCommand;
import com.github.pires.obd.commands.fuel.FuelConsumptionRateObdCommand;
import com.github.pires.obd.commands.fuel.FuelLevelObdCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureObdCommand;
import com.github.pires.obd.exceptions.UnsupportedCommandException;
import com.sjsu.obdreader.db.VehicleDataSource;
import com.sjsu.obdreader.model.VehicleLog;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ObdGatewayService extends Service {

    private static final String TAG = ObdGatewayService.class.getName();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int LOCATION_INTERVAL = 30000;
    private static final float LOCATION_DISTANCE = 0f;
    public static boolean isConnected = false;
    public final String OBD_ADDRESS = "00:1D:A5:00:05:F7";
    private final IBinder binder = new MyBinder();
    boolean found = false;
    private Map<Class, ObdCommandJob> executedCommands;
    private VehicleDataSource dataSource;
    private BluetoothDevice dev = null;
    private BluetoothSocket socket;
    private BluetoothSocket sockFallback = null;
    private ArrayList<ObdCommandJob> obdCommands = new ArrayList();
    private Timer timer;
    private LocationManager locationManager;
    private boolean isRunning = false;
    private MyLocationListener[] locationListener = {new MyLocationListener(LocationManager.GPS_PROVIDER),
            new MyLocationListener(LocationManager.NETWORK_PROVIDER)};
    private Location location;
    private boolean isGpsEnabled = false;
    private boolean isNetworkEnabled = false;
    private MQTTHelper mqttHelper;

    public ObdGatewayService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting Service");
        dataSource = new VehicleDataSource(this);
        setCurrentLocation();
        if (isConnected) {
            mqttHelper = new MQTTHelper(getApplicationContext());
        }

        try {
            connectObdDevice();

            Log.i(TAG, "Service started");
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (found) {
                        Log.i(TAG, "VehicleLog Log");
                        VehicleLog vehicleLog = new VehicleLog();
                        try {
                            executeList();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (executedCommands.size() > 0) {
                            makeVehicle(vehicleLog);
                            if (isConnected) {
                                if (mqttHelper == null)
                                    mqttHelper = new MQTTHelper(getApplicationContext());
                                mqttHelper.publishData(vehicleLog);
                                dataSource.close();
                            } else {
                                saveData(vehicleLog);
                            }
                        }
                    }else{
                        try {
                            connectObdDevice();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 0, 10000);
        } catch (IOException e) {
            e.printStackTrace();
            stopService();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping Service");
        stopService();
        Log.i(TAG, "Service stopped");
    }

    private void connectObdDevice() throws IOException {
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        found = false;

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(OBD_ADDRESS)) {
                    found = true;
                } else {
                    found = false;
                }
            }
        }
        if (found) {
            dev = btAdapter.getRemoteDevice(OBD_ADDRESS);
            Log.d(TAG, "Stopping Bluetooth discovery.");
            btAdapter.cancelDiscovery();

            ConnectThread ct = new ConnectThread();
            ct.run();
        } else {
            Log.e(TAG, "No Bluetooth device has been selected.");
            stopService();
            throw new IOException();
        }
    }


    public void stopService() {
        Log.d(TAG, "Stopping service..");
        obdCommands.clear();
        isRunning = false;
        dataSource.close();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        if (locationManager != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    for (int i = 0; i < locationListener.length; i++) {
                        locationManager.removeUpdates(locationListener[i]);
                    }
                }

            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listeners, ignore", ex);
            }
        }

        stopSelf();
    }

    private void executeList() throws InterruptedException {
        Log.i(TAG, "Executing list...");
        executedCommands = new HashMap<>();

        Log.i(TAG, "List size is " + obdCommands.size());
        for (ObdCommandJob job : obdCommands) {
            try {
                job.getCommand().run(socket.getInputStream(), socket.getOutputStream());
                Log.d(TAG, "Job result" + job.getCommand().getFormattedResult());
            } catch (InterruptedException i) {
                Log.d(TAG, "InterruptedException -> " + i.getMessage());
                Thread.currentThread().interrupt();
            } catch (UnsupportedCommandException u) {
                if (job != null) {
                    job.setState(ObdCommandJobState.NOT_SUPPORTED);
                }
                Log.d(TAG, "Command not supported. -> " + u.getMessage());
            } catch (Exception e) {
                if (job != null) {
                    job.setState(ObdCommandJobState.EXECUTION_ERROR);
                }
                Log.e(TAG, "Failed to run command. -> " + e.getMessage());
            }

            if (job != null) {
                executedCommands.put(job.getCommand().getClass(), job);
            }
        }
    }

    private void makeVehicle(VehicleLog vehicleLog) {

        vehicleLog.setRpm(executedCommands.get(EngineRPMObdCommand.class).getCommand().getFormattedResult());
        vehicleLog.setSpeed(executedCommands.get(SpeedObdCommand.class).getCommand().getFormattedResult());
        vehicleLog.setTemperature(executedCommands.get(AmbientAirTemperatureObdCommand.class).getCommand().getFormattedResult());
        vehicleLog.setTimestamp(CommonUtil.getCurrentDate());
        vehicleLog.setFuelLevel(executedCommands.get(FuelLevelObdCommand.class).getCommand().getFormattedResult());
        vehicleLog.setMileage(executedCommands.get(FuelConsumptionRateObdCommand.class).getCommand().getFormattedResult());
        vehicleLog.setMiles(executedCommands.get(DistanceTraveledSinceCodesClearedObdCommand.class).getCommand().getFormattedResult());
        vehicleLog.setOilLevel(executedCommands.get(EngineOilTempObdCommand.class).getCommand().getFormattedResult());
        vehicleLog.setEngineCoolantTemp(executedCommands.get(EngineCoolantTemperatureObdCommand.class).getCommand().getFormattedResult());
        vehicleLog.setLongitude(location.getLongitude());
        vehicleLog.setLatitude(location.getLatitude());
        vehicleLog.setVehicleId(OBD_ADDRESS);
    }

    public void saveData(VehicleLog vehicleLog) {
        Log.i(TAG, "Saving data to sql lite");

        if (dataSource.insert(vehicleLog))
            Log.i(TAG, "VehicleLog data saved successfully");
        else
            Log.i(TAG, "VehicleLog data cannot be saved");

    }

    private void setCurrentLocation() {
        Log.i(TAG, "initializeLocationManager");
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Location not found");
        } else {
            try {

                isGpsEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);
                // getting network status
                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (isGpsEnabled) {

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                            locationListener[0]);
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                            locationListener[1]);
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            }
        }
    }

    public class MyBinder extends Binder {
        ObdGatewayService getService() {
            return ObdGatewayService.this;
        }
    }

    private class ConnectThread extends Thread {

        @Override
        public void run() {
            android.util.Log.d(TAG, "Starting OBD connection..");
            isRunning = true;
            try {
                socket = dev.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                isRunning = true;
            } catch (Exception e1) {
                Log.e(TAG, "There was an error while establishing Bluetooth connection. Falling back..", e1);
                try {
                    Class<?> clazz = socket.getRemoteDevice().getClass();
                    Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                    Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                    Object[] params = new Object[]{Integer.valueOf(1)};
                    sockFallback = (BluetoothSocket) m.invoke(socket.getRemoteDevice(), params);
                    sockFallback.connect();
                    socket = sockFallback;
                    isRunning = true;
                } catch (Exception e2) {
                    Log.e(TAG, "Couldn't fallback while establishing Bluetooth connection. Maybe bluetooth is turned off..", e2);
                    stopService();
                }
            }

            if (isRunning) {
                // Let's configure the connection.
                Log.d(TAG, "Adding jobs for connection configuration to list..");
//                obdCommands.add(new ObdCommandJob(new ObdResetCommand()));
//                obdCommands.add(new ObdCommandJob(new EchoOffObdCommand()));
//                obdCommands.add(new ObdCommandJob(new LineFeedOffObdCommand()));
//                obdCommands.add(new ObdCommandJob(new TimeoutObdCommand(62)));
//                final String protocol = "AUTO";
//                obdCommands.add(new ObdCommandJob(new SelectProtocolObdCommand(ObdProtocols.valueOf(protocol))));

                obdCommands.add(new ObdCommandJob(new AmbientAirTemperatureObdCommand()));
                obdCommands.add(new ObdCommandJob(new EngineRPMObdCommand()));
                obdCommands.add(new ObdCommandJob(new SpeedObdCommand()));
                obdCommands.add(new ObdCommandJob(new EngineCoolantTemperatureObdCommand()));
                obdCommands.add(new ObdCommandJob(new EngineOilTempObdCommand()));
                obdCommands.add(new ObdCommandJob(new FuelLevelObdCommand()));
                obdCommands.add(new ObdCommandJob(new DistanceTraveledSinceCodesClearedObdCommand()));
                obdCommands.add(new ObdCommandJob(new FuelConsumptionRateObdCommand()));
                obdCommands.add(new ObdCommandJob(new EngineRuntimeObdCommand()));
                obdCommands.add(new ObdCommandJob(new EngineFuelRate()));
                obdCommands.add(new ObdCommandJob(new EngineRuntimeObdCommand()));

                Log.d(TAG, "Initialization jobs added to list.");
            }
        }
    }

    private class MyLocationListener implements android.location.LocationListener {

        public MyLocationListener(String provider) {
            Log.i(TAG, "LocationListener " + provider);
            location = new Location(provider);
            Log.i(TAG, " Location is:" + (location.getLatitude() + " " + location.getLongitude()));
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            ObdGatewayService.this.location.set(location);
            ObdGatewayService.this.location = location;
            Log.i(TAG, " Location is:" + (location.getLatitude() + " " + location.getLongitude()));
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
}
