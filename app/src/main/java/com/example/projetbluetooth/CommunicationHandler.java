package com.example.projetbluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CommunicationHandler extends Thread{
    BluetoothSocket socket;
    InputStream in;
    OutputStream out;
    DataInputStream dataIn;
    DataOutputStream dataOut;
    ClientActivity clientActivity;
    ServerActivity serverActivity;

    //Cette classe est utilisée par le client et par le serveur pour envoyer et lire des messages.
    //Le client et le serveur vont chacun appeler l'une des deux méthodes suivantes pour donner à l'instance de cette classe qu'ils utilisent les références nécéssaires :
    //1) au socket, pour pouvoir envoyer et lire des messages.
    //2) à la classe du client ou du serveur, pour pouvoir appeler la méthode adaptée à la réception d'un message.

    public void ClientCommunication(ClientActivity clientActivity, BluetoothSocket socket) {
        this.clientActivity = clientActivity;
        this.socket = socket;
    }

    public void ServerCommunication(ServerActivity serverActivity, BluetoothSocket socket) {
        this.serverActivity = serverActivity;
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[256];      //array de bytes dans lequel on va stocker les messages reçus
        Log.d("Communication", "thread is running");

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();

            dataIn = new DataInputStream(in);
            dataOut = new DataOutputStream(out);
            //récupération de références aux flux de données entrants et sortants du socket

            Log.d("Communication", "initialization ok");

            while (true) {
                read(buffer);
            }

        } catch (Exception e) {
            Log.d("Communication", "thread failed");
        }
    }

    public void read(byte[] buffer) {
        int bytes;
        try {
            bytes = dataIn.read(buffer);        //lecture du message sous forme d'array de bytes
            String messageReceived = new String(buffer, 0, bytes);  //conversion array de bytes -> string
            Log.d("Communication", messageReceived);

            String[] messages = messageReceived.split("\\|\\|");    //Le caractère  "|" est un caractère spécial, d'où l'utilisation de backslash
            //Nous avons choisi ce caractère pour améliorer la lisibilité des messages tout en évitant d'utiliser un caractère pouvant être présent dans le nom du processus

            for (String message : messages) {   //Permet de traiter séparément plusieurs messages s'ils sont arrivés dans le même string
                Log.d("Communication", "message read : " + message);
                ProcessMessage(message);    //Traitement du message
            }
        } catch (Exception e) {
            Log.d("Communication", "failed to read");
        }
    }

    public void ProcessMessage(String message) {
        String[] messageData = message.split("\\|");    //séparation des composantes du message
        Log.d("Communication", messageData[0] + " message of length " + messageData.length);

        switch (messageData[0]) {   //permet d'identifier le type de message
            case "data":
                // format : data|processName|uid|RSS
                clientActivity.createViewProcess(messageData[1], messageData[2], messageData[3]);
                break;

            case "buttonClick" :
                //format : buttonClick|processName
                serverActivity.buttonClick(messageData[1]);

            case "query":
                //format : query|processName
                serverActivity.requestMonitoring(messageData[1]);
                break;

            case "update":
                //format : update|processName|uid|RSS
                clientActivity.updateViewProcess(messageData[1], messageData[2], messageData[3]);
                break;

        }
    }

    public void sendData(String message) {
        write(message.getBytes());      //conversion string -> array de bytes
    }

    public void write(byte[] buffer) {
        try {
            dataOut.write(buffer);      //écriture des données
            dataOut.flush();            //envoi des données
        } catch (Exception e) {
            Log.d("thread", "failed to write");
        }
    }

}
