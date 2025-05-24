package com.example.emsiapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        // Crée une FrameLayout comme racine
        FrameLayout fullLayout = new FrameLayout(this);

        // Ajoute le layout de l’activité enfant
        ViewGroup.inflate(this, layoutResID, fullLayout);

        // Ajoute le bouton assistant
        FloatingActionButton fab = new FloatingActionButton(this);
        fab.setImageResource(R.drawable.ic_chat); // mets ton icône ici
        fab.setContentDescription("Assistant Virtuel");

        // Configure la position
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 50, 50);
        params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;

        fab.setLayoutParams(params);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, AssistantActivity.class);
            startActivity(intent);
        });

        // Ajoute le bouton au layout global
        fullLayout.addView(fab);

        super.setContentView(fullLayout);
    }
}
