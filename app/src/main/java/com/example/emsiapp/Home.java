package com.example.emsiapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageButton menuButton;
    CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);




        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        profileImage.setOnClickListener(v ->
                Toast.makeText(this, "Nom: Prof. Ahmed", Toast.LENGTH_SHORT).show()
        );
    }
}
