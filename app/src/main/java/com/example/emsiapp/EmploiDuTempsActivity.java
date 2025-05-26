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
                            contenu = String.valueOf(emploi.getHeureDebut()) + " - " + String.valueOf(emploi.getHeureFin())
                                    + "\n" + String.valueOf(emploi.getMatiere())
                                    + "\nSalle : " + String.valueOf(emploi.getSalle())
                                    + "\n" + String.valueOf(emploi.getNiveau())
                                    + "\nSite : " + String.valueOf(emploi.getSite());

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
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(842, 595, 1).create(); // Landscape
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Paint objects for different styles
        Paint headerPaint = new Paint();
        Paint titlePaint = new Paint();
        Paint normalPaint = new Paint();
        Paint boldPaint = new Paint();
        Paint backgroundPaint = new Paint();
        Paint borderPaint = new Paint();
        Paint cellPaint = new Paint();

        // Colors
        int primaryColor = Color.parseColor("#2C3E50");
        int accentColor = Color.parseColor("#3498DB");
        int lightGray = Color.parseColor("#F8F9FA");
        int borderColor = Color.parseColor("#DEE2E6");
        int headerBgColor = Color.parseColor("#E9ECEF");

        // Header background
        backgroundPaint.setColor(primaryColor);
        canvas.drawRect(0, 0, 842, 80, backgroundPaint);

        // Header text
        headerPaint.setColor(Color.WHITE);
        headerPaint.setTextSize(20);
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("EMPLOI DU TEMPS", 421, 30, headerPaint);

        headerPaint.setTextSize(12);
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        canvas.drawText("EMSI - " + nomProfesseur + " | " +
                spinnerAnnee.getSelectedItem().toString() + " - " +
                spinnerSemestre.getSelectedItem().toString() + " | " + date, 421, 55, headerPaint);

        // Table setup
        int startX = 30;
        int startY = 100;
        int cellWidth = 115;
        int cellHeight = 60;
        int timeColumnWidth = 80;

        // Days of week
        String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};

        // Get unique time slots from emploiList
        Set<String> timeSlots = new LinkedHashSet<>();
        for (Emploi emploi : emploiList) {
            timeSlots.add(emploi.getHeureDebut() + "-" + emploi.getHeureFin());
        }

        // Convert to sorted list
        List<String> sortedTimeSlots = new ArrayList<>(timeSlots);
        Collections.sort(sortedTimeSlots);

        // Create a map for quick lookup: day + time -> emploi
        Map<String, Emploi> scheduleMap = new HashMap<>();
        for (Emploi emploi : emploiList) {
            String key = emploi.getJour() + "_" + emploi.getHeureDebut() + "-" + emploi.getHeureFin();
            scheduleMap.put(key, emploi);
        }

        // Border paint
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1);

        // Table header background
        backgroundPaint.setColor(headerBgColor);
        canvas.drawRect(startX, startY, startX + timeColumnWidth + (days.length * cellWidth),
                startY + cellHeight, backgroundPaint);

        // Draw table borders
        titlePaint.setColor(primaryColor);
        titlePaint.setTextSize(11);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);

        // Time column header
        canvas.drawRect(startX, startY, startX + timeColumnWidth, startY + cellHeight, borderPaint);
        canvas.drawText("HORAIRES", startX + timeColumnWidth/2, startY + cellHeight/2 + 4, titlePaint);

        // Day headers
        for (int i = 0; i < days.length; i++) {
            int x = startX + timeColumnWidth + (i * cellWidth);
            canvas.drawRect(x, startY, x + cellWidth, startY + cellHeight, borderPaint);
            canvas.drawText(days[i], x + cellWidth/2, startY + cellHeight/2 + 4, titlePaint);
        }

        // Paint for cell content
        cellPaint.setTextSize(9);
        cellPaint.setColor(Color.BLACK);
        cellPaint.setTextAlign(Paint.Align.LEFT);

        Paint subjectPaint = new Paint();
        subjectPaint.setTextSize(10);
        subjectPaint.setColor(primaryColor);
        subjectPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        subjectPaint.setTextAlign(Paint.Align.LEFT);

        Paint detailPaint = new Paint();
        detailPaint.setTextSize(8);
        detailPaint.setColor(Color.parseColor("#6C757D"));
        detailPaint.setTextAlign(Paint.Align.LEFT);

        // Time column and content rows
        int currentY = startY + cellHeight;
        for (String timeSlot : sortedTimeSlots) {
            // Time column
            canvas.drawRect(startX, currentY, startX + timeColumnWidth, currentY + cellHeight, borderPaint);

            titlePaint.setTextAlign(Paint.Align.CENTER);
            titlePaint.setTextSize(10);
            canvas.drawText(timeSlot, startX + timeColumnWidth/2, currentY + cellHeight/2 + 4, titlePaint);

            // Day columns
            for (int i = 0; i < days.length; i++) {
                int x = startX + timeColumnWidth + (i * cellWidth);
                canvas.drawRect(x, currentY, x + cellWidth, currentY + cellHeight, borderPaint);

                // Check if there's a class for this day and time
                String key = days[i] + "_" + timeSlot;
                Emploi emploi = scheduleMap.get(key);

                if (emploi != null) {
                    // Light background for occupied cells
                    Paint cellBgPaint = new Paint();
                    cellBgPaint.setColor(Color.parseColor("#E3F2FD"));
                    canvas.drawRect(x + 1, currentY + 1, x + cellWidth - 1, currentY + cellHeight - 1, cellBgPaint);

                    // Subject name
                    String subject = emploi.getMatiere();
                    if (subject.length() > 12) {
                        subject = subject.substring(0, 12) + "...";
                    }
                    canvas.drawText(subject, x + 5, currentY + 15, subjectPaint);

                    // Room
                    canvas.drawText("Salle " + emploi.getSalle(), x + 5, currentY + 28, detailPaint);

                    // Level and Site
                    canvas.drawText("Niv." + emploi.getNiveau(), x + 5, currentY + 40, detailPaint);
                    String site = emploi.getSite();
                    if (site.length() > 10) {
                        site = site.substring(0, 10) + "...";
                    }
                    canvas.drawText(site, x + 5, currentY + 52, detailPaint);
                }
            }
            currentY += cellHeight;
        }

        // Legend
        int legendY = currentY + 20;
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTextSize(10);
        titlePaint.setColor(primaryColor);
        canvas.drawText("Légende:", startX, legendY, titlePaint);

        detailPaint.setTextSize(9);
        canvas.drawText("• Niv. = Niveau", startX + 60, legendY, detailPaint);
        canvas.drawText("• Les cours sont affichés avec matière, salle, niveau et site", startX + 150, legendY, detailPaint);

        pdfDocument.finishPage(page);

        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "emploi_du_temps_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date()) + ".pdf";
            File file = new File(downloadsFolder, fileName);
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "✅ PDF enregistré: " + fileName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Erreur PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        pdfDocument.close();
    }
}