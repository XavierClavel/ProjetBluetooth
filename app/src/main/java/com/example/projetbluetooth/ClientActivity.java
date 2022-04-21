package com.example.projetbluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClientActivity extends AppCompatActivity {
    BluetoothSocket client_socket;
    BluetoothDevice device = null;
    Context context = this;
    CommunicationHandler communicationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        Log.d("Connection", "start");
        InitializeDisplayMonitoring();
        ConnectBluetooth();

    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Connection", "permission granted");
                }
                else {
                    Toast.makeText(this, "Merci d'autoriser la connexion aux appareils bluetooth", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }*/

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
                communicationHandler = CommunicationHandler.getInstance();
                communicationHandler.ClientCommunication(ClientActivity.this, client_socket);
                communicationHandler.start();
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

    LinearLayout linLayout;
    RelativeLayout.LayoutParams paramsTopLeft;
    RelativeLayout.LayoutParams paramsTopRight;

    public void InitializeDisplayMonitoring()
    {
        linLayout = findViewById(R.id.linLayout);

        paramsTopLeft =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsTopLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                RelativeLayout.TRUE);
        paramsTopLeft.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                RelativeLayout.TRUE);

        paramsTopRight=
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                RelativeLayout.TRUE);
        paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                RelativeLayout.TRUE);
    }

    Map<String,TextView> dictionaryProcessNameToTextView = new HashMap<String, TextView>();

    public void createViewProcess(String name, String uid, String RSS)
    {
        Log.d("Communication", "view process created");

        RelativeLayout layout = new RelativeLayout(this);
        TextView nameTextView = new TextView(this);
        nameTextView.setText("[" + uid + "] " + name + "\n" + RSS + "\n");
        layout.addView(nameTextView);

        Log.d("Communication", "ok 1");
        Button button = new Button(this);
        button.setText("Monitor");
        layout.addView (button, paramsTopRight);

        Log.d("Communication", "ok 2");

        linLayout.addView(layout);
        Log.d("Communication", "ok 3");
        //dictionaryProcessNameToTextView.put(name, nameTextView);
        return;
    }

    public void updateViewProcess(String processName, String uid,String RSS) {
        try {
            TextView nameTextView = dictionaryProcessNameToTextView.get(processName);
            nameTextView.setText("[" + uid + "] " + processName + "\n" + RSS + "\n");
        } catch (Exception e) {
            Log.d("Connection", " Failed to update view process");
        }

    }


    final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            //
        }
    };
}
