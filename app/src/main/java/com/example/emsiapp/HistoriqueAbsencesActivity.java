package com.example.emsiapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoriqueAbsencesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Spinner spinnerGroupe, spinnerDate, spinnerSeance;
    private EditText searchEtudiant, editTextRemarqueGenerale;
    private Button btnAfficher, btnRetour;
    private HistoriqueAdapter adapter;
    private List<Absence> absenceList = new ArrayList<>();
    private List<Absence> fullList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique_absences);

        recyclerView = findViewById(R.id.recyclerViewHistorique);
        spinnerGroupe = findViewById(R.id.spinnerHistoriqueGroupe);
        spinnerDate = findViewById(R.id.spinnerHistoriqueDate);
        spinnerSeance = findViewById(R.id.spinnerHistoriqueSeance);
        btnAfficher = findViewById(R.id.btnAfficherHistorique);
        btnRetour = findViewById(R.id.btnRetourAbsence);
        searchEtudiant = findViewById(R.id.searchEtudiant);
        editTextRemarqueGenerale = findViewById(R.id.editTextRemarqueGeneraleHistorique);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoriqueAdapter(absenceList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        spinnerSeance.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("S1", "S2")));

        loadDatesAndGroupes();

        btnAfficher.setOnClickListener(v -> filterAbsences());

        btnRetour.setOnClickListener(v -> finish());

        searchEtudiant.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadDatesAndGroupes() {
        db.collection("Absences").get().addOnSuccessListener(result -> {
            List<String> dates = new ArrayList<>();
            List<String> groupes = new ArrayList<>();
            for (DocumentSnapshot doc : result) {
                String date = doc.getString("date");
                String groupe = doc.getString("groupe");
                if (date != null && !dates.contains(date)) dates.add(date);
                if (groupe != null && !groupes.contains(groupe)) groupes.add(groupe);
            }
            spinnerDate.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dates));
            spinnerGroupe.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupes));
        });
    }

    private void filterAbsences() {
        Object dateObj = spinnerDate.getSelectedItem();
        Object groupeObj = spinnerGroupe.getSelectedItem();
        Object seanceObj = spinnerSeance.getSelectedItem();
        if (dateObj == null || groupeObj == null || seanceObj == null) return;

        String date = dateObj.toString();
        String groupe = groupeObj.toString();
        String seance = seanceObj.toString();

        db.collection("Absences")
                .whereEqualTo("date", date)
                .whereEqualTo("groupe", groupe)
                .whereEqualTo("seance", seance)
                .orderBy("nom", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(result -> {
                    fullList.clear();
                    for (DocumentSnapshot doc : result) {
                        Absence a = doc.toObject(Absence.class);
                        fullList.add(a);
                    }
                    absenceList.clear();
                    absenceList.addAll(fullList);
                    adapter.notifyDataSetChanged();
                    if (!fullList.isEmpty()) {
                        editTextRemarqueGenerale.setText(fullList.get(0).remarqueGroupe);
                    }
                });
    }

    private void filterList(String query) {
        absenceList.clear();
        for (Absence a : fullList) {
            String fullName = (a.nom + " " + a.prenom).toLowerCase();
            if (fullName.contains(query.toLowerCase())) {
                absenceList.add(a);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public static class Absence {
        public String nom, prenom, groupe, site, remarqueEtudiant, remarqueGroupe, date, seance;
        public boolean present;
        public Absence() {}
    }

    public class HistoriqueAdapter extends RecyclerView.Adapter<HistoriqueAdapter.ViewHolder> {
        private List<Absence> list;
        public HistoriqueAdapter(List<Absence> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_etudiant, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Absence a = list.get(position);
            holder.nomPrenom.setText(a.nom + " " + a.prenom);
            holder.checkbox.setChecked(!a.present);
            holder.checkbox.setEnabled(false);
            holder.remarqueField.setText(a.remarqueEtudiant);
            holder.remarqueField.setEnabled(false);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nomPrenom;
            CheckBox checkbox;
            EditText remarqueField;
            public ViewHolder(View itemView) {
                super(itemView);
                nomPrenom = itemView.findViewById(R.id.textNomEtudiant);
                checkbox = itemView.findViewById(R.id.checkPresent);
                remarqueField = itemView.findViewById(R.id.editTextRemarqueIndiv);
            }
        }
    }
}