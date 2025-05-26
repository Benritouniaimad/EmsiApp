package com.example.emsiapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mMapView;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker userMarker;
    private Polyline currentRoutePolyline;
    private List<Marker> siteMarkers = new ArrayList<>();

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String professeurId = "";

    private LatLng currentUserLocation = null;
    private boolean isRealTimeTrackingEnabled = false;

    // Sites from Firestore
    private List<SiteInfo> sitesList = new ArrayList<>();
    private SiteInfo closestSite = null;

    // Site info class
    public static class SiteInfo {
        public String name;
        public LatLng location;
        public String address;

        public SiteInfo(String name, double latitude, double longitude, String address) {
            this.name = name;
            this.location = new LatLng(latitude, longitude);
            this.address = address;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            professeurId = user.getUid();
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MaterialButton btnLocate = findViewById(R.id.btn_locate);
        btnLocate.setOnClickListener(v -> {
            isRealTimeTrackingEnabled = true;
            focusOnUserLocation();
        });

        MaterialButton btnDirections = findViewById(R.id.btn_directions);
        btnDirections.setOnClickListener(v -> {
            isRealTimeTrackingEnabled = false;
            showRouteToNearestSite();
        });

        // Configure LocationRequest
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        // Configure LocationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        updateUserMarkerAndCamera(currentUserLocation);
                        findClosestSite();
                    }
                }
            }
        };

        // Load sites from Firestore
        loadSitesFromFirestore();
    }

    private void loadSitesFromFirestore() {
        db.collection("emplois_du_temps")
                .whereEqualTo("professeurId", professeurId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, SiteInfo> uniqueSites = new HashMap<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String siteName = document.getString("site");

                        if (siteName != null && !siteName.isEmpty()) {
                            // Map site names to coordinates (you might want to store these in Firestore too)
                            LatLng siteLocation = getSiteCoordinates(siteName);
                            if (siteLocation != null) {
                                uniqueSites.put(siteName, new SiteInfo(siteName,
                                        siteLocation.latitude, siteLocation.longitude, siteName));
                            }
                        }
                    }

                    sitesList.clear();
                    sitesList.addAll(uniqueSites.values());

                    if (mMap != null) {
                        addSiteMarkers();
                    }

                    if (sitesList.isEmpty()) {
                        Toast.makeText(this, "Aucun site trouvé dans votre emploi du temps", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors du chargement des sites: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    // Fallback to default EMSI sites if Firestore fails
                    loadDefaultSites();
                });
    }

    private LatLng getSiteCoordinates(String siteName) {
        // Map site names to their coordinates
        // You should ideally store coordinates in Firestore too
        switch (siteName.toLowerCase()) {
            case "emsi casablanca":
            case "casablanca":
                return new LatLng(33.589886, -7.603869);
            case "emsi rabat":
            case "rabat":
                return new LatLng(34.020882, -6.841650);
            case "emsi marrakech":
            case "marrakech":
                return new LatLng(31.634224, -8.006386);
            default:
                // If site name doesn't match known locations, you might want to:
                // 1. Use geocoding API to get coordinates
                // 2. Store coordinates in Firestore
                // 3. Return a default location
                return new LatLng(33.971588, -6.849813); // Default Morocco center
        }
    }

    private void loadDefaultSites() {
        // Fallback to default EMSI sites
        sitesList.clear();
        sitesList.add(new SiteInfo("EMSI Casablanca", 33.589886, -7.603869, "Casablanca"));
        sitesList.add(new SiteInfo("EMSI Rabat", 34.020882, -6.841650, "Rabat"));
        sitesList.add(new SiteInfo("EMSI Marrakech", 31.634224, -8.006386, "Marrakech"));

        if (mMap != null) {
            addSiteMarkers();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                isRealTimeTrackingEnabled = false;
            }
        });

        // Set default location for the map
        LatLng defaultLocation = new LatLng(33.971588, -6.849813);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 6));

        addSiteMarkers();
        checkLocationPermissionAndStartUpdates();
    }

    private void addSiteMarkers() {
        if (mMap == null) return;

        // Remove old markers
        for (Marker marker : siteMarkers) {
            marker.remove();
        }
        siteMarkers.clear();

        // Add new markers for sites from Firestore
        for (SiteInfo site : sitesList) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(site.location)
                    .title(site.name)
                    .snippet(site.address)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            siteMarkers.add(marker);
        }
    }

    private void updateUserMarkerAndCamera(LatLng location) {
        if (mMap == null) return;

        if (userMarker == null) {
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Votre position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        } else {
            userMarker.setPosition(location);
        }

        if (isRealTimeTrackingEnabled) {
            float currentZoom = mMap.getCameraPosition().zoom;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, Math.max(currentZoom, 15f)));
        }
    }

    private void checkLocationPermissionAndStartUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            isRealTimeTrackingEnabled = true;
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void focusOnUserLocation() {
        if (currentUserLocation != null && mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15f));
            Toast.makeText(this, "Position localisée", Toast.LENGTH_SHORT).show();
            isRealTimeTrackingEnabled = true;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    updateUserMarkerAndCamera(currentUserLocation);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15f));
                    Toast.makeText(this, "Position localisée", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Impossible d'obtenir votre position. Activez la localisation.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Permission de localisation requise", Toast.LENGTH_SHORT).show();
            checkLocationPermissionAndStartUpdates();
        }
    }

    private void showRouteToNearestSite() {
        if (currentUserLocation == null) {
            Toast.makeText(this, "Localisation en cours... Veuillez patienter ou cliquer sur 'Me Localiser'", Toast.LENGTH_LONG).show();
            focusOnUserLocation();
            return;
        }

        if (sitesList.isEmpty()) {
            Toast.makeText(this, "Aucun site disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        findClosestSite();

        if (closestSite == null) {
            Toast.makeText(this, "Impossible de trouver le site le plus proche", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentRoutePolyline != null) {
            currentRoutePolyline.remove();
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .add(currentUserLocation)
                .add(closestSite.location)
                .width(10)
                .color(Color.BLUE)
                .geodesic(true);

        currentRoutePolyline = mMap.addPolyline(polylineOptions);

        // Adjust camera to show both points
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(currentUserLocation);
        builder.include(closestSite.location);
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

        Toast.makeText(this, "Itinéraire vers " + closestSite.name, Toast.LENGTH_LONG).show();
        isRealTimeTrackingEnabled = false;
    }

    private void findClosestSite() {
        if (currentUserLocation == null || sitesList.isEmpty()) return;

        double minDistance = Double.MAX_VALUE;
        SiteInfo tempClosestSite = null;

        for (SiteInfo site : sitesList) {
            float[] results = new float[1];
            Location.distanceBetween(currentUserLocation.latitude, currentUserLocation.longitude,
                    site.location.latitude, site.location.longitude, results);
            double distance = results[0];

            if (distance < minDistance) {
                minDistance = distance;
                tempClosestSite = site;
            }
        }
        closestSite = tempClosestSite;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        if (mMap != null) {
            checkLocationPermissionAndStartUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        stopLocationUpdates();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    checkLocationPermissionAndStartUpdates();
                }
            } else {
                Toast.makeText(this, "Permission de localisation refusée. Certaines fonctionnalités seront désactivées.", Toast.LENGTH_LONG).show();
            }
        }
    }
}