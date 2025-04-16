package com.example.safpermissionapp;

import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SafUtils {

    public static boolean hasPermissionForPath(Context context, String packageName, String path) {
        List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission permission : permissions) {
            Uri uri = permission.getUri();
            if (uri.toString().contains(packageName) &&
                    (uri.toString().contains(path) || isParentPath(uri.toString(), path))) {
                return permission.isReadPermission() && permission.isWritePermission();
            }
        }
        return false;
    }

    private static boolean isParentPath(String uriString, String targetPath) {
        String lowerUri = uriString.toLowerCase();
        String lowerPath = targetPath.toLowerCase();

        String[] pathSegments = lowerPath.split("/");
        for (String segment : pathSegments) {
            if (!segment.isEmpty() && !lowerUri.contains(segment)) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public static Uri getPersistedUri(Context context, String packageName, String path) {
        List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission permission : permissions) {
            Uri uri = permission.getUri();
            if (uri.toString().contains(packageName) &&
                    (uri.toString().contains(path) || isParentPath(uri.toString(), path))) {
                return uri;
            }
        }
        return null;
    }

    @Nullable
    public static DocumentFile getAppDataDirectory(Context context, Uri treeUri, String packageName, String relativePath) {
        DocumentFile rootDir = DocumentFile.fromTreeUri(context, treeUri);
        if (rootDir == null || !rootDir.exists()) {
            return null;
        }

        String[] pathSegments = relativePath.split("/");
        DocumentFile currentDir = rootDir;

        for (String segment : pathSegments) {
            if (segment.isEmpty()) continue;

            DocumentFile nextDir = findChildByName(currentDir, segment);
            if (nextDir == null) {
                nextDir = currentDir.createDirectory(segment);
                if (nextDir == null) return null;
            }
            currentDir = nextDir;
        }

        return currentDir;
    }

    @Nullable
    private static DocumentFile findChildByName(DocumentFile parentDir, String name) {
        DocumentFile[] children = parentDir.listFiles();
        for (DocumentFile child : children) {
            if (child.getName() != null && child.getName().equalsIgnoreCase(name)) {
                return child;
            }
        }
        return null;
    }

    public static Intent createSafIntent(String packageName, String path) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Force advanced document picker UI for Android 14
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra("android.provider.extra.SHOW_ADVANCED", true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri initialUri = buildTargetUri(packageName, path);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
        }

        return intent;
    }

    private static Uri buildTargetUri(String packageName, String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+
            String volumeName = "primary";
            String documentId = "Android/data/" + packageName + "/" + path;

            // Build document URI first
            Uri documentUri = DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    volumeName + ":" + documentId
            );

            // Convert to tree URI format for directory access
            String treeUriString = documentUri.toString().replace("/document/", "/tree/");
            return Uri.parse(treeUriString);
        } else {
            // For older Android versions, just navigate to Android/data folder
            return Uri.parse("content://com.android.externalstorage.documents/tree/primary:Android%2Fdata");
        }
    }

    public static boolean writeToFile(Context context, DocumentFile directory, String filename, byte[] data) {
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

        try (OutputStream out = context.getContentResolver().openOutputStream(file.getUri())) {
            if (out == null) return false;
            out.write(data);
            out.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    public static byte[] readFromFile(Context context, DocumentFile directory, String filename) {
        if (directory == null || !directory.exists() || !directory.canRead()) {
            return null;
        }

        DocumentFile file = directory.findFile(filename);
        if (file == null || !file.exists() || !file.canRead()) {
            return null;
        }

        try (InputStream in = context.getContentResolver().openInputStream(file.getUri())) {
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

    public static List<String> listFiles(DocumentFile directory) {
        List<String> fileNames = new ArrayList<>();
        if (directory != null && directory.exists() && directory.canRead()) {
            DocumentFile[] files = directory.listFiles();
            for (DocumentFile file : files) {
                if (file.getName() != null) {
                    fileNames.add(file.getName());
                }
            }
        }
        return fileNames;
    }
}