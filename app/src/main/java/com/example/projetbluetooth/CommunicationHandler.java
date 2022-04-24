package com.example.projetbluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CommunicationHandler extends Thread{
    BluetoothSocket socket;
    InputStream in;
    OutputStream out;
    DataInputStream dataIn;
    DataOutputStream dataOut;
    ClientActivity clientActivity;
    ServerActivity serverActivity;
    Boolean connectionInitialized = true;

    // Singleton pattern
    static CommunicationHandler instance = new CommunicationHandler();

    public static CommunicationHandler getInstance() {
        return instance;
    }

    public void ClientCommunication(ClientActivity clientActivity, BluetoothSocket socket) {
        this.clientActivity = clientActivity;
        this.socket = socket;
        //connectionInitialized = false;
        //clientActivity.InitializeDisplayMonitoring();
    }

    public void ServerCommunication(ServerActivity serverActivity, BluetoothSocket socket) {
        this.serverActivity = serverActivity;
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[256];
        Log.d("Communication", "thread is running");

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();

            dataIn = new DataInputStream(in);
            dataOut = new DataOutputStream(out);

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
            bytes = dataIn.read(buffer);
            String messageReceived = new String(buffer, 0, bytes);
            Log.d("Communication", messageReceived);
            String[] messages = messageReceived.split("\\|\\|");
            for (String message : messages) {
                Log.d("Communication", "message read : " + message);
                ProcessMessage(message);
            }
        } catch (Exception e) {
            Log.d("Communication", "failed to read");
        }
    }

    public void ProcessMessage(String message) {
        /*if (!connectionInitialized && clientActivity != null) {
            connectionInitialized = true;
            clientActivity.InitializeDisplayMonitoring();
        }*/
        String[] messageData = message.split("\\|");
        Log.d("Communication", messageData[0] + "   " + messageData.length);
        switch (messageData[0]) {
            case "data":
                // format : data|processName|uid|RSS
                Log.d("Communication", "data received");
                clientActivity.createViewProcess(messageData[1], messageData[2], messageData[3]);
                break;

            case "query":
                //format : query|processName
                serverActivity.requestMonitoring(messageData[1]);
                break;

            case "update":
                //format : update|processName|RSS
                clientActivity.updateViewProcess(messageData[1], messageData[2], messageData[3]);
                break;

        }
    }

    public void sendData(String message) {
        write(message.getBytes());
    }

    public void write(byte[] buffer) {
        try {
            dataOut.write(buffer);
            dataOut.flush();
            //String readMessage = new String(buffer, 0, bytes);
        } catch (Exception e) {
            Log.d("thread", "failed to write");
        }
    }

    public byte[] StringToByte(String string) {
        return string.getBytes();
    }

    public void ByteToString(byte[] buffer) {
        String string = buffer.toString();
        String[] data = string.split("|");  //Split string on the | character
        String packageName = data[0];
        String uid = data[1];
        String rss = data[2];

        //call function in server/client with arguments String, int, int that creates a new
        //need to check if value already exists later
        //pas de monitor pour le client
        //couleur pour indiquer activité monitorée
    }

    //Encodage des données : String sous la forme :
    // [packageName]|[uid]|[rss]
}
