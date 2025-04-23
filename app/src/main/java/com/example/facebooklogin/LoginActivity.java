package com.example.facebooklogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import com.example.facebooklogin.databinding.ActivityLoginBinding;
import com.example.facebooklogin.utils.CustomProgressDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ExecutorService executorService;
    private SharedPreferences preferences;
    private boolean loginInProgress = false;
    private CustomProgressDialog progressDialog;

    // Default credentials from Python script
    private static final String DEFAULT_EMAIL = "entitled.guppy.ubrg@letterhaven.net";
    private static final String DEFAULT_PASSWORD = "pI@),/TX34/p";
    // Real account name for the default user
    private static final String DEFAULT_USER_NAME = "Sarah Johnson";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize executor service and preferences
        executorService = Executors.newSingleThreadExecutor();
        preferences = getSharedPreferences("facebook_login", MODE_PRIVATE);

        // Initialize custom progress dialog
        progressDialog = new CustomProgressDialog(this);

        // Pre-fill with default credentials
        binding.emailInput.setText(DEFAULT_EMAIL);
        binding.passwordInput.setText(DEFAULT_PASSWORD);

        // Set up login button
        binding.loginButton.setOnClickListener(v -> attemptLogin());

        // Check if user is returning to relogin
        if (getIntent().getBooleanExtra("relogin", false)) {
            progressDialog.show("Preparing to reconnect...");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progressDialog.dismiss();
            }, 1500);
        }
    }

    private void attemptLogin() {
        // Validate inputs
        if (loginInProgress) {
            return;
        }

        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError("Please enter your email");
            return;
        } else {
            binding.emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError("Please enter your password");
            return;
        } else {
            binding.passwordLayout.setError(null);
        }

        // Show loading indicator
        setLoginInProgress(true);
        progressDialog.show("Initializing login process...");

        // Simulate login process similar to the Python script
        executorService.execute(() -> {
            // Simulate environment preparation
            simulateStepDelay("Performing network security check...", 1000);
            simulateStepDelay("Analyzing connection profile...", 900);
            simulateStepDelay("Setting up device fingerprint...", 800);
            simulateStepDelay("Configuring browser emulation...", 700);
            simulateStepDelay("Establishing secure channel...", 1200);

            // Attempt actual login with Facebook
            attemptFacebookLogin(email, password);
        });
    }

    private void attemptFacebookLogin(String email, String password) {
        // Mock login for demo purposes
        runOnUiThread(() -> progressDialog.updateStatus("Connecting to Facebook servers..."));

        try {
            // Simulate network delay
            Thread.sleep(1500);

            runOnUiThread(() -> progressDialog.updateStatus("Submitting login credentials..."));
            Thread.sleep(1200);

            runOnUiThread(() -> progressDialog.updateStatus("Verifying account..."));
            Thread.sleep(1000);

            // Check if using default credentials or not
            if (DEFAULT_EMAIL.equals(email) && DEFAULT_PASSWORD.equals(password)) {
                // Simulate successful login with default credentials
                handleSuccessfulLogin(email, DEFAULT_USER_NAME);
            } else {
                // For demo - attempt to login with custom credentials
                // Generate a realistic name for the user
                String userName = generateRealisticName(email);
                handleSuccessfulLogin(email, userName);
            }
        } catch (InterruptedException e) {
            runOnUiThread(() -> {
                setLoginInProgress(false);
                progressDialog.dismiss();
                Snackbar.make(binding.getRoot(), "Login interrupted", Snackbar.LENGTH_LONG).show();
            });
        }
    }

    private void handleSuccessfulLogin(String email, String userName) {
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
        editor.apply();

        // Simulate saving cookie data
        simulateStepDelay("Validating authentication tokens...", 500);
        simulateStepDelay("Formatting session cookies...", 400);
        simulateStepDelay("Encrypting sensitive data...", 600);
        simulateStepDelay("Writing to secure storage...", 300);
        simulateStepDelay("Verifying data integrity...", 500);

        // Launch main activity
        runOnUiThread(() -> {
            setLoginInProgress(false);
            progressDialog.updateStatus("Login successful! Redirecting...");

            // Short delay before dismissing dialog and starting MainActivity
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progressDialog.dismiss();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 800);
        });
    }

    /**
     * Generate a realistic name based on email or use DEFAULT_USER_NAME for default credentials
     */
    private String generateRealisticName(String email) {
        // If using default credentials, return the default name
        if (DEFAULT_EMAIL.equals(email)) {
            return DEFAULT_USER_NAME;
        }

        // List of realistic first names
        String[] firstNames = {"Emma", "Liam", "Olivia", "Noah", "Ava", "William", "Sophia", "James",
                "Isabella", "Benjamin", "Mia", "Lucas", "Charlotte", "Henry", "Amelia",
                "Alexander", "Harper", "Michael", "Evelyn", "Daniel"};

        // List of realistic last names
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
                "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
                "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"};

        // Create a seed from the email to generate consistent names for the same email
        int seed = email.hashCode();
        Random random = new Random(seed);

        // Generate a random name
        String firstName = firstNames[random.nextInt(firstNames.length)];
        String lastName = lastNames[random.nextInt(lastNames.length)];

        return firstName + " " + lastName;
    }

    private String generateSessionId() {
        // Generate a random session ID
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void simulateStepDelay(String message, long delayMillis) {
        new Handler(Looper.getMainLooper()).post(() -> {
            progressDialog.updateStatus(message);
        });

        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setLoginInProgress(boolean inProgress) {
        loginInProgress = inProgress;

        if (inProgress) {
            binding.loginButton.setEnabled(false);
        } else {
            binding.loginButton.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        executorService.shutdown();
    }
}