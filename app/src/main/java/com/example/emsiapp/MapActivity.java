package com.example.emsiapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mMapView;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LatLng userLocation = null;

    // Sites EMSI
    private final LatLng[] emsiSites = {
            new LatLng(33.589886, -7.603869), // EMSI Casablanca
            new LatLng(34.020882, -6.841650), // EMSI Rabat
            new LatLng(31.634224, -8.006386)  // EMSI Marrakech
    };

    // Site EMSI le plus proche (initialisé à Casablanca par défaut)
    private LatLng closestEmsiSite = new LatLng(33.589886, -7.603869);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialisation de la carte avec MapView
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Récupère le bouton "Me localiser"
        MaterialButton btnLocate = findViewById(R.id.btn_locate);
        btnLocate.setOnClickListener(v -> locateUserOnly());

        // Récupère le bouton "Itinéraire"
        MaterialButton btnDirections = findViewById(R.id.btn_directions);
        btnDirections.setOnClickListener(v -> showRouteToNearestEmsi());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Définir un emplacement par défaut pour la carte
        LatLng defaultLocation = new LatLng(33.971588, -6.849813);  // Emplacement centré au Maroc
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 6)); // Vue globale du Maroc

        // Ajouter les marqueurs pour chaque site EMSI
        for (int i = 0; i < emsiSites.length; i++) {
            String title = "";
            switch (i) {
                case 0:
                    title = "EMSI Casablanca";
                    break;
                case 1:
                    title = "EMSI Rabat";
                    break;
                case 2:
                    title = "EMSI Marrakech";
                    break;
            }
            mMap.addMarker(new MarkerOptions().position(emsiSites[i]).title(title));
        }

        // Vérifier les permissions de localisation
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Fonction pour seulement localiser l'utilisateur sans montrer d'itinéraire
    private void locateUserOnly() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Nettoyer la carte des anciens polylines et marqueurs (sauf les sites EMSI)
                            mMap.clear();
                            // Re-ajouter les marqueurs EMSI
                            for (int i = 0; i < emsiSites.length; i++) {
                                String title = "";
                                switch (i) {
                                    case 0:
                                        title = "EMSI Casablanca";
                                        break;
                                    case 1:
                                        title = "EMSI Rabat";
                                        break;
                                    case 2:
                                        title = "EMSI Marrakech";
                                        break;
                                }
                                mMap.addMarker(new MarkerOptions().position(emsiSites[i]).title(title));
                            }

                            // Stocker la position de l'utilisateur
                            userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            // Trouver le site EMSI le plus proche
                            findClosestEmsiSite();

                            // Ajouter marqueur pour la position actuelle
                            mMap.addMarker(new MarkerOptions()
                                    .position(userLocation)
                                    .title("Votre position"));

                            // Déplacer la caméra à la position de l'utilisateur avec un zoom élevé
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                            Toast.makeText(this, "Position localisée", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Impossible d'obtenir votre position actuelle", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Permission de localisation requise", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Fonction pour afficher l'itinéraire vers le site EMSI le plus proche
    private void showRouteToNearestEmsi() {
        if (userLocation == null) {
            // Si la position de l'utilisateur n'est pas encore déterminée
            Toast.makeText(this, "Veuillez d'abord localiser votre position", Toast.LENGTH_SHORT).show();
            locateUserOnly();
            return;
        }

        // Nettoyer la carte des anciens polylines (mais garder les marqueurs)
        mMap.clear();

        // Re-ajouter les marqueurs EMSI
        for (int i = 0; i < emsiSites.length; i++) {
            String title = "";
            switch (i) {
                case 0:
                    title = "EMSI Casablanca";
                    break;
                case 1:
                    title = "EMSI Rabat";
                    break;
                case 2:
                    title = "EMSI Marrakech";
                    break;
            }
            mMap.addMarker(new MarkerOptions().position(emsiSites[i]).title(title));
        }

        // Ajouter marqueur pour la position actuelle
        mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title("Votre position"));

        // Dessiner l'itinéraire vers le site EMSI le plus proche
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(userLocation)
                .add(closestEmsiSite)
                .width(8)
                .color(ContextCompat.getColor(this, android.R.color.holo_blue_dark));

        mMap.addPolyline(polylineOptions);

        // Ajuster la caméra pour voir à la fois l'utilisateur et le site EMSI
        double minLat = Math.min(userLocation.latitude, closestEmsiSite.latitude);
        double maxLat = Math.max(userLocation.latitude, closestEmsiSite.latitude);
        double minLng = Math.min(userLocation.longitude, closestEmsiSite.longitude);
        double maxLng = Math.max(userLocation.longitude, closestEmsiSite.longitude);

        // Calculer les limites avec une marge
        double latPadding = (maxLat - minLat) * 0.2;
        double lngPadding = (maxLng - minLng) * 0.2;

        // Créer les limites et zoom sur la vue
        com.google.android.gms.maps.model.LatLngBounds bounds = new com.google.android.gms.maps.model.LatLngBounds(
                new LatLng(minLat - latPadding, minLng - lngPadding),
                new LatLng(maxLat + latPadding, maxLng + lngPadding));

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

        // Indiquer à l'utilisateur vers quel campus l'itinéraire est affiché
        String campusName = "EMSI";
        for (int i = 0; i < emsiSites.length; i++) {
            if (emsiSites[i].equals(closestEmsiSite)) {
                switch (i) {
                    case 0:
                        campusName = "EMSI Casablanca";
                        break;
                    case 1:
                        campusName = "EMSI Rabat";
                        break;
                    case 2:
                        campusName = "EMSI Marrakech";
                        break;
                }
                break;
            }
        }

        Toast.makeText(this, "Itinéraire vers " + campusName, Toast.LENGTH_SHORT).show();
    }

    // Calculer le site EMSI le plus proche de la position de l'utilisateur
    private void findClosestEmsiSite() {
        if (userLocation == null) return;

        double minDistance = Double.MAX_VALUE;

        for (LatLng site : emsiSites) {
            double distance = calculateDistance(userLocation, site);
            if (distance < minDistance) {
                minDistance = distance;
                closestEmsiSite = site;
            }
        }
    }

    // Calcul de distance basique entre deux coordonnées
    private double calculateDistance(LatLng point1, LatLng point2) {
        // Formule de distance euclidienne simple (pour démo)
        // Pour une application réelle, utilisez la formule de Haversine
        return Math.sqrt(
                Math.pow(point1.latitude - point2.latitude, 2) +
                        Math.pow(point1.longitude - point2.longitude, 2)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            } else {
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
