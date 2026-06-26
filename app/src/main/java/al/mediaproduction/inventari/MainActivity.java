package al.mediaproduction.inventari;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipe;
    private static final String START_URL = "http://13.140.152.29/";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipe = findViewById(R.id.swipe);
        webView = findViewById(R.id.webview);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);            // localStorage (token JWT)
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setSupportZoom(false);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        // Mban navigimin brenda app-it
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String url = req.getUrl().toString();
                if (url.startsWith("http://13.140.152.29") || url.startsWith("https://13.140.152.29")) {
                    return false; // ngarko brenda WebView
                }
                return false;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                swipe.setRefreshing(false);
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        // Interface per ruajtjen e skedareve (Excel) nga blob
        webView.addJavascriptInterface(new FileSaver(), "AndroidSaver");

        // Shkarkimet: blob (Excel nga SheetJS) + http
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (url.startsWith("blob:")) {
                fetchBlobAndSave(url, mimetype);
            } else if (url.startsWith("data:")) {
                saveDataUrl(url, mimetype);
            } else {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                if (dm != null) dm.enqueue(request);
                toast("Duke shkarkuar " + fileName);
            }
        });

        // Pull-to-refresh
        swipe.setOnRefreshListener(() -> webView.reload());

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(START_URL);
        }
    }

    // Lexon blob-in ne JS, e kthen ne base64 dhe ia kalon FileSaver-it
    private void fetchBlobAndSave(String blobUrl, String mimetype) {
        String name = "inventar_" + System.currentTimeMillis() + extFor(mimetype);
        String js = "javascript:(function(){"
                + "var x=new XMLHttpRequest();"
                + "x.open('GET','" + blobUrl + "',true);"
                + "x.responseType='blob';"
                + "x.onload=function(){"
                + "  var r=new FileReader();"
                + "  r.onloadend=function(){"
                + "    var b64=r.result.split(',')[1];"
                + "    AndroidSaver.saveBase64(b64,'" + name + "','" + mimetype + "');"
                + "  };"
                + "  r.readAsDataURL(x.response);"
                + "};"
                + "x.send();"
                + "})()";
        webView.evaluateJavascript(js, null);
    }

    private void saveDataUrl(String dataUrl, String mimetype) {
        try {
            String b64 = dataUrl.substring(dataUrl.indexOf(",") + 1);
            new FileSaver().saveBase64(b64, "inventar_" + System.currentTimeMillis() + extFor(mimetype), mimetype);
        } catch (Exception e) {
            toast("Gabim ne shkarkim");
        }
    }

    private String extFor(String mimetype) {
        if (mimetype == null) return ".bin";
        if (mimetype.contains("sheet") || mimetype.contains("excel")) return ".xlsx";
        if (mimetype.contains("pdf")) return ".pdf";
        if (mimetype.contains("csv")) return ".csv";
        return ".bin";
    }

    private void toast(String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    // Klasa qe ruan base64 → dosja Downloads
    public class FileSaver {
        @JavascriptInterface
        public void saveBase64(String base64, String fileName, String mimeType) {
            try {
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        OutputStream os = getContentResolver().openOutputStream(uri);
                        if (os != null) { os.write(data); os.close(); }
                    }
                } else {
                    java.io.File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!dir.exists()) dir.mkdirs();
                    java.io.File file = new java.io.File(dir, fileName);
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                    fos.write(data); fos.close();
                }
                toast("U ruajt te Downloads: " + fileName);
            } catch (Exception e) {
                toast("Gabim ne ruajtje: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    // Butoni Back → kthehet ne faqen e meparshme te WebView
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
