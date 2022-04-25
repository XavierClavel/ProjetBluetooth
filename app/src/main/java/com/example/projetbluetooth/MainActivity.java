package com.example.projetbluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onStart() {
        super.onStart();
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT},1);
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){


                // You can show your dialog message here but instead I am
                // showing the grant permission dialog box
                ActivityCompat.requestPermissions(this, new String[] {
                                Manifest.permission.BLUETOOTH_CONNECT},
                        10);



            }
            else{

                //Requesting permission
                ActivityCompat.requestPermissions(this, new String[] {
                                Manifest.permission.BLUETOOTH_CONNECT },
                        10);
            }
            Log.d("Connection", "Refused");
            //return;
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //On récupère une référence aux deux boutons :
        Button clientButton = (Button) findViewById(R.id.clientButton);
        Button serverButton = (Button) findViewById(R.id.serverButton);

        //On active l'écoute sur les deux boutons :
        clientButton.setOnClickListener(this);
        serverButton.setOnClickListener(this);

        Log.d("Start", "Initialize");

    }

    // Si l'utiliateur appuyes sur un bouton que l'on écoute :
    @Override
    public void onClick(View view) {
        Intent intent;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

        }

        findViewById(R.id.serverButton).setVisibility(View.GONE);
        findViewById(R.id.clientButton).setVisibility(View.GONE);
        findViewById(R.id.connecting).setVisibility(View.VISIBLE);

        switch (view.getId()) {

            // Si l'utilisateur appuye sur le bouton "Client", on lance l'activité ClientActivity
            case R.id.clientButton:
                intent = new Intent(this, ClientActivity.class);
                startActivity(intent);
                break;

            //Si l'utilisateur appuye sur le bouton "Serveur, on lance l'activité ServerActivity
            case R.id.serverButton:
                intent = new Intent(this, ServerActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("Communication", "permission requested ?????????");
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Please authorize access to bluetooth devices", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}