package com.example.emsiapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class RattrapagesScreen extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RattrapageAdapter adapter;
    private List<Rattrapage> rattrapageList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rattrapages);

        recyclerView = findViewById(R.id.recyclerViewRattrapage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RattrapageAdapter(rattrapageList);
        recyclerView.setAdapter(adapter);

        loadRattrapages();
    }

    private void loadRattrapages() {
        String currentProfId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rattrapages")
                .whereEqualTo("professeurId", currentProfId)
                .orderBy("date")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    rattrapageList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Rattrapage r = doc.toObject(Rattrapage.class);
                        rattrapageList.add(r);
                    }
                    adapter.notifyDataSetChanged();

                    if (rattrapageList.isEmpty()) {
                        Toast.makeText(this, "Aucune session de rattrapage trouvée.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
