package com.example.emsiapp; // Remplacez par votre package réel

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    // UI Elements
    private TextView userName;
    private CircleImageView profileImage;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        userName = findViewById(R.id.userName);
        profileImage = findViewById(R.id.profileImage);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData(String userId) {
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    if (name != null) {
                        userName.setText(name); // ✅ Dynamic update here
                    }

                    String profileImageUrl = dataSnapshot.child("profileImage").getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(HomeActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.avatar)
                                .error(R.drawable.avatar)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this,
                        "Erreur lors du chargement des données: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Vérifier l'état d'authentification à chaque démarrage de l'activité
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Rediriger si nécessaire
            // startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            // finish();
        }
    }
}