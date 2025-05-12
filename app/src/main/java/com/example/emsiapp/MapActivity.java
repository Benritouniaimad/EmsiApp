package com.example.emsiapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
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

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mMapView;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialisation de la carte avec MapView
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Récupère le bouton "Locate Me"
        Button btnLocate = findViewById(R.id.btn_locate);
        btnLocate.setOnClickListener(v -> locateUser());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Définir un emplacement par défaut pour la carte
        LatLng place = new LatLng(33.594584, -6.712530);  // Exemple de coordonnées
        mMap.addMarker(new MarkerOptions().position(place).title("Marker in Rabat"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 12)); // Zoom sur l'endroit

        // Ajouter les marqueurs pour chaque site EMSI
        LatLng[] emsiSites = {
                new LatLng(33.589886, -7.603869), // EMSI Casablanca
                new LatLng(34.020882, -6.841650), // EMSI Rabat
                new LatLng(31.634224, -8.006386)  // EMSI Marrakech
        };

        for (LatLng site : emsiSites) {
            mMap.addMarker(new MarkerOptions().position(site).title("EMSI Site"));
        }

        // Vérifier les permissions de localisation
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Fonction pour localiser l'utilisateur et déplacer la carte
    private void locateUser() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Utiliser la position de l'utilisateur pour déplacer la carte
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15)); // Zoom sur la position actuelle

                            // Appeler la fonction pour afficher l'itinéraire vers un site EMSI
                            showRouteToEMSI(userLocation, new LatLng(33.589886, -7.603869)); // Exemple vers EMSI Casablanca
                        } else {
                            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Permission required to access location", Toast.LENGTH_SHORT).show();
        }
    }

    // Fonction pour afficher l'itinéraire depuis la position actuelle vers un site EMSI
    private void showRouteToEMSI(LatLng userLocation, LatLng destination) {
        // Exemple simple pour dessiner une polyline entre les deux points
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(userLocation)
                .add(destination)
                .width(5)
                .color(0xFF0000FF); // Bleu

        mMap.addPolyline(polylineOptions);
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
        super.onLowMemory();  // Appel à la méthode parent
        mMapView.onLowMemory();  // Gestion de la mémoire pour MapView
    }
}
