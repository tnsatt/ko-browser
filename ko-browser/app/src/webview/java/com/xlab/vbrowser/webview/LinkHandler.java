/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 Copyright by MonnyLab */

package com.xlab.vbrowser.webview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import android.view.View;
import android.webkit.WebView;

import com.xlab.vbrowser.web.IWebView;

/* package */ class LinkHandler implements View.OnLongClickListener {
    private final WebView webView;
    private @Nullable
    IWebView.Callback callback = null;

    public LinkHandler(final WebView webView) {
        this.webView = webView;
    }

    public void setCallback(final @Nullable IWebView.Callback callback) {
        this.callback = callback;
    }

    @Override
    public boolean onLongClick(View v) {
        if (callback == null) {
            return false;
        }

        final WebView.HitTestResult hitTestResult = webView.getHitTestResult();

        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                final String linkURL = hitTestResult.getExtra();
                callback.onLongPress(new IWebView.HitTarget(true, linkURL, false, null));
                return false;

            case WebView.HitTestResult.IMAGE_TYPE:
                final String imageURL = hitTestResult.getExtra();
                callback.onLongPress(new IWebView.HitTarget(false, null, true, imageURL));
                return false;

            case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                // hitTestResult.getExtra() contains only the image URL, and not the link
                // URL. Internally, WebView's HitTestData contains both, but they only
                // make it available via requestFocusNodeHref...
                final Message message = new Message();
                message.setTarget(new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        final Bundle data = msg.getData();
                        final String url = data.getString("url");
                        final String src = data.getString("src");

                        if (url == null || src == null) {
                            throw new IllegalStateException("WebView did not supply url or src for image link");
                        }

                        if (callback != null) {
                            callback.onLongPress(new IWebView.HitTarget(true, url, true, src));
                        }
                    }
                });

                webView.requestFocusNodeHref(message);
                return false;

            default:
                return false;
        }
    }
}