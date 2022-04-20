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

public class Communicate extends Thread{
    public BluetoothSocket socket;
    InputStream in;
    OutputStream out;
    DataInputStream dataIn;
    DataOutputStream dataOut;

    @Override
    public void run() {
        byte[] buffer = new byte[256];
        int bytes;

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();

            dataIn = new DataInputStream(in);
            dataOut = new DataOutputStream(out);

        } catch (Exception e) {
            Log.d("thread", "thread failed");
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
            String readMessage = new String(buffer, 0, bytes);
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
        int uid = Integer.valueOf(data[1]);
        int rss = Integer.valueOf(data[2]);

        //call function in server/client with arguments String, int, int that creates a new
    }

    //Encodage des données : String sous la forme :
    // [packageName]|[uid]|[rss]
}
