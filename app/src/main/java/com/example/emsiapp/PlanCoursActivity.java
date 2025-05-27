package com.example.emsiapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.*;

public class PlanCoursActivity extends AppCompatActivity {

    EditText editModule, editChapitre;
    Spinner spinnerType;
    Button btnGenerer, btnTelecharger;
    TextView textResultat;

    OkHttpClient client = new OkHttpClient();
    String apiKey = "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"; // ✅ Mets ta vraie clé ici

    String[] types = {"Plan de cours", "Quiz", "Test rapide", "Examen blanc"};
    String contenuGenere = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_cours);

        editModule = findViewById(R.id.editModule);
        editChapitre = findViewById(R.id.editChapitre);
        spinnerType = findViewById(R.id.spinnerType);
        btnGenerer = findViewById(R.id.btnGenerer);
        btnTelecharger = findViewById(R.id.btnTelecharger);
        textResultat = findViewById(R.id.textResultat);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerType.setAdapter(adapter);

        btnGenerer.setOnClickListener(v -> {
            String module = editModule.getText().toString().trim();
            String chapitre = editChapitre.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();

            if (module.isEmpty() || chapitre.isEmpty()) {
                Toast.makeText(this, "Remplis tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            genererAvecIA(module, chapitre, type);
        });

        btnTelecharger.setOnClickListener(v -> {
            if (contenuGenere.isEmpty()) {
                Toast.makeText(this, "Rien à télécharger", Toast.LENGTH_SHORT).show();
                return;
            }

            if (Build.VERSION.SDK_INT < 30 &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                String module = editModule.getText().toString().trim();
                sauvegarderPDF(module.isEmpty() ? "Module" : module, contenuGenere);
            }
        });
    }

    private void genererAvecIA(String module, String chapitre, String type) {
        textResultat.setText("Génération en cours...");

        String prompt = "Génère un contenu pédagogique de type : " + type + " pour le module \"" + module + "\" sur le(s) chapitre(s) : " + chapitre + ".\n"
                + "Format clair et structuré, utile pour les étudiants.";

        try {
            JSONObject json = new JSONObject();
            json.put("model", "openai/gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "user").put("content", prompt));
            json.put("messages", messages);
            json.put("max_tokens", 800);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("HTTP-Referer", "https://emsiapp.com")
                    .addHeader("X-Title", "PlanCoursEmsi")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> textResultat.setText("Erreur réseau : " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> textResultat.setText("Erreur API : " + response.code()));
                        return;
                    }

                    String res = response.body().string();
                    try {
                        JSONObject obj = new JSONObject(res);
                        contenuGenere = obj
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        runOnUiThread(() -> textResultat.setText(contenuGenere));
                    } catch (Exception e) {
                        runOnUiThread(() -> textResultat.setText("Erreur lecture réponse : " + e.getMessage()));
                    }
                }
            });

        } catch (Exception e) {
            textResultat.setText("Erreur préparation requête : " + e.getMessage());
        }
    }

    private void sauvegarderPDF(String module, String contenu) {
        try {
            String fileName = "Plan_" + module.replace(" ", "_") + "_" + System.currentTimeMillis() + ".pdf";
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(directory, fileName);

            PdfDocument pdfDocument = new PdfDocument();
            Paint paint = new Paint();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int x = 40, y = 60;
            for (String line : contenu.split("\n")) {
                canvas.drawText(line, x, y, paint);
                y += 20;
                if (y > 800) break; // simple : une page max pour l’instant
            }

            pdfDocument.finishPage(page);
            pdfDocument.writeTo(new FileOutputStream(file));
            pdfDocument.close();

            Toast.makeText(this, "PDF enregistré : " + file.getName(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Erreur PDF : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
