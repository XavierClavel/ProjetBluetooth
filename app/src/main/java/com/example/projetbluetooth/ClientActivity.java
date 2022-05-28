package com.example.projetbluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClientActivity extends AppCompatActivity implements  View.OnClickListener {
    BluetoothSocket client_socket;
    BluetoothDevice device = null;
    Context context = this;
    CommunicationHandler communicationHandler;
    boolean connectionEstablished = false;

    LinearLayout linLayout;
    RelativeLayout.LayoutParams paramsTopRight;

    String processName;

    MonitorRSS monitorRSS;

    Map<String, TextView> dictionaryProcessNameToTextView = new HashMap<String, TextView>();
    Map<Integer, String> dictionaryButtonToProcessName = new HashMap<Integer, String>();

    boolean shouldSendQueries = false;
    Button prevButton;

    PorterDuffColorFilter greenFilter = new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        Log.d("Connection", "start");
        InitializeDisplayMonitoring();
        ConnectBluetooth();

    }

    void ConnectBluetooth() {

        UUID uuid = UUID.fromString("f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Connection", "Refused");
        }

        Log.d("Connection", "Looking for devices");
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();

        Log.d("Connection", "Got devices");
        Log.d("Connection", Integer.toString(bondedDevices.size()));
        loop: for (BluetoothDevice deviceIterator : bondedDevices) {
            Log.d("Connection", deviceIterator.getName());
            switch (deviceIterator.getName()) {
                case "OPPO Reno Z":
                    device = deviceIterator;
                    Log.d("Connection", "Device was found !");
                    break;

                case "ProjetBluetooth" :
                    device = deviceIterator;
                    Log.d("Connection", "Device was found !");
                    break loop;

                case "Redmi Note 9":
                    device = deviceIterator;
                    Log.d("Connection", "Device was found !");
                    break loop;

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
        else {
            Toast.makeText(this, "Aucun appareil compatible n'a été trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    class connect extends AsyncTask<String, Integer, String> {
        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            while (!connectionEstablished) {
                try {
                    Log.d("Connection", "client trying");
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("Connection", "permission error");
                    }

                    client_socket.connect();

                    Log.d("Connection", "client succeeded");
                    connectionEstablished = true;

                    //}
                } catch (Exception e) {
                    Log.d("Connection", "client failed");
                    try {
                        Thread.sleep(500);
                    } catch (Exception f){
                        Log.d("Connection", "Failed to sleep");
                    }

                }

            }
            communicationHandler = new CommunicationHandler();
            communicationHandler.ClientCommunication(ClientActivity.this, client_socket);
            communicationHandler.start();
            return "Done";
        }
    }

    public void InitializeDisplayMonitoring() {
        runOnUiThread(new Runnable() {  //Un runOnUIThread est ici utilisé à la place d'un thread handler
            public void run() {
                linLayout = findViewById(R.id.linLayout);

                paramsTopRight = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                //Paramètres utilisés pour placer le bouton dans le relative layout

                Log.d("Communication", "view initialized");
            }
        });
    }

    public void createViewProcess(String name, String uid, String RSS) {
        //Méthode appelée à la réception d'un message ayant pour premier paramètre "data"
        // Elle a pour fonction d'afficher les informations envoyées par le serveur sur une application : nom, uid et RSS.
        runOnUiThread(new Runnable() {
            public void run() {
                Log.d("Communication", "view process created");

                RelativeLayout layout = new RelativeLayout(ClientActivity.this);
                TextView nameTextView = new TextView(ClientActivity.this);
                Log.d("Communication", name + uid + RSS);
                nameTextView.setText("[" + uid + "] " + name + "\n" + "RSS " + RSS + "\n");     //affichage des informations
                layout.addView(nameTextView);

                Log.d("RSS", "\"" + RSS + "\"");

                if (!RSS.equals("unknown")) {       //On affiche un bouton uniquement si le serveur a accès au RSS du processus
                    Button button = new Button(ClientActivity.this);
                    button.setText("Monitor");
                    layout.addView(button, paramsTopRight);

                    dictionaryButtonToProcessName.put(button.getId(), name);
                    //On place le nom du processus dans un dictionnaire avec comme clé l'identifiant du bouton.
                    //Cela nous permettra lors de l'appui sur le bouton de savoir à quel processus il est lié.

                    button.setOnClickListener(ClientActivity.this);     //on active l'écoute sur le bouton

                } else Log.d("Communication", "no button");

                try {
                    linLayout.addView(layout);  //ajouter les relative layouts au linear layout permet d'obtenir l'affichage désiré, avec des blocs qui se succèdent verticalement
                    Log.d("Communication", "success");

                    dictionaryProcessNameToTextView.put(name, nameTextView);
                    //on place le textView dans un dictionnaire avec comme clé le nom du processus pour pouvoir récupérer une référence au textView plus tard,
                    // ce qui sera nécessaire pour mettre à jour le RSS

                } catch (Exception e) {
                    Log.d("Communication", "failed to add view");
                }
            }
        });
    }

    public void updateViewProcess(String processName, String uid, String RSS) {
        //Méthode appelée à la réception d'un message ayant pour premier paramètre "update"
        // Elle a pour fonction de mettre à jour les informations affichées par le client sur une application monitorée
        Log.d("Thread", "method called with parameters : " + processName + " " + uid + " " + RSS);

        runOnUiThread(new Runnable() {
            public void run() {
                Log.d("Thread", "update thread running");
                try {
                    Log.d("Thread", "tried");
                    TextView nameTextView = dictionaryProcessNameToTextView.get(processName);
                    Log.d("Thread", "succeeded at reading dictionary");
                    nameTextView.setText("[" + uid + "] " + processName + "\n" + "RSS " + RSS + "\n");
                    Log.d("Communication", "successfully updated text view");
                } catch (Exception e) {
                    Log.d("Connection", " Failed to update view process");
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        Integer buttonId = view.getId();
        processName = dictionaryButtonToProcessName.get(buttonId);  //On récupère le nom du processus associé au bouton grâce à un dictionnaire
        //La variable globale processName est ensuite utilisée pour l'envoi des requêtes de RSS et d'indiaction d'appui sur un bouton, pour indiquer le processus cible

        Button button = (Button) view;
        shouldSendQueries = button != prevButton; //variable indiquant si un monitoring est en cours

        if (prevButton == button) { //correspond au cas où on appuye sur le bouton d'un processus en cours de monitoring
            button.getBackground().clearColorFilter();  //On retire la coloration du bouton
            prevButton = null;      //On réinitialise la valeur de prevButton pour permettre de monitorer de nouveau le processus
        }
        else {
            button.getBackground().setColorFilter(greenFilter); //Coloration du bouton du processus pour lequel on demande un monitoring
            if (prevButton != null) {
                prevButton.getBackground().clearColorFilter();      //On retire la coloration de l'ancien bouton (on n'autorise qu'un seul monitoring simultané)
            }
            prevButton = button;
        }

        communicationHandler.sendData(ProcessData.MessageButtonClick(processName));

        if (monitorRSS == null) {
            monitorRSS = new MonitorRSS();
            monitorRSS.start();
            //Le thread est lancé une seule fois, puis tourne en continu.
        }
    }

    class MonitorRSS extends Thread {
    //Ce thread a pour fonction d'effectuer une requête de RSS à chaque seconde.
    //Il n'est jamais stoppé une fois lancé, mais n'envoie de requête que si un processus est monitoré.
        @Override
        public void run() {
            while(true) {
                try {
                    Log.d("bool value", String.valueOf(shouldSendQueries));
                    if (shouldSendQueries) {
                        communicationHandler.sendData(ProcessData.MessageQuery(processName));
                    }
                    Thread.sleep(1000); //attente avant d'envoyer une nouvelle requête de RSS
                } catch (Exception e) {
                    Log.d("Communication", "monitoring request failed");
                }
            }
        }
    }


}
