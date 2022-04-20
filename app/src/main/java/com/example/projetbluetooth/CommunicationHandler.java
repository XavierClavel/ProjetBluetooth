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

public class CommunicationHandler extends Thread{
    public BluetoothSocket socket;
    InputStream in;
    OutputStream out;
    DataInputStream dataIn;
    DataOutputStream dataOut;

    // Singleton pattern
    static CommunicationHandler instance = new CommunicationHandler();

    public static CommunicationHandler getInstance() {
        return instance;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[256];
        int bytes;

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();

            dataIn = new DataInputStream(in);
            dataOut = new DataOutputStream(out);

            while (true) {

                bytes = dataIn.read(buffer);
                String message = new String(buffer, 0, bytes);
                Log.d("Communication", "message read : " + message);
            }

        } catch (Exception e) {
            Log.d("Communication", "thread failed");
        }
    }

    public void read(byte[] buffer) {
        int bytes;
        try {
            bytes = dataIn.read(buffer);
            String readMessage = new String(buffer, 0, bytes);
        } catch (Exception e) {
            Log.d("thread", "failed to read");
        }
    }

    public void write(byte[] buffer) {
        int bytes = 3;
        try {
            dataOut.write(bytes);
            dataOut.flush();
            //String readMessage = new String(buffer, 0, bytes);
        } catch (Exception e) {
            Log.d("thread", "failed to read");
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
