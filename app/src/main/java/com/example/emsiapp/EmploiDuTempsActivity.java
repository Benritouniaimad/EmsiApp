package com.example.emsiapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class EmploiDuTempsActivity extends BaseActivity {

    private Spinner spinnerAnnee, spinnerSemestre;
    private Button btnAfficher, btnTelecharger;
    private TableLayout tableLayout;
    private List<Emploi> emploiList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String nomProfesseur = "";
    private String professeurId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emploi_du_temps);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        spinnerAnnee = findViewById(R.id.spinnerAnnee);
        spinnerSemestre = findViewById(R.id.spinnerSemestre);
        btnAfficher = findViewById(R.id.btnAfficherEmploi);
        btnTelecharger = findViewById(R.id.btnTelechargerPDF);
        tableLayout = findViewById(R.id.tableLayoutEmploi);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            professeurId = user.getUid();

            db.collection("professeurs").document(professeurId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            nomProfesseur = doc.getString("nom");
                        }
                    });

            chargerAnneeEtSemestre();
        } else {
            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnAfficher.setOnClickListener(v -> chargerEmploi());
        btnTelecharger.setOnClickListener(v -> genererPDF());
    }

    private void chargerAnneeEtSemestre() {
        db.collection("emplois_du_temps")
                .whereEqualTo("professeurId", professeurId)
                .get()
                .addOnSuccessListener(query -> {
                    Set<String> annees = new HashSet<>();
                    Set<String> semestres = new HashSet<>();

                    for (QueryDocumentSnapshot doc : query) {
                        annees.add(doc.getString("anneeScolaire"));
                        semestres.add(doc.getString("semestre"));
                    }

                    List<String> anneeList = new ArrayList<>(annees);
                    Collections.sort(anneeList);
                    spinnerAnnee.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, anneeList));

                    List<String> semestreList = new ArrayList<>(semestres);
                    Collections.sort(semestreList);
                    spinnerSemestre.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semestreList));
                });
    }

    private void chargerEmploi() {
        emploiList.clear();
        String annee = spinnerAnnee.getSelectedItem().toString();
        String semestre = spinnerSemestre.getSelectedItem().toString();

        db.collection("emplois_du_temps")
                .whereEqualTo("professeurId", professeurId)
                .whereEqualTo("anneeScolaire", annee)
                .whereEqualTo("semestre", semestre)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Emploi emploi = doc.toObject(Emploi.class);
                        emploiList.add(emploi);
                    }
                    afficherGrille();
                    if (emploiList.isEmpty()) {
                        Toast.makeText(this, "Aucun emploi trouvé.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void afficherGrille() {
        tableLayout.removeAllViews();

        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
        String[] niveaux = {"S1", "S2", "S3", "S4", "S5", "S6"};

        // Récupération des heures uniques présentes dans Firestore
        Set<String> heuresSet = new TreeSet<>();
        for (Emploi emploi : emploiList) {
            heuresSet.add(emploi.getHeureDebut());
        }
        List<String> heures = new ArrayList<>(heuresSet);

        // En-tête
        TableRow header = new TableRow(this);
        header.addView(createCell("Jour/Heure", true));
        for (String niveau : niveaux) {
            header.addView(createCell(niveau, true));
        }
        tableLayout.addView(header);

        // Remplissage du tableau
        for (String heure : heures) {
            for (String jour : jours) {
                TableRow row = new TableRow(this);
                row.addView(createCell(jour + "\n" + heure, true));

                for (String niveau : niveaux) {
                    String contenu = "-";
                    for (Emploi emploi : emploiList) {
                        if (emploi.getJour().equals(jour)
                                && emploi.getHeureDebut().equals(heure)
                                && emploi.getNiveau().equals(niveau)) {
                            contenu = emploi.getHeureDebut() + " - " + emploi.getHeureFin()
                                    + "\n" + emploi.getMatiere()
                                    + "\nSalle " + emploi.getSalle()
                                    + "\n" + emploi.getNiveau();
                            break;
                        }
                    }
                    row.addView(createCell(contenu, false));
                }

                tableLayout.addView(row);
            }
        }
    }

    private TextView createCell(String text, boolean isHeader) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(8, 12, 8, 12);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundResource(R.drawable.cell_border); // Assure-toi que ce fichier existe
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(isHeader ? 16 : 12);
        tv.setTypeface(null, isHeader ? Typeface.BOLD : Typeface.NORMAL);
        tv.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return tv;
    }

    private void genererPDF() {
        if (emploiList.isEmpty()) {
            Toast.makeText(this, "Aucun emploi à exporter.", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int y = 60;
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        paint.setTextSize(16);
        canvas.drawText("EMSI", 250, y, paint);
        y += 25;
        canvas.drawText("Nom du professeur : " + nomProfesseur, 50, y, paint);
        y += 20;
        canvas.drawText("Date d'export : " + date, 50, y, paint);
        y += 20;
        canvas.drawText("Année scolaire : " + spinnerAnnee.getSelectedItem().toString()
                + " - Semestre : " + spinnerSemestre.getSelectedItem().toString(), 50, y, paint);
        y += 30;

        paint.setTextSize(14);
        for (Emploi emploi : emploiList) {
            if (y > 800) break;
            String line = emploi.getHeureDebut() + " - " + emploi.getHeureFin()
                    + "\n" + emploi.getMatiere()
                    + "\nSalle " + emploi.getSalle()
                    + "\n" + emploi.getJour() + " - Niveau " + emploi.getNiveau();
            for (String l : line.split("\n")) {
                canvas.drawText(l, 50, y, paint);
                y += 20;
            }
            y += 10;
        }

        pdfDocument.finishPage(page);

        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsFolder, "emploi_du_temps.pdf");
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "✅ PDF enregistré dans Téléchargements.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur PDF : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }
}
