package com.example.emsiapp;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ViewPdfActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pdf);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.setWebViewClient(new WebViewClient());

        // On récupère l’URL envoyée depuis l’intent
        String pdfUrl = getIntent().getStringExtra("pdfUrl");

        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            webView.loadUrl(pdfUrl);
        } else {
            Toast.makeText(this, "Aucun lien PDF fourni.", Toast.LENGTH_LONG).show();
            finish(); // ferme l’activité proprement
        }
    }
}