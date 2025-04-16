package com.example.safpermissionapp;

import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String ML_PACKAGE = "com.mobile.legends";
    private static final String TARGET_PATH = "files/dragon2017/assets";
    private TextView statusTextView;
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
        requestButton = findViewById(R.id.requestButton);
        accessButton = findViewById(R.id.accessButton);

        requestButton.setOnClickListener(v -> requestSafPermission());
        accessButton.setOnClickListener(v -> accessFiles());

        checkExistingPermissions();
    }

    private void checkExistingPermissions() {
        List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
        for (UriPermission permission : permissions) {
            Uri uri = permission.getUri();
            if (uri.toString().contains(ML_PACKAGE) && uri.toString().contains(TARGET_PATH)) {
                targetUri = uri;
                statusTextView.setText("Permission already granted");
                accessButton.setEnabled(true);
                return;
            }
        }
        statusTextView.setText("Permission not granted");
        accessButton.setEnabled(false);
    }

    private void requestSafPermission() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri initialUri = buildTargetDirectoryUri();
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
        }

        safLauncher.launch(intent);
    }

    private Uri buildTargetDirectoryUri() {
        Uri baseUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String volumeName = "primary";
            String documentId = "Android/data/" + ML_PACKAGE + "/" + TARGET_PATH;

            // Use buildTreeDocumentUri instead of buildDocumentUri
            baseUri = DocumentsContract.buildTreeDocumentUri(
                    "com.android.externalstorage.documents",
                    volumeName + ":" + documentId
            );
        } else {
            baseUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary:Android%2Fdata");
        }
        return baseUri;
    }

    private void handleSafResult(Intent data) {
        Uri treeUri = data.getData();
        if (treeUri == null) {
            statusTextView.setText("Failed to get URI");
            return;
        }

        if (!isCorrectDirectory(treeUri)) {
            statusTextView.setText("Selected wrong directory. Please select the Mobile Legends assets directory.");
            return;
        }

        getContentResolver().takePersistableUriPermission(treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        targetUri = treeUri;
        statusTextView.setText("Permission granted successfully");
        accessButton.setEnabled(true);
    }

    private boolean isCorrectDirectory(Uri treeUri) {
        String uriPath = treeUri.toString().toLowerCase();
        return uriPath.contains(ML_PACKAGE.toLowerCase()) &&
                (uriPath.contains(TARGET_PATH.toLowerCase()) ||
                        isParentDirectory(treeUri));
    }

    private boolean isParentDirectory(Uri treeUri) {
        DocumentFile directory = DocumentFile.fromTreeUri(this, treeUri);
        if (directory == null) return false;

        String[] pathSegments = TARGET_PATH.split("/");
        DocumentFile currentDir = directory;

        for (String segment : pathSegments) {
            boolean found = false;
            DocumentFile[] files = currentDir.listFiles();
            for (DocumentFile file : files) {
                if (file.getName() != null && file.getName().equalsIgnoreCase(segment)) {
                    currentDir = file;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private void accessFiles() {
        if (targetUri == null) {
            statusTextView.setText("No permission granted");
            return;
        }

        DocumentFile directory = DocumentFile.fromTreeUri(this, targetUri);
        if (directory == null || !directory.exists()) {
            statusTextView.setText("Directory not found or permission revoked");
            return;
        }

        StringBuilder fileList = new StringBuilder("Files in directory:\n");
        DocumentFile[] files = directory.listFiles();

        if (files.length == 0) {
            statusTextView.setText("Directory is empty");
            return;
        }

        for (DocumentFile file : files) {
            fileList.append("- ").append(file.getName()).append("\n");
        }

        statusTextView.setText(fileList.toString());
        Toast.makeText(this, "Successfully accessed files", Toast.LENGTH_SHORT).show();
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