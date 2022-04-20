package com.example.projetbluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

    // Si l'utiliateur appuyes sur un bouton que l'on écoute :
    @Override
    public void onClick(View view) {
        Intent intent;
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
}