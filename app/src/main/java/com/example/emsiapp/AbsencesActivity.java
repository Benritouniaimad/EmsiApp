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

import java.text.SimpleDateFormat;
import java.util.*;

public class AbsencesActivity extends AppCompatActivity {

    private Spinner spinnerGroupe, spinnerSite, spinnerSeance;
    private RecyclerView recyclerView;
    private EditText editTextRemarqueGroupe, searchEtudiant;
    private Button btnValider;

    private FirebaseFirestore db;
    private List<Etudiant> etudiants = new ArrayList<>();
    private List<Etudiant> fullList = new ArrayList<>();
    private EtudiantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absences);

        spinnerGroupe = findViewById(R.id.spinnerGroupe);
        spinnerSite = findViewById(R.id.spinnerSite);
        spinnerSeance = findViewById(R.id.spinnerSeance);
        recyclerView = findViewById(R.id.recyclerViewEtudiants);
        editTextRemarqueGroupe = findViewById(R.id.editTextRemarqueGroupe);
        btnValider = findViewById(R.id.btnValider);

        db = FirebaseFirestore.getInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EtudiantAdapter(etudiants);
        recyclerView.setAdapter(adapter);

        loadGroupes();

        spinnerGroupe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String groupe = spinnerGroupe.getSelectedItem().toString();
                if (!groupe.equals("Sélectionner un groupe")) {
                    loadEtudiants(groupe);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        List<String> seances = new ArrayList<>(Arrays.asList("Sélectionner une séance", "S1", "S2"));
        spinnerSeance.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, seances));

        btnValider.setOnClickListener(v -> enregistrerAbsences());

        Button btnHistorique = findViewById(R.id.btnHistorique);
        btnHistorique.setOnClickListener(v -> {
            startActivity(new Intent(AbsencesActivity.this, HistoriqueAbsencesActivity.class));
        });

    }

    private void loadGroupes() {
        db.collection("Groupes").get().addOnSuccessListener(result -> {
            List<String> groupes = new ArrayList<>();
            Set<String> sites = new HashSet<>();
            groupes.add("Sélectionner un groupe");
            sites.add("Sélectionner un site");
            for (DocumentSnapshot doc : result) {
                groupes.add(doc.getString("nom"));
                sites.add(doc.getString("site"));
            }
            spinnerGroupe.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupes));
            spinnerSite.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(sites)));
        });
    }

    private void loadEtudiants(String groupeNom) {
        db.collection("Groupes").whereEqualTo("nom", groupeNom).get().addOnSuccessListener(result -> {
            if (!result.isEmpty()) {
                String groupeId = result.getDocuments().get(0).getId();
                db.collection("Groupes").document(groupeId).collection("étudiants").get().addOnSuccessListener(snapshot -> {
                    fullList.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        String nomComplet = doc.getString("nom");
                        String[] split = nomComplet != null ? nomComplet.split(" ", 2) : new String[]{"", ""};
                        fullList.add(new Etudiant(doc.getId(), split[0], split.length > 1 ? split[1] : ""));
                    }
                    etudiants.clear();
                    etudiants.addAll(fullList);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void enregistrerAbsences() {
        String remarqueGroupe = editTextRemarqueGroupe.getText().toString();
        String groupe = spinnerGroupe.getSelectedItem().toString();
        String site = spinnerSite.getSelectedItem().toString();
        String seance = spinnerSeance.getSelectedItem().toString();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (Etudiant e : etudiants) {
            Map<String, Object> data = new HashMap<>();
            data.put("etudiantId", e.id);
            data.put("nom", e.nom);
            data.put("prenom", e.prenom);
            data.put("present", e.present);
            data.put("remarqueEtudiant", e.remarque);
            data.put("remarqueGroupe", remarqueGroupe);
            data.put("groupe", groupe);
            data.put("site", site);
            data.put("date", date);
            data.put("seance", seance);
            db.collection("Absences").add(data);
        }

        Toast.makeText(this, "Absences enregistrées et formulaire réinitialisé.", Toast.LENGTH_SHORT).show();

        // Vider les champs
        editTextRemarqueGroupe.setText("");
        spinnerGroupe.setSelection(0);
        spinnerSite.setSelection(0);
        spinnerSeance.setSelection(0);
        etudiants.clear();
        fullList.clear();
        adapter.notifyDataSetChanged();
    }

    private void filterList(String query) {
        etudiants.clear();
        for (Etudiant e : fullList) {
            String fullName = (e.nom + " " + e.prenom).toLowerCase();
            if (fullName.contains(query.toLowerCase())) {
                etudiants.add(e);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public static class Etudiant {
        public String id;
        public String nom;
        public String prenom;
        public boolean present;
        public String remarque;

        public Etudiant() {}
        public Etudiant(String id, String nom, String prenom) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.present = false;
            this.remarque = "";
        }
    }

    public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {
        private List<Etudiant> etudiants;

        public EtudiantAdapter(List<Etudiant> etudiants) {
            this.etudiants = etudiants;
        }

        @NonNull
        @Override
        public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_etudiant, parent, false);
            return new EtudiantViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
            Etudiant e = etudiants.get(position);
            holder.nomPrenom.setText(e.nom + " " + e.prenom);
            holder.checkbox.setChecked(!e.present); // ✅ checked = absent
            holder.checkbox.setOnCheckedChangeListener((b, checked) -> e.present = !checked);
            holder.remarqueField.setText(e.remarque);
            holder.remarqueField.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) e.remarque = holder.remarqueField.getText().toString();
            });
        }

        @Override
        public int getItemCount() {
            return etudiants.size();
        }

        class EtudiantViewHolder extends RecyclerView.ViewHolder {
            TextView nomPrenom;
            CheckBox checkbox;
            EditText remarqueField;

            public EtudiantViewHolder(@NonNull View itemView) {
                super(itemView);
                nomPrenom = itemView.findViewById(R.id.textNomEtudiant);
                checkbox = itemView.findViewById(R.id.checkPresent);
                remarqueField = itemView.findViewById(R.id.editTextRemarqueIndiv);
            }
        }
    }
}
