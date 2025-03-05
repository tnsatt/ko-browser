package com.tonyodev.fetchapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.xlab.vbrowser.z.Toast;

import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.FetchErrorUtils;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2rx.RxFetch;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

import io.reactivex.functions.Consumer;

public class GameFilesActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 400;

    private static final int groupId = 12;

    private View mainView;
    private TextView progressTextView;
    private ProgressBar progressBar;
    private Button startButton;
    private TextView labelTextView;

    private final ArrayMap<Integer, Integer> fileProgressMap = new ArrayMap<>();

    private RxFetch rxFetch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_files);
        setUpViews();
        rxFetch = ((App) getApplication()).getRxFetch();
        rxFetch.deleteAll();
        reset();
    }

    private void setUpViews() {
        progressTextView = (TextView) findViewById(R.id.progressTextView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        startButton = (Button) findViewById(R.id.startButton);
        labelTextView = (TextView) findViewById(R.id.labelTextView);
        mainView = findViewById(R.id.activity_loading);

        //Start downloads
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String label = (String) startButton.getText();
                final Context context = GameFilesActivity.this;
                if (label.equals(context.getString(R.string.reset))) {
                    reset();
                } else {
                    startButton.setVisibility(View.GONE);
                    labelTextView.setText(R.string.fetch_started);
                    checkStoragePermission();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        rxFetch.addListener(fetchListener);
        rxFetch.getDownloadsInGroup(groupId)
                .asFlowable()
                .subscribe(new Consumer<List<Download>>() {
                    @Override
                    public void accept(List<Download> downloads) throws Exception {
                        for (Download download : downloads) {
                            if (fileProgressMap.containsKey(download.getId())) {
                                fileProgressMap.put(download.getId(), download.getProgress());
                                updateUIWithProgress();
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        final Error error = FetchErrorUtils.getErrorFromThrowable(throwable);
                        Log.d("GamesFilesActivity", "Error:" + error.toString());
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        rxFetch.removeListener(fetchListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rxFetch.deleteAll();
        rxFetch.close();
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        } else {
            enqueueFiles();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE || grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            enqueueFiles();

        } else {
            Toast.makeText(this, R.string.permission_not_enabled, Toast.LENGTH_SHORT)
                    .show();
            reset();
        }
    }

    private void updateUIWithProgress() {
        final int totalFiles = fileProgressMap.size();
        final int completedFiles = getCompletedFileCount();

        progressTextView.setText(getResources()
                .getString(R.string.complete_over, completedFiles, totalFiles));

        final int progress = getDownloadProgress();

        progressBar.setProgress(progress);

        if (completedFiles == totalFiles) {
            labelTextView.setText(getString(R.string.fetch_done));
            startButton.setText(R.string.reset);
            startButton.setVisibility(View.VISIBLE);
        }
    }

    private int getDownloadProgress() {
        int currentProgress = 0;
        final int totalProgress = fileProgressMap.size() * 100;

        final Set<Integer> ids = fileProgressMap.keySet();

        for (int id : ids) {
            currentProgress += fileProgressMap.get(id);
        }

        currentProgress = (int) (((double) currentProgress / (double) totalProgress) * 100);

        return currentProgress;
    }

    private int getCompletedFileCount() {
        int count = 0;
        final Set<Integer> ids = fileProgressMap.keySet();

        for (int id : ids) {
            int progress = fileProgressMap.get(id);
            if (progress == 100) {
                count++;
            }
        }

        return count;
    }

    private void reset() {
        rxFetch.deleteAll();
        fileProgressMap.clear();
        progressBar.setProgress(0);
        progressTextView.setText("");
        labelTextView.setText(R.string.start_fetching);
        startButton.setText(R.string.start);
        startButton.setVisibility(View.VISIBLE);
    }

    private void enqueueFiles() {
        final List<Request> requestList = Data.getGameUpdates();
        for (Request request : requestList) {
            request.setGroupId(groupId);
        }
        rxFetch.enqueue(requestList)
                .asFlowable()
                .subscribe(new Consumer<List<Download>>() {
                    @Override
                    public void accept(List<Download> downloads) throws Exception {
                        for (Download download : downloads) {
                            fileProgressMap.put(download.getId(), 0);
                            updateUIWithProgress();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        final Error error = FetchErrorUtils.getErrorFromThrowable(throwable);
                        Log.d("GamesFilesActivity", "Error:" + error.toString());
                    }
                });
    }

    private final FetchListener fetchListener = new AbstractFetchListener() {

        @Override
        public void onCompleted(@NotNull Download download) {
            fileProgressMap.put(download.getId(), download.getProgress());
            updateUIWithProgress();
        }

        @Override
        public void onError(@NotNull Download download) {
            reset();
            Snackbar.make(mainView, R.string.game_download_error, Snackbar.LENGTH_INDEFINITE)
                    .show();
        }

        @Override
        public void onProgress(@NotNull Download download, long etaInMilliseconds, long downloadedBytesPerSecond) {
            super.onProgress(download, etaInMilliseconds, downloadedBytesPerSecond);
            fileProgressMap.put(download.getId(), download.getProgress());
            updateUIWithProgress();
        }

    };

}
