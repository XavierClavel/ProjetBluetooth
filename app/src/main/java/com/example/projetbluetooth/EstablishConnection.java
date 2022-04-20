package com.example.projetbluetooth;

import android.Manifest;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import androidx.core.app.ActivityCompat;

public class EstablishConnection extends AsyncTask<String, Void, String> {
    public BluetoothSocket socket;

    public String doInBackground(String... id) {
        try {
            while (true) {
                //socket.connect();
            }
        } catch (Exception e) {

        }
        return "Connection established";
    }
}
