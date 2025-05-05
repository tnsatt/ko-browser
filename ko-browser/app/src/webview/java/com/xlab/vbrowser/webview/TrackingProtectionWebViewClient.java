/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 Copyright by MonnyLab */
package com.xlab.vbrowser.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.WorkerThread;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.novacloud.data.adblock.RuleSet;
import com.xlab.vbrowser.webview.matcher.UrlMatcher;
import com.xlab.vbrowser.web.IWebView;
import com.xlab.vbrowser.z.ad.AdblockRuleSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class TrackingProtectionWebViewClient extends WebViewClient {
    private static volatile UrlMatcher MATCHER;

    public static void triggerPreload(final Context context) {
        // Only trigger loading if MATCHER is null. (If it's null, MATCHER could already be loading,
        // but we don't have any way of being certain - and there's no real harm since we're not
        // blocking anything else.)
        if (MATCHER == null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    // We don't need the result here - we just want to trigger loading
                    getMatcher(context);
                    return null;
                }
            }.execute();
        }
    }

    @WorkerThread private static synchronized UrlMatcher getMatcher(final Context context) {
        if (MATCHER == null) {
            MATCHER = UrlMatcher.loadMatcher(context, com.xlab.vbrowser.R.raw.blocklist, new int[] { com.xlab.vbrowser.R.raw.google_mapping }, com.xlab.vbrowser.R.raw.entitylist);
        }
        return MATCHER;
    }

    private boolean blockingEnabled;
    /* package */ String currentPageURL;
    protected IWebView.Callback callback;

    /* package */ TrackingProtectionWebViewClient(final Context context) {
        // Hopefully we have loaded background data already. We call triggerPreload() to try to trigger
        // background loading of the lists as early as possible.
        triggerPreload(context);

        this.blockingEnabled = true;
    }

    public void setCallback(IWebView.Callback callback) {
        this.callback = callback;
    }

    public void setBlockingEnabled(boolean enabled) {
        this.blockingEnabled = enabled;
    }

    public boolean isBlockingEnabled() {
        return blockingEnabled;
    }

//    @Override
//    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        if (!blockingEnabled) {
//            return super.shouldOverrideUrlLoading(view, url);
//        }
//        AdblockRuleSet rulesetAdBlocker = AdblockRuleSet.getInstance();
//        RuleSet ruleSet = rulesetAdBlocker.getRuleSet();
//        if(!ruleSet.matchesWhitelist(url) && ruleSet.matchesBlacklist(url)){
//            view.loadUrl("file:///android_asset/www/adblock.html?url="+url);
//            return true;
//        }
//        return super.shouldOverrideUrlLoading(view, url);
//    }

    @Override
    public WebResourceResponse shouldInterceptRequest(final WebView view, final WebResourceRequest request) {
        if (!blockingEnabled) {
            return super.shouldInterceptRequest(view, request);
        }

        final Uri resourceUri = request.getUrl();

        if (resourceUri == null || currentPageURL == null) {
            return super.shouldInterceptRequest(view, request);
        }

        //(thuan): We don' use this here because of some request as video streaming use blob:// to request data
        //in the future, we are not sure chromium add others schema.
        /*
        // shouldInterceptRequest() might be called _before_ onPageStarted or shouldOverrideUrlLoading
        // are called (this happens when the webview is first shown). However we are notified of the URL
        // via notifyCurrentURL in that case.
        final String scheme = resourceUri.getScheme();

        if (!request.isForMainFrame() &&
                !scheme.equals("http") && !scheme.equals("https")) {
            // Block any malformed non-http(s) URIs. WebView will already ignore things like market: URLs,
            // but not in all cases (malformed market: URIs, such as market:://... will still end up here).
            // (Note: data: URIs are automatically handled by WebView, and won't end up here either.)
            // file:// URIs are disabled separately by setting WebSettings.setAllowFileAccess()
            return new WebResourceResponse(null, null, null);
        }*/

        // WebView always requests a favicon, even though it won't be used anywhere. This check
        // isn't able to block all favicons (some of them will be loaded using <link rel="shortcut icon">
        // with a custom URL which we can't match or detect), but reduces the amount of unnecessary
        // favicon loading that's performed.
        /*final String path = resourceUri.getPath();
        if (path != null && path.endsWith("/favicon.ico")) {
            return new WebResourceResponse(null, null, null);
        }*/

//        final UrlMatcher matcher = getMatcher(view.getContext());
//
//        // Don't block the main frame from being loaded. This also protects against cases where we
//        // open a link that redirects to another app (e.g. to the play store).
//        final Uri pageUri = Uri.parse(currentPageURL);
//        if ((!request.isForMainFrame()) &&
//                matcher.matches(resourceUri, pageUri)) {
//            if (callback != null) {
//                callback.countBlockedTracker();
//            }
//            return new WebResourceResponse(null, null, null);
//        }

//        final Uri pageUri = Uri.parse(currentPageURL);

        String url = resourceUri.toString();
        AdblockRuleSet rulesetAdBlocker = AdblockRuleSet.getInstance();
        RuleSet ruleSet = rulesetAdBlocker.getRuleSet();
        try {
            if (!ruleSet.matchesWhitelist(url) && ruleSet.matchesBlacklist(url)) {
                if (callback != null) {
                    callback.countBlockedTracker();
                }
                if (request.isForMainFrame()) {
                    try {
                        InputStream inputStream = view.getContext().getAssets().open("www/adblock.html"); // Load local file
                        return new WebResourceResponse("text/html", "UTF-8", inputStream);
                    } catch (IOException e) {

                    }
                }
                return new WebResourceResponse(null, null, null);
            }
        }catch (Exception e){}
//        if (request.isForMainFrame()) { // Only modify the main HTML document
//            try {
//                // Open a connection to the original URL
//                URLConnection connection = new URL(url).openConnection();
//
//                Map<String, String> headers = request.getRequestHeaders();
//                for (Map.Entry<String, String> entry : headers.entrySet()) {
//                    connection.addRequestProperty(entry.getKey(), entry.getValue());
//                }
//                InputStream inputStream = connection.getInputStream();
//
//                // Read the original HTML
//                String html = readStream(inputStream);
//
//                Document doc = Jsoup.parse(html, url);
//                // Modify the HTML (Example: Inject a script)
//                String modifiedHtml = rulesetAdBlocker.removeAd(doc).html();
//
//                // Return the modified HTML
//                return new WebResourceResponse("text/html", "UTF-8",
//                        new ByteArrayInputStream(modifiedHtml.getBytes(StandardCharsets.UTF_8)));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        return super.shouldInterceptRequest(view, request);
    }
    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
//        byte[] buffer = new byte[8192];
//        while(inputStream.read(buffer)!=-1) {
//            stringBuilder.append(buffer);
//            buffer = new byte[8192];
//        }
        reader.close();
        return stringBuilder.toString();
    }
    /**
     * Notify that the user has requested a new URL. This MUST be called before loading a new URL
     * into the webview: sometimes content requests might begin before the WebView itself notifies
     * the WebViewClient via onpageStarted/shouldOverrideUrlLoading. If we don't know the current page
     * URL then the entitylist whitelists might not work if we're trying to load an explicitly whitelisted
     * page.
     */
    public void notifyCurrentURL(final String url) {
        currentPageURL = url;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (callback != null) {
            callback.resetBlockedTrackers();
        }

        currentPageURL = url;

        super.onPageStarted(view, url, favicon);
    }
}
