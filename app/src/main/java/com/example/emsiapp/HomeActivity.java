package com.example.emsiapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    // UI Elements
    private TextView userName, userDepartment, userEmail, currentDate;
    private TextView coursesThisWeek, makeupSessions, attendanceRate;
    private CircleImageView profileImage;

    // Function Cards
    private CardView locationCard, documentsCard, scheduleCard, makeupCard;
    private CardView absenceCard, assistantCard;

    // Bottom Navigation
    private LinearLayout homeButton, searchButton, menuButton;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        initializeViews();

        // Set current date
        setCurrentDate();

        // Set click listeners
        setClickListeners();

        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
            loadStatistics();
        } else {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        // Profile elements
        userName = findViewById(R.id.userName);
        userDepartment = findViewById(R.id.userDepartment);
        userEmail = findViewById(R.id.userEmail);
        profileImage = findViewById(R.id.profileImage);
        currentDate = findViewById(R.id.currentDate);

        // Statistics
        coursesThisWeek = findViewById(R.id.coursesThisWeek);
        makeupSessions = findViewById(R.id.makeupSessions);
        attendanceRate = findViewById(R.id.attendanceRate);

        // Function cards
        locationCard = findViewById(R.id.locationCard);
        documentsCard = findViewById(R.id.documentsCard);
        scheduleCard = findViewById(R.id.scheduleCard);
        makeupCard = findViewById(R.id.makeupCard);
        absenceCard = findViewById(R.id.absenceCard);
        assistantCard = findViewById(R.id.assistantCard);

        // Bottom navigation
        homeButton = findViewById(R.id.homeButton);
        searchButton = findViewById(R.id.searchButton);
        menuButton = findViewById(R.id.menuButton);
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE. dd MMM", new Locale("fr", "FR"));
        String todayDate = sdf.format(new Date());
        currentDate.setText(todayDate);
    }

    private void setClickListeners() {
        // Function cards click listeners
        locationCard.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(HomeActivity.this, MapActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "MapActivity not found", Toast.LENGTH_SHORT).show();
            }
        });

        documentsCard.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(HomeActivity.this, ConsultationDoc.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "DocumentsActivity not found", Toast.LENGTH_SHORT).show();
            }
        });

        scheduleCard.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(HomeActivity.this, EmploiDuTempsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "ScheduleActivity not found", Toast.LENGTH_SHORT).show();
            }
        });

        makeupCard.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(HomeActivity.this, RattrapagesScreen.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Rattrapage not found", Toast.LENGTH_SHORT).show();
            }
        });

        absenceCard.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(HomeActivity.this, AbsencesActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "AbsenceActivity not found", Toast.LENGTH_SHORT).show();
            }
        });

        assistantCard.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(HomeActivity.this, AssistantActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "AssistantActivity not found", Toast.LENGTH_SHORT).show();
            }
        });

        // Bottom navigation click listeners
        homeButton.setOnClickListener(v -> {
            // Already on home, maybe refresh
            Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show();
            // Optionally refresh the current activity
            recreate();
        });

        searchButton.setOnClickListener(v -> {
            // Intent to SearchActivity
            Toast.makeText(this, "Recherche", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(HomeActivity.this, SearchActivity.class));
        });

        menuButton.setOnClickListener(v -> {
            // Intent to MenuActivity or show menu
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(HomeActivity.this, MenuActivity.class));
        });
    }

    private void loadUserData(String userId) {
        db.collection("professeurs")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("nom");
                        String prenom = documentSnapshot.getString("prenom");
                        String departement = documentSnapshot.getString("departement");
                        String email = documentSnapshot.getString("email");
                        String profileImageUrl = documentSnapshot.getString("profileImage");

                        // Set user name
                        if (name != null && prenom != null) {
                            userName.setText("Pr. " + prenom + " " + name);
                        } else if (name != null) {
                            userName.setText("Pr. " + name);
                        }

                        // Set department
                        if (departement != null) {
                            userDepartment.setText(departement);
                        }

                        // Set email
                        if (email != null) {
                            userEmail.setText(email);
                        }

                        // Set profile image
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(HomeActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.avatar)
                                    .error(R.drawable.avatar)
                                    .into(profileImage);
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, "Aucune donnée trouvée pour l'utilisateur", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Erreur lors du chargement : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadStatistics() {
        // Load statistics from Firestore or calculate them
        // For now, using sample data - replace with actual Firebase queries

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Load courses this week
            loadCoursesThisWeek(userId);

            // Load makeup sessions
            loadMakeupSessions(userId);

            // Load attendance rate
            loadAttendanceRate(userId);
        }
    }

    private void loadCoursesThisWeek(String userId) {
        // Query to get courses for this week
        // This is a placeholder - implement actual query based on your data structure
        db.collection("cours")
                .whereEqualTo("professeurId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int coursesCount = queryDocumentSnapshots.size();
                    coursesThisWeek.setText(String.valueOf(coursesCount));
                })
                .addOnFailureListener(e -> {
                    // Set default value on error
                    coursesThisWeek.setText("12");
                });
    }

    private void loadMakeupSessions(String userId) {
        // Query to get makeup sessions
        // This is a placeholder - implement actual query based on your data structure
        db.collection("rattrapages")
                .whereEqualTo("professeurId", userId)
                .whereEqualTo("statut", "prevu")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int makeupCount = queryDocumentSnapshots.size();
                    makeupSessions.setText(String.valueOf(makeupCount));
                })
                .addOnFailureListener(e -> {
                    // Set default value on error
                    makeupSessions.setText("3");
                });
    }

    private void loadAttendanceRate(String userId) {
        // Calculate attendance rate based on your data structure
        // This is a placeholder - implement actual calculation
        db.collection("presences")
                .whereEqualTo("professeurId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Calculate actual attendance rate here
                    // For now, using sample data
                    attendanceRate.setText("85%");
                })
                .addOnFailureListener(e -> {
                    // Set default value on error
                    attendanceRate.setText("85%");
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check authentication state on activity start
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Redirect to login if user is not authenticated
            // startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            // finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadStatistics();
        }
    }
}