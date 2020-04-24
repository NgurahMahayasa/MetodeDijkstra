package com.example.metodedjikstra;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnSetup,btnTracking;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnSetup = (Button) findViewById(R.id.btn_setup_map);
        btnTracking = (Button) findViewById(R.id.btn_map);

        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                FragmentManager manager = getFragmentManager();
////                FragmentTransaction transaction = manager.beginTransaction();
////                transaction.replace(R.layout.activity_main,MapsActivity);
////                FragmentActivity
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            }
        });

        btnTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

}
