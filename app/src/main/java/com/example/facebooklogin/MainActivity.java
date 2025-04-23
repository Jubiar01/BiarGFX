package com.example.facebooklogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.facebooklogin.databinding.ActivityMainBinding;
import com.example.facebooklogin.utils.CustomProgressDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences preferences;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize progress dialog
        progressDialog = new CustomProgressDialog(this);

        // Get preferences
        preferences = getSharedPreferences("facebook_login", MODE_PRIVATE);

        // Check if logged in
        if (!preferences.getBoolean("is_logged_in", false)) {
            // Not logged in, redirect to login with relogin flag
            redirectToLogin(true);
            return;
        }

        // Setup UI with user data
        setupWelcomeUI();

        // Setup logout button
        binding.logoutButton.setOnClickListener(v -> logout());
    }

    private void setupWelcomeUI() {
        // Get user details from preferences
        String userName = preferences.getString("user_name", "User");
        String email = preferences.getString("user_email", "");
        String sessionId = preferences.getString("session_id", "Unknown");
        long timestamp = preferences.getLong("login_timestamp", System.currentTimeMillis());

        // Create device name based on random selection
        String[] deviceNames = {"iPhone 15 Pro", "Google Pixel 8", "Samsung Galaxy S24", "OnePlus 12"};
        String deviceName = deviceNames[new Random().nextInt(deviceNames.length)];

        // Format timestamp
        String formattedTimestamp = formatTimestamp(timestamp);

        // Update UI with real account name
        // Extract first name for the welcome message
        String firstName = userName.contains(" ") ?
                userName.substring(0, userName.indexOf(" ")) :
                userName;

        binding.welcomeMessage.setText("Welcome back, " + firstName + "!");
        binding.sessionInfo.setText("Device: " + deviceName + "\nAccount: " + email);
        binding.timestampInfo.setText("Session ID: " + sessionId + "\nTimestamp: " + formattedTimestamp);
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void logout() {
        // Show progress dialog during logout
        progressDialog.show("Logging out...");

        // Clear login status
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_logged_in", false);
        editor.apply();

        // Delay slightly for visual effect
        binding.getRoot().postDelayed(() -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Redirect to login screen without relogin flag (normal login)
            redirectToLogin(false);
        }, 1000);
    }

    private void redirectToLogin(boolean isRelogin) {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // Add relogin flag if needed
        if (isRelogin) {
            intent.putExtra("relogin", true);
        }
        // Clear back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}