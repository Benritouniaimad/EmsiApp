package com.example.emsiapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConsultationDoc extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DocumentAdapter adapter;
    private List<Document> documentList = new ArrayList<>();
    private List<Document> filteredList = new ArrayList<>();

    private EditText searchField;
    private Spinner sortSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consultation_doc_activity);

        recyclerView = findViewById(R.id.recyclerViewDocuments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DocumentAdapter(documentList, document -> {
            Intent intent = new Intent(ConsultationDoc.this, ViewPdfActivity.class);
            intent.putExtra("pdfUrl", document.getUrl());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        searchField = findViewById(R.id.searchField);
        sortSpinner = findViewById(R.id.sortSpinner);

        // Adapter pour le spinner
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Nom ↑", "Nom ↓", "Date ↑", "Date ↓"});
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        // Écouteur pour le tri
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applySearchAndSort(searchField.getText().toString(), position);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Écouteur pour le champ de recherche
        searchField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchAndSort(s.toString(), sortSpinner.getSelectedItemPosition());
            }
        });

        loadDocumentsFromFirestore();
    }

    private void loadDocumentsFromFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("documents_professeurs")
                .document(uid)
                .collection("documents")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    documentList.clear();
                    for (DocumentSnapshot snapshot : querySnapshots) {
                        Document doc = snapshot.toObject(Document.class);
                        documentList.add(doc);
                    }
                    applySearchAndSort(searchField.getText().toString(), sortSpinner.getSelectedItemPosition());
                })
                .addOnFailureListener(e -> Log.e("FIRESTORE", "Erreur de chargement : ", e));
    }

    private void applySearchAndSort(String query, int sortOption) {
        filteredList.clear();
        String lower = query.toLowerCase();

        // Filtrage par nom
        for (Document doc : documentList) {
            if (doc.getName().toLowerCase().contains(lower)) {
                filteredList.add(doc);
            }
        }

        // Tri
        Comparator<Document> comparator;
        switch (sortOption) {
            case 0: comparator = Comparator.comparing(Document::getName); break;
            case 1: comparator = (d1, d2) -> d2.getName().compareTo(d1.getName()); break;
            case 2: comparator = Comparator.comparing(Document::getDate); break;
            case 3: comparator = (d1, d2) -> d2.getDate().compareTo(d1.getDate()); break;
            default: comparator = Comparator.comparing(Document::getName);
        }
        Collections.sort(filteredList, comparator);
        adapter.updateList(filteredList);
    }
}
