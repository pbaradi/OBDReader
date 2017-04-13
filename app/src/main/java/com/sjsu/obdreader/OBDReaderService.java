package com.sjsu.obdreader;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.github.pires.obd.commands.SpeedObdCommand;
import com.github.pires.obd.commands.engine.EngineRPMObdCommand;
import com.github.pires.obd.commands.protocol.EchoOffObdCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffObdCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolObdCommand;
import com.github.pires.obd.commands.protocol.TimeoutObdCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.UUID;


public class OBDReaderService extends Service {

    private final IBinder binder = new MyBinder();
    public final String OBD_ADDRESS = "00:1D:A5:00:05:F7";
    BluetoothSocket socket;

    public class  MyBinder extends Binder {
        OBDReaderService getService() {
            return OBDReaderService.this;
        }
    }

    public OBDReaderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("OBDReaderService","Starting Service");
        new OBDConnectThread().run();
        Log.i("OBDReaderService","Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("OBDReaderService","Stopping Service");
        stopService();
        Log.i("OBDReaderService", "Service stopped");
    }

    private void connectOBDDevice(){
        Log.i("OBDReaderService","Connecting to OBD Device");

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothDevice device = btAdapter.getRemoteDevice(OBD_ADDRESS);

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            if(socket == null)
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();
            btAdapter.cancelDiscovery();
            Log.i("OBDReaderService", "OBD Device Connected");
        } catch (IOException e) {
            e.printStackTrace();
            stopService();
        }
    }

    private void initializeOBDAdapter(){
        Log.i("OBDReaderService","initializing OBD Adapter");
        try {
            new EchoOffObdCommand().run(socket.getInputStream(), socket.getOutputStream());

            new LineFeedOffObdCommand().run(socket.getInputStream(), socket.getOutputStream());

            new TimeoutObdCommand(20).run(socket.getInputStream(), socket.getOutputStream());

            new SelectProtocolObdCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());

            Log.i("OBDReaderService", "OBD Adapter initialized");
        }catch (Exception e) {
            e.printStackTrace();
            stopService();
        }
    }

    private void getDeviceData(){
        EngineRPMObdCommand engineRpmCommand = new EngineRPMObdCommand();
        SpeedObdCommand speedCommand = new SpeedObdCommand();
        while (!Thread.currentThread().isInterrupted())
        {
            try {
                engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                // TODO handle commands result
                Log.d("OBDReaderService", "RPM: " + engineRpmCommand.getFormattedResult());
                Log.d("OBDReaderService", "Speed: " + speedCommand.getFormattedResult());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private class OBDConnectThread extends Thread {
        @Override
        public void run() {
            connectOBDDevice();
            initializeOBDAdapter();
            getDeviceData();
        }
    }

    public void stopService() {
        Log.d("OBDReaderService", "Stopping service..");
        if (socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("OBDReaderService", e.getMessage());
            }
        stopSelf();
    }
}
