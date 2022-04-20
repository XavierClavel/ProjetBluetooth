package com.example.projetbluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class ClientActivity extends AppCompatActivity {
    BluetoothSocket client_socket;
    BluetoothDevice device = null;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        Log.d("Connection", "start");
        ConnectBluetooth();
        //connect.execute();

        //new Thread(r).start();
    }

    void ConnectBluetooth() {

        UUID uuid = UUID.fromString("f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d("Connection", "Refused");
            //return;
        }
        Log.d("Connection", "Looking for devices");
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        //TODO : case where no device is bonded => TOAST
        Log.d("Connection", "Got devices");
        Log.d("Connection", Integer.toString(bondedDevices.size()));
        for (BluetoothDevice deviceIterator : bondedDevices) {
            Log.d("Connection", deviceIterator.getName());
            switch (deviceIterator.getName()) {
                case "OPPO Reno Z" :
                    device = deviceIterator;
                    Log.d("Connection", "Device was found !");
                    break;

                case "ProjetBluetooth" :
                    device = deviceIterator;
                    Log.d("Connection", "Device was found !");
                    break;

                default:
                    Log.d("Connection", deviceIterator.getName());

            }
        }

        if (device != null) {
            try {
                client_socket = device.createRfcommSocketToServiceRecord(uuid);
                Log.d("Connection", "Creation of RF comm socket succeeded");
                connect connection = new connect();
                connection.doInBackground();
            } catch (Exception e) {
                Log.d("Connection", "Creation of RF comm socket failed");
            }
        }
    }

    void Dialogue()
    {
        Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        Log.d("client thread", "thread running");
                    }
                } catch (Exception e) {
                    Log.d("thread", "thread failed");
                }
            }
        });
    }
/*
    //Thread utilisé pour l'envoi et la réception des messages
    final int MSG_CALCUL = 1;
    Runnable r = new Runnable() {
      public void run() {
          while (true) {
              try {
                  Thread.sleep(1000);
                  break;
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
          String messageString = "Client Connected";
          Message message = mHandler.obtainMessage(MSG_CALCUL, (Object) messageString);
          mHandler.sendMessage(message);
      }
    };

    /*public AsyncTask connect = new AsyncTask() {
        @Override
        protected Object doInBackground(Object[] objects) {
            while (true) {
                try {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        client_socket.connect();
                        if (client_socket.isConnected()) {
                            Log.d("Connection", "client connected");
                            break;
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    };*/

    class connect extends AsyncTask<String, Integer, String>
    {
        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            try {
                Log.d("Connection", "client trying");
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {}
                    client_socket.connect();
                    Log.d("Connection", "client succeeded");
                //}
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Connection", "client failed");
            }

            return "this string is passed to onPostExecute";
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Do things like hide the progress bar or change a TextView
        }
    }


    final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            //
        }
    };
}
