package com.example.imhangry;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocal();
        setContentView(R.layout.activity_home_page);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.app_name));
        Button button = findViewById(R.id.btn);
        Button change = findViewById(R.id.btnChange);

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLangueDialog();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Créer un Intent pour l'activité que vous souhaitez démarrer
                Intent intent = new Intent(HomePage.this, Map.class);

                // Ajouter des données supplémentaires à l'Intent si nécessaire
                intent.putExtra("clé", "valeur");

                // Démarrer l'activité en utilisant l'Intent
                startActivity(intent);
            }
        });
    }

    private void showChangeLangueDialog() {
        final  String[] listItems = {"English","Français","العربية"};
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this);
        builder.setTitle("Choose Language...");
        builder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if( i ==0){
                    setLocale("En");
                    recreate();
                }
                else if( i ==1){
                    setLocale("fr");
                    recreate();
                }
                else if( i ==2){
                    setLocale("Ar");
                    recreate();
                }
                dialog.dismiss();
            }
        });
        AlertDialog alertDialogdialog = builder.create();
        alertDialogdialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("settings",MODE_PRIVATE).edit();
        editor.putString("My_Lang",lang);
        editor.apply();
    }
    public void  loadLocal(){
        SharedPreferences prefs = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        String langague = prefs.getString("My_Lang","");
        setLocale(langague);

    }
    }
