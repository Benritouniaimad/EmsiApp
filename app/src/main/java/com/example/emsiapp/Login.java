package com.example.emsiapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private EditText etEmail, etPass;
    private FirebaseAuth mAuth;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_email);
        etPass = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> authentificateUser());
    }

    private void authentificateUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPass.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }


        Task<AuthResult> authResultTask = mAuth.signInWithEmailAndPassword(email, password);
        authResultTask.addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Connexion réussie
                Toast.makeText(Login.this, "Connexion réussie", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Login.this, ParametresActivity.class);

                startActivity(intent);
                finish();
            } else {

                Toast.makeText(Login.this, "Erreur: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void redirectToRegister(View view) {
        Intent intent = new Intent(Login.this, RegisterActivity.class);
        startActivity(intent);
    }
}
