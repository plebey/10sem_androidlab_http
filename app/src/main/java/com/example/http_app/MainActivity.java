package com.example.http_app;

import android.Manifest;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {

    String[] extensions = {"mp4","m4v", "mkv", "webm"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasPermission();

        setContentView(R.layout.activity_main);
        SearchView searchView = findViewById(R.id.searchView);
        WebView webView = findViewById(R.id.webView);
        LinkedBlockingQueue<String> toShow = new LinkedBlockingQueue<String>();
        searchView.setOnQueryTextListener(new Listener(toShow, webView));
    }



    private class Listener implements SearchView.OnQueryTextListener {
        private final LinkedBlockingQueue<String> toShow;
        private final WebView webView;
        public Listener(LinkedBlockingQueue<String> queue, WebView webView) {
            this.toShow = queue;
            this.webView = webView;
        }
        private boolean arrCheck(String[] arr, String s){
            return Arrays.asList(arr).contains(s);
        }
        @Override
        public boolean onQueryTextSubmit(String s) {
            String ext = s.substring(s.lastIndexOf('.') + 1);
            if (!arrCheck(extensions,ext)) {
                UrlConnector urlConnector = new UrlConnector(s, toShow);
                urlConnector.start();

                runOnUiThread(() -> {
                    while (true) {
                        try {
                            String s1 = toShow.poll();
                            if (s1 != null) {
                                webView.loadData(s1, "text/plain", "utf-8");
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                });
            } else {
                downloadVid(s);
            }

            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }

        private void downloadVid(String url) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            String title = URLUtil.guessFileName(url, null, "video/mp4");
            request.setTitle(title);
            request.setDescription("some video for you");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);

            Toast.makeText(MainActivity.this, "Идёт скачивание...", Toast.LENGTH_SHORT).show();
        }
    }
    private void hasPermission() { // runs in onCreate()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.INTERNET,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        }
    }
}
