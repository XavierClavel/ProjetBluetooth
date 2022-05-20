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

    // Si l'utilisateur appuyes sur l'un des deux boutons :
    @Override
    public void onClick(View view) {
        Intent intent;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

        }

        // On masque les boutons ainsi que le texte descriptif
        findViewById(R.id.serverButton).setVisibility(View.GONE);
        findViewById(R.id.clientButton).setVisibility(View.GONE);
        findViewById(R.id.infoText).setVisibility(View.GONE);

        //A la place, on affiche un texte indiquant que le connexion est en cours
        findViewById(R.id.connecting).setVisibility(View.VISIBLE);

        switch (view.getId()) {

            // Si l'utilisateur appuye sur le bouton "Client", on lance l'activité ClientActivity :
            case R.id.clientButton:
                intent = new Intent(this, ClientActivity.class);
                startActivity(intent);
                break;

            //Si l'utilisateur appuye sur le bouton "Serveur, on lance l'activité ServerActivity :
            case R.id.serverButton:
                intent = new Intent(this, ServerActivity.class);
                startActivity(intent);
                break;
        }
    }

}