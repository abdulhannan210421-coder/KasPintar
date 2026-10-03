package com.kaspintar.app;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.os.Environment;
import android.os.Build;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import android.util.Base64;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = this.bridge.getWebView();
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidExport");
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void saveFile(String base64Data, String fileName, String mimeType) {
            try {
                byte[] fileData = Base64.decode(base64Data, Base64.DEFAULT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    ContentResolver resolver = getContentResolver();
                    Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                    if (uri != null) {
                        OutputStream os = resolver.openOutputStream(uri);
                        if (os != null) {
                            os.write(fileData);
                            os.flush();
                            os.close();
                        }
                    }
                } else {
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs();
                    }
                    File file = new File(downloadsDir, fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(fileData);
                    fos.flush();
                    fos.close();
                }

                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "File tersimpan di Downloads: " + fileName, Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }
    }
}