package com.example.emsiapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class AbsenceAutoActivity extends AppCompatActivity {

    EditText editGroupe;
    Button btnPrendrePhoto;
    TextView resultat;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absence_auto);

        editGroupe = findViewById(R.id.editGroupe);
        btnPrendrePhoto = findViewById(R.id.btnPrendrePhoto);
        resultat = findViewById(R.id.resultat);

        btnPrendrePhoto.setOnClickListener(v -> {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePicture.resolveActivity(getPackageManager()) != null) {
                try {
                    photoFile = File.createTempFile("photo_classe", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                    Uri photoURI = Uri.fromFile(photoFile);
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            envoyerPhotoAuServeur();
        }
    }

    private void envoyerPhotoAuServeur() {
        new Thread(() -> {
            try {
                String groupe = editGroupe.getText().toString().trim();
                String boundary = "*****" + System.currentTimeMillis() + "*****";
                URL url = new URL("http://192.168.11.106:3001/detecter-presence"); // 💡 adapte à ton IP locale
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                // Ajouter le champ texte "groupe"
                outputStream.writeBytes("--" + boundary + "\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"groupe\"\r\n\r\n");
                outputStream.writeBytes(groupe + "\r\n");

                // Ajouter le fichier image
                outputStream.writeBytes("--" + boundary + "\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"photo.jpg\"\r\n");
                outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n");

                FileInputStream fileInputStream = new FileInputStream(photoFile);
                int bytesRead;
                byte[] buffer = new byte[4096];
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.writeBytes("\r\n");
                outputStream.writeBytes("--" + boundary + "--\r\n");

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(response.toString());
                    JSONArray presents = json.getJSONArray("presents");
                    JSONArray absents = json.getJSONArray("absents");

                    StringBuilder resultText = new StringBuilder("✅ Présents :\n");
                    for (int i = 0; i < presents.length(); i++) {
                        resultText.append("• ").append(presents.getString(i)).append("\n");
                    }

                    resultText.append("\n❌ Absents :\n");
                    for (int i = 0; i < absents.length(); i++) {
                        resultText.append("• ").append(absents.getString(i)).append("\n");
                    }

                    runOnUiThread(() -> resultat.setText(resultText.toString()));
                } else {
                    runOnUiThread(() -> resultat.setText("Erreur serveur : " + responseCode));
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> resultat.setText("Erreur : " + e.getMessage()));
            }
        }).start();
    }
}
