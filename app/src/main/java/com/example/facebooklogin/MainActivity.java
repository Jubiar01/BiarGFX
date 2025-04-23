package com.example.facebooklogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.facebooklogin.databinding.ActivityMainBinding;
import com.example.facebooklogin.utils.CustomProgressDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences preferences;
    private CustomProgressDialog progressDialog;
    private boolean isPaused = false;
    private ExecutorService executorService;

    // Default account name for the default user
    private static final String DEFAULT_USER_NAME = "Sarah Johnson";

    // Session timeout for auto-reconnect (milliseconds)
    // For demo purposes, we'll use a very short time (10 seconds)
    private static final long SESSION_AUTO_RECONNECT_TIME = 10 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize progress dialog and executor service
        progressDialog = new CustomProgressDialog(this);
        executorService = Executors.newSingleThreadExecutor();

        // Get preferences
        preferences = getSharedPreferences("facebook_login", MODE_PRIVATE);

        // Check if logged in
        if (!preferences.getBoolean("is_logged_in", false)) {
            // Not logged in, attempt to refetch account if credentials exist
            refetchAccount();
            return;
        }

        // Setup UI with user data
        setupWelcomeUI();

        // Setup logout button
        binding.logoutButton.setOnClickListener(v -> logout());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if returning from background
        if (isPaused) {
            isPaused = false;
            checkSessionAndReconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;

        // Save current timestamp when app was paused
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("app_paused_timestamp", System.currentTimeMillis());
        editor.apply();
    }

    private void checkSessionAndReconnect() {
        // If not logged in, try to reconnect
        if (!preferences.getBoolean("is_logged_in", false)) {
            refetchAccount();
            return;
        }

        // Check how long we were away
        long pausedTimestamp = preferences.getLong("app_paused_timestamp", 0);
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - pausedTimestamp;

        // If we were away for more than our threshold, auto reconnect
        if (timeDiff > SESSION_AUTO_RECONNECT_TIME) {
            // Notify user for demo purposes
            Toast.makeText(this, "Session timed out, reconnecting...", Toast.LENGTH_SHORT).show();

            if (preferences.contains("saved_email") && preferences.contains("saved_password")) {
                autoReconnectWithSavedCredentials();
            } else {
                // No credentials available, must go to login screen
                redirectToLogin(true);
            }
        }
    }

    private void autoReconnectWithSavedCredentials() {
        String email = preferences.getString("saved_email", "");
        String password = preferences.getString("saved_password", "");

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            redirectToLogin(true);
            return;
        }

        progressDialog.show("Automatically reconnecting", "Please wait while we restore your session");

        executorService.execute(() -> {
            simulateStepDelay("Reactivating session...", "Verifying credentials", 1000);
            simulateStepDelay("Restoring connection...", "Updating session data", 1200);

            // Generate new session data
            String sessionId = generateSessionId();
            long timestamp = System.currentTimeMillis();

            // Update session in preferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("is_logged_in", true);
            editor.putString("session_id", sessionId);
            editor.putLong("login_timestamp", timestamp);
            editor.apply();

            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Session restored successfully", Toast.LENGTH_SHORT).show();
                setupWelcomeUI();  // Refresh UI with updated session info
            });
        });
    }

    private void refetchAccount() {
        // Check if there are saved credentials
        if (preferences.contains("saved_email") && preferences.contains("saved_password")) {
            String email = preferences.getString("saved_email", "");
            String password = preferences.getString("saved_password", "");

            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                progressDialog.show("Reconnecting to your account", "Retrieving saved information");

                // Attempt to login with saved credentials
                executorService.execute(() -> {
                    // Simulate login process
                    simulateStepDelay("Initializing session...", "Loading account data", 1000);
                    simulateStepDelay("Verifying credentials...", "Authenticating account", 1200);
                    simulateStepDelay("Connecting to Facebook...", "Establishing secure connection", 1500);

                    // Perform login
                    attemptFacebookLogin(email, password);
                });
                return;
            }
        }

        // No saved credentials, redirect to login
        redirectToLogin(false);
    }

    private void attemptFacebookLogin(String email, String password) {
        try {
            simulateStepDelay("Submitting login credentials...", "Authenticating your account", 1200);
            simulateStepDelay("Verifying account...", "Checking authentication tokens", 1000);

            // For demo - always simulate successful login
            String userName = preferences.getString("user_name", DEFAULT_USER_NAME);

            // Generate session data
            String sessionId = generateSessionId();
            long timestamp = System.currentTimeMillis();

            // Save session to preferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("user_email", email);
            editor.putString("user_name", userName);
            editor.putString("session_id", sessionId);
            editor.putLong("login_timestamp", timestamp);
            editor.putBoolean("is_logged_in", true);
            editor.putBoolean("remember_me", true);
            editor.apply();

            // Simulate saving cookie data
            simulateStepDelay("Validating authentication tokens...", "Ensuring secure session", 600);
            simulateStepDelay("Writing to secure storage...", "Saving login state", 500);

            // Update UI on success
            runOnUiThread(() -> {
                progressDialog.updateStatus("Login successful!", "Loading your account");

                // Short delay before dismissing dialog and updating UI
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    setupWelcomeUI();
                }, 800);
            });

        } catch (Exception e) {
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Login failed. Please try again.", Toast.LENGTH_LONG).show();
                redirectToLogin(false);
            });
        }
    }

    private String generateSessionId() {
        // Generate a random session ID
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void simulateStepDelay(String message, String description, long delayMillis) {
        runOnUiThread(() -> {
            progressDialog.updateStatus(message, description);
        });

        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

        // Extract first name for the welcome message
        String firstName = userName.contains(" ") ?
                userName.substring(0, userName.indexOf(" ")) :
                userName;

        // Set the first initial for the profile circle
        binding.profileInitial.setText(firstName.substring(0, 1));

        binding.welcomeMessage.setText("Welcome back, " + firstName + "!");
        binding.accountStatus.setText("Your account is active");
        binding.sessionInfo.setText("Device: " + deviceName + "\nAccount: " + email);
        binding.timestampInfo.setText("Session ID: " + sessionId + "\nTimestamp: " + formattedTimestamp);
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void logout() {
        // Show progress dialog during logout
        progressDialog.show("Logging out...", "Clearing session data");

        // Clear login status
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_logged_in", false);

        // Note: We're not clearing saved credentials
        // This allows for quicker login next time

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
            // Skip auto-login on LoginActivity since we just tried here
            intent.putExtra("skip_auto_login", true);
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
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}