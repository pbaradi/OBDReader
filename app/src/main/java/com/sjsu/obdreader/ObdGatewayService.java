package com.sjsu.obdreader;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.github.pires.obd.commands.SpeedObdCommand;
import com.github.pires.obd.commands.control.DistanceTraveledSinceCodesClearedObdCommand;
import com.github.pires.obd.commands.engine.EngineOilTempObdCommand;
import com.github.pires.obd.commands.engine.EngineRPMObdCommand;
import com.github.pires.obd.commands.fuel.FuelConsumptionRateObdCommand;
import com.github.pires.obd.commands.fuel.FuelLevelObdCommand;
import com.github.pires.obd.commands.protocol.EchoOffObdCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffObdCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolObdCommand;
import com.github.pires.obd.commands.protocol.TimeoutObdCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureObdCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.UnsupportedCommandException;
import com.sjsu.obdreader.db.VehicleDataSource;
import com.sjsu.obdreader.model.Vehicle;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ObdGatewayService extends Service {
    private static final String TAG = ObdGatewayService.class.getName();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public final String OBD_ADDRESS = "00:1D:A5:00:05:F7";
    private final IBinder binder = new MyBinder();
    private Map<Class, ObdCommandJob> executedCommands = new HashMap<>();
    private VehicleDataSource dataSource;
    private BluetoothDevice dev = null;
    private BluetoothSocket socket;
    private BluetoothSocket sockFallback = null;
    private ArrayList<ObdCommandJob> obdCommands = new ArrayList();
//    private Thread t = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            try {
//                Log.i(TAG, "executing list");
//                executeList();
//            } catch (InterruptedException e) {
//                t.interrupt();
//            }
//        }
//    });
    private boolean isRunning = false;
    private Long queueCounter = 0L;


    public ObdGatewayService() {
    }

    public boolean isRunning() {
        return isRunning;
    }

    protected synchronized void putResult(Class commandClass, ObdCommandJob commandInstance) {
        executedCommands.put(commandClass, commandInstance);
    }

    public synchronized ObdCommandJob getResult(Class commandClass) {
        return executedCommands.get(commandClass);
    }

    public void add(ObdCommandJob job) {
        if (!isRunning) return;

        queueCounter++;
        Log.d(TAG, "Adding job[" + queueCounter + "] to queue..");

        job.setId(queueCounter);
        obdCommands.add(job);
        Log.d(TAG, "Job queued successfully.");

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
        dataSource.open();
        try {
            connectObdDevice();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Service started");
        //t.start();
        VehicleLogThread vt = new VehicleLogThread();
        vt.start();
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
        boolean found = false;

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(OBD_ADDRESS)) {
                    found = true;
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
        if (socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        stopSelf();
    }


    public BluetoothSocket getSocket() {
        return socket;
    }

    private void executeList() throws InterruptedException {
        Log.i(TAG, "Executing list...");

        Log.i(TAG, "List size is " + obdCommands.size());
        for (ObdCommandJob job : obdCommands) {

            try {
                if (job.getState().equals(ObdCommandJobState.NEW)) {
                    job.setState(ObdCommandJobState.RUNNING);
                    job.getCommand().run(socket.getInputStream(), socket.getOutputStream());
                    Log.d(TAG, "Job result" + job.getCommand().getFormattedResult());
                } else
                    Log.e(TAG, "Job state was not new, so it shouldn't be in list. BUG ALERT!");
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
                putResult(job.getCommand().getClass(), job);
            }
        }


    }

    private void makeVehicle(Vehicle vehicle) {
        LocationService locationService = new LocationService();
        vehicle.setRpm(getResult(EngineRPMObdCommand.class).getCommand().getFormattedResult());
        vehicle.setSpeed(getResult(SpeedObdCommand.class).getCommand().getFormattedResult());
        vehicle.setTemperature(getResult(AmbientAirTemperatureObdCommand.class).getCommand().getFormattedResult());
        vehicle.setTimestamp(CommonUtil.getCurrentDate());
        vehicle.setFuelLevel(getResult(FuelLevelObdCommand.class).getCommand().getFormattedResult());
        vehicle.setMileage(getResult(FuelConsumptionRateObdCommand.class).getCommand().getFormattedResult());
        vehicle.setMiles(getResult(DistanceTraveledSinceCodesClearedObdCommand.class).getCommand().getFormattedResult());
        vehicle.setOilLevel(getResult(EngineOilTempObdCommand.class).getCommand().getFormattedResult());
        vehicle.setEngineCoolantTemp(getResult(EngineCoolantTemperatureObdCommand.class).getCommand().getFormattedResult());
        setCurrentLocation(vehicle);
//        vehicle.setLongitude(locationService.getLongitude());
//        vehicle.setLatitude(locationService.getLatitude());
    }

    public void saveData(Vehicle vehicle) {
        Log.i(TAG, "Saving data to sql lite");

        if (dataSource.insert(vehicle))
            Log.i(TAG, "Vehicle data saved successfully");
        else
            Log.i(TAG, "Vehicle data cannot be saved");

    }

    private void setCurrentLocation(Vehicle vehicle) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationService locationListener = new LocationService();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Location not found");
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        vehicle.setLatitude(locationListener.getLatitude());
        vehicle.setLongitude(locationListener.getLongitude());
    }

    public class MyBinder extends Binder {
        ObdGatewayService getService() {
            return ObdGatewayService.this;
        }
    }

    private class VehicleLogThread extends Thread{
        @Override
        public void run() {
            Log.i(TAG,"Vehicle Log");
            Vehicle vehicle = new Vehicle();
            try {
                executeList();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (executedCommands.size() > 0) {
                makeVehicle(vehicle);
                saveData(vehicle);
            }
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
                add(new ObdCommandJob(new ObdResetCommand()));
                add(new ObdCommandJob(new EchoOffObdCommand()));
                add(new ObdCommandJob(new LineFeedOffObdCommand()));
                add(new ObdCommandJob(new TimeoutObdCommand(62)));
                final String protocol = "AUTO";
                add(new ObdCommandJob(new SelectProtocolObdCommand(ObdProtocols.valueOf(protocol))));

                add(new ObdCommandJob(new AmbientAirTemperatureObdCommand()));
                add(new ObdCommandJob(new EngineRPMObdCommand()));
                add(new ObdCommandJob(new SpeedObdCommand()));
                add(new ObdCommandJob(new EngineCoolantTemperatureObdCommand()));
                add(new ObdCommandJob(new EngineOilTempObdCommand()));
                add(new ObdCommandJob(new FuelLevelObdCommand()));
                add(new ObdCommandJob(new DistanceTraveledSinceCodesClearedObdCommand()));
                add(new ObdCommandJob(new FuelConsumptionRateObdCommand()));

                Log.d(TAG, "Initialization jobs added to list.");
            }
        }
    }
}
