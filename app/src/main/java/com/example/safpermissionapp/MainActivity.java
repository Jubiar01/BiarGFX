package com.example.safpermissionapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String ML_PACKAGE = "com.mobile.legends";
    private static final String TARGET_PATH = "files/dragon2017/assets";
    private static final int REQUEST_MANAGE_ALL_FILES = 100;

    private TextView statusTextView;
    private Button allFilesAccessButton;
    private Button requestButton;
    private Button accessButton;
    private Uri targetUri = null;

    private final ActivityResultLauncher<Intent> safLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleSafResult(result.getData());
                } else {
                    statusTextView.setText("Permission denied or canceled");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        allFilesAccessButton = findViewById(R.id.allFilesAccessButton);
        requestButton = findViewById(R.id.requestButton);
        accessButton = findViewById(R.id.accessButton);

        allFilesAccessButton.setOnClickListener(v -> requestAllFilesAccess());
        requestButton.setOnClickListener(v -> requestSafPermission());
        accessButton.setOnClickListener(v -> accessFiles());

        // Initial state based on permissions
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    private void checkPermissions() {
        // First check if we have All Files Access permission (Android 11+)
        if (hasAllFilesAccessPermission()) {
            statusTextView.setText("All Files Access permission granted. You can access all files.");
            allFilesAccessButton.setEnabled(false);
            // Keep SAF button enabled as fallback for Android 14
            requestButton.setEnabled(true);
            accessButton.setEnabled(true);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            allFilesAccessButton.setEnabled(true);
        } else {
            allFilesAccessButton.setEnabled(false);
        }

        // If not, check for SAF permissions
        checkExistingPermissions();
    }

    private boolean hasAllFilesAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return false;
    }

    private void requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                new AlertDialog.Builder(this)
                        .setTitle("All Files Access")
                        .setMessage("This app needs permission to access all files on your device to read Mobile Legends files. Please grant 'Allow access to manage all files' on the next screen.")
                        .setPositiveButton("Continue", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } catch (Exception e) {
                // If settings screen not available, fall back to SAF
                statusTextView.setText("Could not open All Files Access settings: " + e.getMessage());
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkExistingPermissions() {
        List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
        for (UriPermission permission : permissions) {
            Uri uri = permission.getUri();
            if (uri.toString().contains(ML_PACKAGE) &&
                    (uri.toString().contains(TARGET_PATH) || isParentOf(uri, ML_PACKAGE, TARGET_PATH))) {
                targetUri = uri;
                statusTextView.setText("SAF Permission already granted");
                accessButton.setEnabled(true);
                return;
            }
        }
        statusTextView.setText("No permissions granted. Please choose one of the permission methods above.");
        accessButton.setEnabled(false);
    }

    private boolean isParentOf(Uri uri, String packageName, String path) {
        try {
            DocumentFile docFile = DocumentFile.fromTreeUri(this, uri);
            if (docFile == null) return false;

            // Check if it contains the package name
            String uriString = uri.toString().toLowerCase();
            if (!uriString.contains(packageName.toLowerCase())) {
                return false;
            }

            // Now check if we can navigate to the target path
            String[] segments = path.split("/");
            DocumentFile current = docFile;

            for (String segment : segments) {
                if (segment.isEmpty()) continue;

                boolean found = false;
                for (DocumentFile child : current.listFiles()) {
                    if (child.isDirectory() &&
                            child.getName() != null &&
                            child.getName().equalsIgnoreCase(segment)) {
                        current = child;
                        found = true;
                        break;
                    }
                }

                if (!found) return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void requestSafPermission() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Force advanced document picker UI
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra("android.provider.extra.SHOW_ADVANCED", true);

        // Android 13+ specific flags to show all files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.putExtra("android.provider.extra.INITIAL_URI", buildAndroidDataUri());
        }
        // For Android 10 and 11, attempt to set initial URI
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri initialUri = buildAndroidDataUri();
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
        }

        safLauncher.launch(intent);
    }

    private Uri buildAndroidDataUri() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Focus on Android/data folder
            return DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    "primary:Android/data"
            );
        }
        return null;
    }

    private void handleSafResult(Intent data) {
        Uri treeUri = data.getData();
        if (treeUri == null) {
            statusTextView.setText("Failed to get URI");
            return;
        }

        // Debug: Show the received URI
        statusTextView.setText("Received URI: " + treeUri.toString());

        try {
            // Take persistable permissions
            getContentResolver().takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            targetUri = treeUri;

            // Navigate to find the correct directory
            DocumentFile docFile = DocumentFile.fromTreeUri(this, treeUri);
            statusTextView.setText(statusTextView.getText() + "\nNavigating from selected directory...");

            // If we need to navigate to the ML directory (user selected parent folder)
            if (!treeUri.toString().contains(ML_PACKAGE) || !treeUri.toString().contains(TARGET_PATH)) {
                statusTextView.setText(statusTextView.getText() +
                        "\nSelected directory doesn't directly contain the target path." +
                        "\nAttempting to navigate to: " + ML_PACKAGE + "/" + TARGET_PATH);

                // Try to navigate to the correct directory
                DocumentFile mlDir = navigateToTargetDirectory(docFile, ML_PACKAGE, TARGET_PATH);
                if (mlDir != null) {
                    statusTextView.setText(statusTextView.getText() +
                            "\nSuccessfully found target directory!");
                    accessButton.setEnabled(true);
                } else {
                    statusTextView.setText(statusTextView.getText() +
                            "\nCould not find the exact target directory, but continuing with selected directory." +
                            "\nPlease make sure you selected Android/data or a parent of " + ML_PACKAGE + "/" + TARGET_PATH);
                    accessButton.setEnabled(true);
                }
            } else {
                statusTextView.setText("Permission granted successfully for exact target directory");
                accessButton.setEnabled(true);
            }

        } catch (SecurityException e) {
            statusTextView.setText("Failed to take persistable permission: " + e.getMessage());
        }
    }

    private DocumentFile navigateToTargetDirectory(DocumentFile rootDir, String packageName, String targetPath) {
        try {
            if (rootDir == null || !rootDir.exists() || !rootDir.isDirectory()) {
                return null;
            }

            // First, try to find the package directory
            DocumentFile packageDir = null;

            // If we're already in Android/data, look for the package
            for (DocumentFile file : rootDir.listFiles()) {
                if (file.isDirectory() && file.getName() != null &&
                        file.getName().equalsIgnoreCase(packageName)) {
                    packageDir = file;
                    break;
                }
            }

            // If we didn't find it, maybe we need to navigate to Android/data first
            if (packageDir == null) {
                // Try to find Android directory
                DocumentFile androidDir = null;
                for (DocumentFile file : rootDir.listFiles()) {
                    if (file.isDirectory() && file.getName() != null &&
                            file.getName().equalsIgnoreCase("Android")) {
                        androidDir = file;
                        break;
                    }
                }

                // If found Android, look for data
                if (androidDir != null) {
                    DocumentFile dataDir = null;
                    for (DocumentFile file : androidDir.listFiles()) {
                        if (file.isDirectory() && file.getName() != null &&
                                file.getName().equalsIgnoreCase("data")) {
                            dataDir = file;
                            break;
                        }
                    }

                    // If found data, look for package
                    if (dataDir != null) {
                        for (DocumentFile file : dataDir.listFiles()) {
                            if (file.isDirectory() && file.getName() != null &&
                                    file.getName().equalsIgnoreCase(packageName)) {
                                packageDir = file;
                                break;
                            }
                        }
                    }
                }
            }

            // If we found the package directory, navigate to the target path
            if (packageDir != null) {
                String[] segments = targetPath.split("/");
                DocumentFile currentDir = packageDir;

                for (String segment : segments) {
                    if (segment.isEmpty()) continue;

                    DocumentFile nextDir = null;
                    for (DocumentFile file : currentDir.listFiles()) {
                        if (file.isDirectory() && file.getName() != null &&
                                file.getName().equalsIgnoreCase(segment)) {
                            nextDir = file;
                            break;
                        }
                    }

                    if (nextDir == null) return currentDir; // Return as far as we got
                    currentDir = nextDir;
                }

                return currentDir;
            }

            return null;
        } catch (Exception e) {
            statusTextView.setText(statusTextView.getText() + "\nError navigating: " + e.getMessage());
            return null;
        }
    }

    private void accessFiles() {
        StringBuilder output = new StringBuilder();

        // If we have All Files Access, try direct file access first
        if (hasAllFilesAccessPermission()) {
            output.append("Trying with All Files Access permission\n\n");
            try {
                String directPath = getMLDirectPath();
                output.append("Path: ").append(directPath).append("\n");

                File directory = new File(directPath);
                if (!directory.exists()) {
                    output.append("Directory does not exist using direct path. Trying alternative paths...\n");

                    // Try alternative paths for Android 14
                    String[] alternativePaths = {
                            Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + ML_PACKAGE + "/" + TARGET_PATH,
                            "/storage/emulated/0/Android/data/" + ML_PACKAGE + "/" + TARGET_PATH,
                            "/sdcard/Android/data/" + ML_PACKAGE + "/" + TARGET_PATH
                    };

                    boolean found = false;
                    for (String path : alternativePaths) {
                        directory = new File(path);
                        output.append("Trying path: ").append(path).append("\n");
                        if (directory.exists()) {
                            output.append("Found directory at: ").append(path).append("\n");
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        output.append("Could not find directory with direct access, falling back to SAF.\n\n");
                        accessWithSafPermission(output);
                        return;
                    }
                }

                if (!directory.canRead()) {
                    output.append("Cannot read directory: Permission denied\n");
                    output.append("Falling back to SAF...\n\n");
                    accessWithSafPermission(output);
                    return;
                }

                File[] files = directory.listFiles();
                if (files == null || files.length == 0) {
                    output.append("Directory is empty or cannot list files\n");
                    output.append("Falling back to SAF...\n\n");
                    accessWithSafPermission(output);
                    return;
                }

                output.append("Successfully accessed directory!\n");
                output.append("Files in directory:\n");
                for (File file : files) {
                    String type = file.isDirectory() ? "[DIR] " : "[FILE] ";
                    output.append(type).append(file.getName()).append("\n");
                }

                Toast.makeText(this, "Successfully accessed files", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                output.append("Error accessing files: ").append(e.getMessage()).append("\n");
                output.append("Falling back to SAF...\n\n");
                accessWithSafPermission(output);
            }
        }
        // Otherwise use SAF
        else if (targetUri != null) {
            accessWithSafPermission(output);
        }
        else {
            output.append("No permissions granted. Please request permission first.");
        }

        statusTextView.setText(output.toString());
    }

    private String getMLDirectPath() {
        // For Android 14, try multiple path formats
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return "/storage/emulated/0/Android/data/" + ML_PACKAGE + "/" + TARGET_PATH;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.getExternalStorageDirectory().getPath() +
                    "/Android/data/" + ML_PACKAGE + "/" + TARGET_PATH;
        } else {
            return Environment.getExternalStorageDirectory().getPath() +
                    "/Android/data/" + ML_PACKAGE + "/" + TARGET_PATH;
        }
    }

    private void accessWithSafPermission(StringBuilder output) {
        try {
            output.append("Using SAF permission\n\n");

            if (targetUri == null) {
                // Check if we have any SAF permissions that might work
                List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
                for (UriPermission permission : permissions) {
                    if (permission.isReadPermission() && permission.isWritePermission()) {
                        targetUri = permission.getUri();
                        output.append("Found potential SAF permission: ").append(targetUri).append("\n");
                        break;
                    }
                }

                if (targetUri == null) {
                    output.append("No SAF permissions available. Please request SAF permission first.");
                    return;
                }
            }

            DocumentFile directory = DocumentFile.fromTreeUri(this, targetUri);

            if (directory == null) {
                output.append("Directory not found (null reference)");
                return;
            }

            if (!directory.exists()) {
                output.append("Directory does not exist");
                return;
            }

            if (!directory.canRead()) {
                output.append("Cannot read directory");
                return;
            }

            // Try to navigate to the exact target directory if needed
            if (!targetUri.toString().toLowerCase().contains(ML_PACKAGE.toLowerCase()) ||
                    !targetUri.toString().toLowerCase().contains(TARGET_PATH.toLowerCase())) {

                output.append("Navigating to target directory...\n");
                DocumentFile mlDir = navigateToTargetDirectory(directory, ML_PACKAGE, TARGET_PATH);
                if (mlDir != null) {
                    directory = mlDir;
                    output.append("Successfully navigated to target directory\n\n");
                } else {
                    output.append("Could not navigate to exact target directory, showing contents of selected directory\n\n");
                }
            }

            DocumentFile[] files = directory.listFiles();
            if (files.length == 0) {
                output.append("Directory is empty");
                return;
            }

            output.append("Files in directory:\n");
            for (DocumentFile file : files) {
                String type = file.isDirectory() ? "[DIR] " : "[FILE] ";
                output.append(type).append(file.getName()).append("\n");
            }

            Toast.makeText(this, "Successfully accessed files", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            output.append("Error accessing files: ").append(e.getMessage());
        }
    }

    public boolean writeToFile(DocumentFile directory, String filename, byte[] data) {
        if (directory == null || !directory.exists() || !directory.canWrite()) {
            return false;
        }

        DocumentFile file = directory.findFile(filename);
        if (file == null) {
            file = directory.createFile("application/octet-stream", filename);
        }

        if (file == null) {
            return false;
        }

        try (OutputStream out = getContentResolver().openOutputStream(file.getUri())) {
            if (out == null) return false;
            out.write(data);
            out.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public byte[] readFromFile(DocumentFile directory, String filename) {
        if (directory == null || !directory.exists() || !directory.canRead()) {
            return null;
        }

        DocumentFile file = directory.findFile(filename);
        if (file == null || !file.exists() || !file.canRead()) {
            return null;
        }

        try (InputStream in = getContentResolver().openInputStream(file.getUri())) {
            if (in == null) return null;

            int size = (int) file.length();
            byte[] buffer = new byte[size];
            int bytesRead = in.read(buffer);

            if (bytesRead != size) {
                byte[] actual = new byte[bytesRead];
                System.arraycopy(buffer, 0, actual, 0, bytesRead);
                return actual;
            }

            return buffer;
        } catch (Exception e) {
            return null;
        }
    }
}