package com.monopoly63.hablas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 42;
    private ValueCallback<Uri[]> filePathCallback;
    private WebView webView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        requestAudioPermissionIfNeeded();
        setupWebView();
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#020203"));
        window.setNavigationBarColor(Color.parseColor("#020203"));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void requestAudioPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 10);
        } else if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#020203"));
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setContentView(webView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        if (Build.VERSION.SDK_INT >= 23) s.setOffscreenPreRaster(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        if (Build.VERSION.SDK_INT >= 26) s.setSafeBrowsingEnabled(true);
        webView.addJavascriptInterface(new HablasBridge(), "HablasAndroid");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                try { startActivityForResult(Intent.createChooser(intent, "Select music"), FILE_CHOOSER_REQUEST); }
                catch (Exception e) { filePathCallback = null; return false; }
                return true;
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER_REQUEST) return;
        Uri[] results = null;
        if (resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                results = new Uri[count];
                for (int i = 0; i < count; i++) results[i] = data.getClipData().getItemAt(i).getUri();
            } else if (data.getData() != null) results = new Uri[]{data.getData()};
        }
        if (filePathCallback != null) { filePathCallback.onReceiveValue(results); filePathCallback = null; }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }

    public class HablasBridge {
        @JavascriptInterface
        public String scanDeviceMusic() {
            JSONArray array = new JSONArray();
            try {
                Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    projection = new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.RELATIVE_PATH,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.IS_MUSIC
                    };
                } else {
                    projection = new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.IS_MUSIC
                    };
                }

                String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DURATION + " > ?";
                String[] args = new String[]{"30000"};
                String sort = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MediaStore.Audio.Media.RELATIVE_PATH : MediaStore.Audio.Media.DATA) + " COLLATE NOCASE ASC, " + MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC";

                try (Cursor c = getContentResolver().query(collection, projection, selection, args, sort)) {
                    if (c == null) return array.toString();
                    int idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    int titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                    int artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                    int albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                    int durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                    int sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                    int pathCol = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        ? c.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
                        : c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                    int displayCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);

                    while (c.moveToNext()) {
                        long id = c.getLong(idCol);
                        String title = clean(c.getString(titleCol), "Unknown Title");
                        String artist = clean(c.getString(artistCol), "Unknown Artist");
                        String album = clean(c.getString(albumCol), "Unknown Album");
                        long durationMs = c.getLong(durationCol);
                        long size = c.getLong(sizeCol);
                        String rawPath = clean(c.getString(pathCol), "Device Music/").replace('\\', '/');
                        String display = clean(c.getString(displayCol), title);

                        String folderPath;
                        String relativePath;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            folderPath = rawPath.replaceAll("/+$", "");
                            if (folderPath.length() == 0) folderPath = "Device Music";
                            relativePath = folderPath + "/" + display;
                        } else {
                            int slash = rawPath.lastIndexOf('/');
                            folderPath = slash > 0 ? rawPath.substring(0, slash) : "Device Music";
                            relativePath = slash > 0 ? rawPath.substring(slash + 1) : display;
                        }

                        Uri contentUri = ContentUris.withAppendedId(collection, id);
                        JSONObject o = new JSONObject();
                        o.put("id", "native:" + id + ":" + size);
                        o.put("url", contentUri.toString());
                        o.put("title", title);
                        o.put("artist", artist);
                        o.put("album", album);
                        o.put("duration", Math.max(0, durationMs / 1000.0));
                        o.put("coverUrl", JSONObject.NULL);
                        o.put("folderPath", folderPath);
                        o.put("relativePath", relativePath);
                        array.put(o);
                    }
                }
            } catch (Exception ignored) {
            }
            return array.toString();
        }

        private String clean(String value, String fallback) {
            if (value == null) return fallback;
            String trimmed = value.trim();
            if (trimmed.length() == 0 || trimmed.equalsIgnoreCase("<unknown>")) return fallback;
            return trimmed;
        }
    }

}
