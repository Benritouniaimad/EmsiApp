package com.example.emsiapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ParametresActivity extends AppCompatActivity {

    private Switch switchDarkMode;
    private Spinner spinnerLangue;
    private EditText editUsername, editNewEmail, editOldPassword, editNewPassword;
    private Button btnUpdateUsername, btnUpdateEmail, btnUpdatePassword, btnLogout;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametre);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        switchDarkMode = findViewById(R.id.switchDarkMode);
        spinnerLangue = findViewById(R.id.spinnerLangue);
        editUsername = findViewById(R.id.editUsername);
        editNewEmail = findViewById(R.id.editNewEmail);
        editOldPassword = findViewById(R.id.editOldPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        btnUpdateUsername = findViewById(R.id.btnUpdateUsername);
        btnUpdateEmail = findViewById(R.id.btnUpdateEmail);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        btnLogout = findViewById(R.id.btnLogout);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // MODE SOMBRE
        boolean isDark = prefs.getBoolean("darkMode", false);
        switchDarkMode.setChecked(isDark);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("darkMode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // LANGUES
        String[] langues = {"Français", "Anglais", "Arabe", "Espagnol"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, langues);
        spinnerLangue.setAdapter(adapter);

        String savedLangue = prefs.getString("langue", "Français");
        spinnerLangue.setSelection(adapter.getPosition(savedLangue));

        spinnerLangue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLangue = parent.getItemAtPosition(position).toString();
                prefs.edit().putString("langue", selectedLangue).apply();
                String code = LocaleHelper.getLanguageCode(selectedLangue);
                LocaleHelper.setLocale(ParametresActivity.this, code);
                recreate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Afficher nom et email actuel si dispo
        if (user != null) {
            if (user.getDisplayName() != null) {
                editUsername.setText(user.getDisplayName());
            }
            if (user.getEmail() != null) {
                editNewEmail.setHint(user.getEmail());
            }
        }

        // CHANGER PSEUDO
        btnUpdateUsername.setOnClickListener(v -> {
            String newUsername = editUsername.getText().toString().trim();
            if (!TextUtils.isEmpty(newUsername) && user != null) {
                user.updateProfile(new UserProfileChangeRequest.Builder()
                                .setDisplayName(newUsername).build())
                        .addOnSuccessListener(task -> Toast.makeText(this, "Pseudo mis à jour", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        // CHANGER EMAIL
        btnUpdateEmail.setOnClickListener(v -> {
            String newEmail = editNewEmail.getText().toString().trim();
            String oldPassword = editOldPassword.getText().toString().trim();

            if (user != null && !newEmail.isEmpty() && !oldPassword.isEmpty()) {
                user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), oldPassword))
                        .addOnSuccessListener(authResult -> {
                            user.updateEmail(newEmail)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Email mis à jour", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Mot de passe incorrect", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Remplir l'email et le mot de passe actuel", Toast.LENGTH_SHORT).show();
            }
        });

        // CHANGER MOT DE PASSE
        btnUpdatePassword.setOnClickListener(v -> {
            String oldPass = editOldPassword.getText().toString().trim();
            String newPass = editNewPassword.getText().toString().trim();

            if (user != null && !oldPass.isEmpty() && !newPass.isEmpty()) {
                user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), oldPass))
                        .addOnSuccessListener(authResult -> {
                            user.updatePassword(newPass)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Mot de passe changé", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Mot de passe incorrect", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Remplir les deux champs de mot de passe", Toast.LENGTH_SHORT).show();
            }
        });

        // DÉCONNEXION
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }

    // Appliquer la langue au démarrage
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE);
        String lang = prefs.getString("langue", "Français");
        String code = LocaleHelper.getLanguageCode(lang);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, code));
    }
}
