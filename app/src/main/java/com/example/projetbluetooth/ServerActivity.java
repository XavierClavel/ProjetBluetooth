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

    LinearLayout linLayout;
    RelativeLayout.LayoutParams paramsTopRight;

    Map<String, String> dictionaryProcessNameToUID = new HashMap<String, String>();
    Map<String, Button> dictionaryProcessNameToButton = new HashMap<String, Button>();
    Map<String, TextView> dictionaryProcessNameToTextView = new HashMap<>();

    String previousProcessName = null;

    PorterDuffColorFilter greenFilter = new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
    //filtre qui va nous permettre de modifier la couleur d'un bouton sans changer sa forme

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
            Log.d("Connection", "Refused");
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
        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            try {
                Log.d("Connection", "server trying");
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                }   //n??cessaire pour ??viter d'avoir une erreur

                socket = server_socket.accept();
                Log.d("Communication", "Connection established");
                server_socket.close();

                DisplayMonitoring();    //Affichage des packages

                communicationHandler = new CommunicationHandler();
                communicationHandler.ServerCommunication(ServerActivity.this, socket);
                communicationHandler.start();

                Thread.sleep(1000); //On pause le thread pour ne pas commencer ?? ??mettre avant que le client n'??coute, sinon il ya ura perte de donn??es
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
    }

    void DisplayMonitoring()
    //Cette m??thode a pour fonction d'afficher tous les processus du serveur
    {
        linLayout = findViewById(R.id.linLayout);

        paramsTopRight= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        paramsTopRight.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        //Param??tres pour l'affichage du bouton

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List pkgAppsList = getPackageManager().queryIntentActivities(mainIntent, 0);
        //On r??cup??re l'ensemble des applications install??es sur le smartphone du serveur (dans la mesure des autorisations dont on dispose)
        //Sur des versions plus r??centes d'android, seules certaines applications syst??mes ainsi que notre apk sont accessibles.

        Log.d("Connection", pkgAppsList.toString());

        for(Object object :pkgAppsList) { //On it??re sur chaque application d??tect??e
            Log.d("Connection", "added item");
            ResolveInfo info = (ResolveInfo) object;
            String strPackageName = info.activityInfo.applicationInfo.packageName.toString();   //On r??cup??re son nom et son UID
            String UID = Integer.toString(info.activityInfo.applicationInfo.uid);
            String RSS = getRSS(strPackageName);

            createViewProcess(UID, strPackageName, "RSS " + RSS);   //Affichage de ces donn??es
            listProcess.add(new ProcessData(strPackageName, UID, RSS));
            //Pour chaque application, on instancie la classe ProcessData pour stocker les informations relatives ?? l'application,
            // dans le but de pouvoir les envoyer au client pour qu'il puisse afficher ces m??mes informations.
            //On place ces instances dans une liste pour les enregistrer et pouvoir les envoyer une fois la connexion ??tablie.

            dictionaryProcessNameToUID.put(strPackageName, UID);
            //On enregistre la valeur de l'uid dans un dictionnaire pour pouvoir le r??cup??rer lorsqu'on aura besoin de mettre ?? jour les donn??es client
            //pour un monitoring
        }
    }

    String getRSS(String wantedPackageName)
    //M??thode qui prend en argument un nom d'application et retourne sous la forme d'un string son RSS si celui-ci est connu, ou ?? d??faut "unknown"
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
        Scanner scanner = new Scanner(in);      //Le scanner permet d'acc??der au RSS de certaines applications dans la mesure des autorisations.
        //On utilise un scanner car le m??thode getPackageManager().queryIntentActivities(mainIntent, 0) n'a pas acc??s aux informations sur le RSS.
        //En revanche, le scanner ne peut voir que notre apk tandis que cette m??thode peut voir de nombreaux packages,
        // c'est pourquoi on voit que pour beaucoup de processus le RSS est inconnu.

        while (scanner.hasNextLine()) {     //On it??re sur les lignes du string renvoy?? par le scanner
            String line = scanner.nextLine();   //On passe ?? la ligne suivante
            if (line.startsWith("u0_")) {       //Si la ligne comporte des informations sur un package
                String[] temp = line.split("\\s+");
                String packageName = temp[temp.length - 1];     //On r??cup??re le nom du package
                if (packageName.equals(wantedPackageName)) {    //Si l'application dont on recherche le RSS apparait dans le scanner
                    RSS = new Integer(temp[4]).intValue();  //m??moire qu???occupe le processus
                    Log.d("RSS?", String.valueOf(RSS));
                    RSSstring = ": " + String.valueOf(RSS);
                    return RSSstring;
                }
            }
        }
        return RSSstring;
    }

    void createViewProcess(String uid, String name, String monitoring)
    // M??thode dont la fonction est d'afficher les informations d'un processus dans un RelativeLayout
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
            //On ins??re le bouton cr???? dans un dictionnaire avec pour cl?? le nom du processus
            //Cela permettra par la suite de retrouver ce bouton et de changer sa couleur lorsque le client appuyes sur le m??me bouton de son c??t??
            //idem avec le textview
        }

        linLayout.addView(layout);
        return;
    }

    public void buttonClick(String processName) {
        Log.d("Communication", "here !");
        Log.d("Communication", "previous : " + String.valueOf(previousProcessName));
        Log.d("Communication", "current : " + String.valueOf(processName));

        Button button = dictionaryProcessNameToButton.get(processName);
        //On r??cup??re une r??f??rence au bouton gr??ce au dictionnaire cr???? pr??c??demment

        if (processName.equals(previousProcessName)) {  //si l'utilisateur appuye une deuxi??me fois sur le m??me bouton, on signale l'arr??t du monitoring
            Log.d("Communication", "same process");

            button.getBackground().clearColorFilter();  //On modifie l'apparence du bouton pour qu'il apparaisse comme ?? son ??tat initial
            previousProcessName = null; //On r??initialise la m??moire en anticipation du cas o?? l'utilisateur r??appuyes sur le m??me bouton,
            //Ce qui v??rifierait cette m??me condition si on ne modifie pas la valeur de previousProcessName
        }
        else { //sinon, on signale que le monitoring est en cours via un changement de couleur du boutpn

            if (previousProcessName != null) {
                dictionaryProcessNameToButton.get(previousProcessName).getBackground().clearColorFilter();
                //On r??initialise l'apparence du bouton de l'ancienne activit?? monitor??e
            }

            button.getBackground().setColorFilter(greenFilter);
            //On modifie l'apparence du bouton en changeant sa couleur.
            //L'utilisation d'un filtre nous permet de modifier la couleur du bouton sans changer sa forme, contrairement ?? la m??thode setBackgroundColor()

            previousProcessName = processName;
        }
    }

    public void requestMonitoring(String processName) {
    // M??thode appel??e lorsque le client demande le monitoring d'une application. Elle retourne la valeur du RSS ?? l'instant o?? elle est appel??e.

        String RSS = getRSS(processName);   //r??cup??ration du RSS
        String message = ProcessData.FormatDataForUpdate(processName, dictionaryProcessNameToUID.get(processName), RSS);  //Formatage du message

        communicationHandler.sendData(message); //On envoie un message au client pour lui donner la nouvelle valeur du RSS
        updateViewProcess(processName, dictionaryProcessNameToUID.get(processName), RSS);   //On update la valeur du RSS affich??e par le serveur

    }

    void updateViewProcess(String processName, String uid, String RSS) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    TextView nameTextView = dictionaryProcessNameToTextView.get(processName);
                    nameTextView.setText("[" + uid + "] " + processName + "\n" + "RSS " + RSS + "\n");
                } catch (Exception e) {
                    Log.d("Connection", " Failed to update view process");
                }
            }
        });

    }

}