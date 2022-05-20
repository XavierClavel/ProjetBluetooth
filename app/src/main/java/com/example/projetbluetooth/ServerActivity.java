package com.example.projetbluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;



public class ServerActivity extends AppCompatActivity {
    BluetoothServerSocket server_socket;
    BluetoothSocket socket;
    BluetoothAdapter bluetoothAdapter;
    Context context = this;
    List<ProcessData> listProcess = new ArrayList<>();
    CommunicationHandler communicationHandler;

    Map<String, String> dictionaryProcessNameToUID = new HashMap<String, String>();
    Map<String, Button> dictionaryProcessNameToButton = new HashMap<String, Button>();
    Map<String, TextView> dictionaryProcessNameToTextView = new HashMap<>();

    String previousProcessName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        Log.d("Connection", "start");
        ConnectBluetooth();
    }

    void ConnectBluetooth() {

        UUID uuid = UUID.fromString("f8c3de3d-1fea-4d7c-a8b0-29f63c4c3454");
        //identifiant commun au client et au serveur qui va leur permettre de se connecter

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

        try {
            BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            server_socket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("MonServer", uuid);
            Log.d("Connection", "Creation of RF comm socket succeeded");
            connect connection = new connect();
            connection.doInBackground();
        } catch (Exception e) {
            Log.d("Connection", "Creation of RF comm socket failed");
        }
    }

    class connect extends AsyncTask<String, Integer, String> {
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
                Log.d("Connection", "server trying");
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                }
                socket = server_socket.accept();
                Log.d("Communication", "Connection established");
                server_socket.close();
                DisplayMonitoring();
                communicationHandler = CommunicationHandler.getInstance();
                communicationHandler.ServerCommunication(ServerActivity.this, socket);
                communicationHandler.start();
                Thread.sleep(1000); //On pause le thread pour ne pas commencer à émettre avant que le client n'écoute, sinon il ya ura perte de données
                for (ProcessData processData : listProcess) {
                    communicationHandler.sendData(processData.FormatDataForInitializing());
                    //On envoie un message par process du serveur, qui contiendra le nom du processus, son uid et son RSS (potentiellement inconnu)
                }
                Log.d("Connection", "server succeeded");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Connection", "server failed");
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
    RelativeLayout.LayoutParams paramsTopRight;

    void DisplayMonitoring()
    //Cette méthode a pour fonction d'afficher tous les processus du serveur
    {
        linLayout = findViewById(R.id.linLayout);

        paramsTopRight=
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                RelativeLayout.TRUE);
        paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                RelativeLayout.TRUE);

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List pkgAppsList = getPackageManager().queryIntentActivities(mainIntent, 0);
        //On récupère l'ensemble des applications installées sur le smartphone du serveur (dans la mesure des autorisations dont on dispose)
        //Sur des versions plus récentes d'android, seules des applications systèmes ainsi que notre apk sont visibles.

        Log.d("Connection", pkgAppsList.toString());

        for(Object object :pkgAppsList) { //On itère sur chaque application détectée
            Log.d("Connection", "added item");
            ResolveInfo info = (ResolveInfo) object;
            String strPackageName = info.activityInfo.applicationInfo.packageName.toString();   //On récupère son nom et son UID
            String UID = Integer.toString(info.activityInfo.applicationInfo.uid);
            String RSS = getRSS(strPackageName);

            createViewProcess(UID, strPackageName, "RSS " + RSS);
            listProcess.add(new ProcessData(strPackageName, UID, RSS));
            dictionaryProcessNameToUID.put(strPackageName, UID);
        }
    }

    String getRSS(String wantedPackageName)
    //Méthode qui prend en argument un nom d'application et retourne son RSS sous forme d'un string si celui-ci est connu, ou à défaut "unknow"
    {
        int RSS = 0;
        String RSSstring ="unknown";
        Process process = null;
        try {
            process = new ProcessBuilder("ps").start();
        } catch (Exception e) {
            return "echec";
        }
        InputStream in = process.getInputStream();
        Scanner scanner = new Scanner(in);  //Le scanner comporte les applications dont on peut visionner le RSS
        while (scanner.hasNextLine()) { //On itère sur les applications détectées par le scanner
            String line = scanner.nextLine();
            if (line.startsWith("u0_")) {
                String[] temp = line.split("\\s+");
                String packageName = temp[temp.length - 1];
                if (packageName.equals(wantedPackageName)) {    //Si l'application dont on recherche le RSS apparait dans le scanner
                    int pid = new Integer(temp[1]).intValue();
                    Log.d("PID?", String.valueOf(pid));
                    //memoire qu’occupe le processus
                    RSS = new Integer(temp[4]).intValue();
                    Log.d("RSS?", String.valueOf(RSS));
                    RSSstring = ": " + String.valueOf(RSS);
                    return RSSstring;
                }
            }
        }
        return RSSstring;
    }

    void createViewProcess(String uid, String name, String monitoring)
    // Méthode dont la fonction est d'afficher les informations d'un processus dans un RelativeLayout
    {

        RelativeLayout layout = new RelativeLayout(this);
        TextView nameTextView = new TextView(this);
        nameTextView.setText("[" + uid + "] " + name + "\n" + monitoring + "\n");
        layout.addView(nameTextView);

        if (!monitoring.equals("RSS unknown")) {    //On affiche le bouton de monitoring du RSS uniquement si le RSS est accessible
            Button button = new Button(this);
            button.setText("Monitor");
            layout.addView (button, paramsTopRight);
            dictionaryProcessNameToButton.put(name, button);
            dictionaryProcessNameToTextView.put(name, nameTextView);
            //On insère le bouton créé dans un dictionnaire avec pour clé le nom du processus
            //Cela permettra par la suite de retrouver ce bouton et de changer sa couleur lorsque le client appuyes sur le même bouton de son côté
        }

        linLayout.addView(layout);
        return;
    }

    public void buttonClick(String processName) {
        Log.d("Communication", "here !");
        Log.d("Communication", "previous : " + String.valueOf(previousProcessName));
        Log.d("Communication", "current : " + String.valueOf(processName));

        Button button = dictionaryProcessNameToButton.get(processName);
        if (!processName.equals(previousProcessName)) {
            Log.d("Communication", "different process");

             //On modifie l'apparence du bouton en changeant sa couleur et la couleur de son texte
            PorterDuffColorFilter greenFilter = new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
            button.getBackground().setColorFilter(greenFilter);

            previousProcessName = processName;
        }
        else {
            Log.d("Communication", "same process");

            button.getBackground().clearColorFilter();
            previousProcessName = null;
        }
    }

    public void requestMonitoring(String processName) {
    // Méthode appelée lorsque le client demande un monitoring d'une application qui retourne le RSS à l'instant ou elle est appelée
        String RSS = getRSS(processName);
        String message = ProcessData.StringFormatDataForUpdate(processName, dictionaryProcessNameToUID.get(processName), RSS);
        communicationHandler.sendData(message); //On envoie un message au client pour lui donner la nouvelle valeur du RSS
        updateViewProcess(processName, dictionaryProcessNameToUID.get(processName), RSS);

    }

    void updateViewProcess(String processName, String uid, String RSS) {
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

}