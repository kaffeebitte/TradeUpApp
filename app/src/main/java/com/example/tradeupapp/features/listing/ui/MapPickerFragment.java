package com.example.tradeupapp.features.listing.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tradeupapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvAddress;
    private TextView tvCoordinates;
    private LatLng selectedLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvAddress = view.findViewById(R.id.tvAddress);
        tvCoordinates = view.findViewById(R.id.tvCoordinates);
        MaterialButton btnSelectLocation = view.findViewById(R.id.btnSelectLocation);
        FloatingActionButton fabMyLocation = view.findViewById(R.id.fabMyLocation);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up button click listeners
        btnSelectLocation.setOnClickListener(v -> {
            if (selectedLocation != null) {
                // Bundle up the location data to return to the calling fragment
                Bundle result = new Bundle();
                result.putDouble("latitude", selectedLocation.latitude);
                result.putDouble("longitude", selectedLocation.longitude);
                result.putString("address", tvAddress.getText().toString());

                // Navigate back with the selected location data
                NavController navController = NavHostFragment.findNavController(this);
                navController.getPreviousBackStackEntry().getSavedStateHandle().set("location_data", result);
                navController.popBackStack();
            } else {
                Toast.makeText(requireContext(), "Please select a location first", Toast.LENGTH_SHORT).show();
            }
        });

        fabMyLocation.setOnClickListener(v -> {
            if (mMap != null) {
                moveToCurrentLocation();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set map UI settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Enable my location button if permission is granted
        enableMyLocation();

        // Set up camera move listener to update location info
        mMap.setOnCameraIdleListener(() -> {
            selectedLocation = mMap.getCameraPosition().target;
            updateLocationInfo(selectedLocation);
        });

        // Start with current location if available
        moveToCurrentLocation();
    }

    private void moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
            } else {
                // If location is null, use a default location (could be customized for your app's region)
                LatLng defaultLocation = new LatLng(21.0285, 105.8542); // Hanoi as example
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f));
                Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLocationInfo(LatLng latLng) {
        // Update coordinates display
        String coordinatesText = String.format(Locale.getDefault(),
                "Lat: %.6f, Lng: %.6f", latLng.latitude, latLng.longitude);
        tvCoordinates.setText(coordinatesText);

        // Get address from coordinates using Geocoder
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Lấy phường/xã, quận/huyện, tỉnh/thành phố
                String ward = address.getSubLocality(); // phường/xã
                String district = address.getLocality(); // quận/huyện
                if (district == null) district = address.getSubAdminArea(); // fallback
                String city = address.getAdminArea(); // tỉnh/thành phố
                StringBuilder addressText = new StringBuilder();
                if (ward != null && !ward.isEmpty()) {
                    addressText.append(ward);
                }
                if (district != null && !district.isEmpty()) {
                    if (addressText.length() > 0) addressText.append(", ");
                    addressText.append(district);
                }
                if (city != null && !city.isEmpty()) {
                    if (addressText.length() > 0) addressText.append(", ");
                    addressText.append(city);
                }
                if (addressText.length() > 0) {
                    tvAddress.setText(addressText.toString());
                } else {
                    tvAddress.setText("Address not found");
                }
            } else {
                tvAddress.setText("Address not found");
            }
        } catch (IOException e) {
            tvAddress.setText("Unable to get address");
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                moveToCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
