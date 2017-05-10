package org.bostwickenator.googlephotos;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import com.github.ma1co.pmcademo.app.BaseActivity;
import com.github.ma1co.pmcademo.app.Logger;

public class MainActivity extends BaseActivity {
    private ProgressBar progressBarUploadProgress;

    private TextView textViewPhotosToUploadCount;
    private TextView textViewUploadedCount;
    private TextView textViewUploadStatus;

    private View buttonUploadPhotos;
    private View buttonSettings;

    private View progressBarSpin;

    private final UploadRecordDatabase uploadRecordDatabase = UploadRecordDatabase.getInstance();

    private UploadTask mUploadTask;

    boolean goingToSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        progressBarUploadProgress = (ProgressBar) findViewById(R.id.progressBarUploadProgress);

        buttonUploadPhotos = findViewById(R.id.buttonUploadPhotos);
        buttonSettings = findViewById(R.id.buttonSettings);

        progressBarSpin = findViewById(R.id.progressBarSpin);


        buttonUploadPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUploadTask = new UploadTask();
                mUploadTask.execute();
            }
        });

        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goingToSettings = true;
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        textViewPhotosToUploadCount = (TextView) findViewById(R.id.textViewPhotosToUploadCount);
        textViewUploadedCount = (TextView) findViewById(R.id.textViewUploadedCount);
        textViewUploadStatus = (TextView) findViewById(R.id.textViewUploadStatus);
        updateNumberOfPhotos();

        buttonUploadPhotos.setEnabled(toUploadCount != 0);
    }

    private static final int NOT_INITIALIZED = -1;

    private int toUploadCount = NOT_INITIALIZED;

    private void updateNumberOfPhotos() {

        if(toUploadCount == NOT_INITIALIZED) {
            List<File> files = getFilesToUpload();
            toUploadCount = files.size();
        }
        textViewPhotosToUploadCount.setText("" + toUploadCount);
        textViewUploadedCount.setText("" + uploadRecordDatabase.getUploadedCount());

        buttonUploadPhotos.setEnabled(toUploadCount != 0 && mUploadTask == null );
    }

    class UploadTask extends AsyncTask<Void, Integer, Void> {

        int totalFiles;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            buttonUploadPhotos.setEnabled(false);
            buttonSettings.setEnabled(false);
            textViewUploadStatus.setText(R.string.statusUploading);
            progressBarSpin.setVisibility(View.VISIBLE);
            setAutoPowerOffMode(false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            buttonUploadPhotos.setEnabled(true);
            buttonSettings.setEnabled(true);
            textViewUploadStatus.setText(R.string.statusComplete);
            progressBarSpin.setVisibility(View.GONE);
            setAutoPowerOffMode(true);
            mUploadTask = null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(progressBarUploadProgress.getMax() != totalFiles -1) {
                progressBarUploadProgress.setMax(totalFiles -1);
            }
            progressBarUploadProgress.setProgress(values[0]);
            toUploadCount--;
            updateNumberOfPhotos();
            Logger.info("Progress:" + values[0]);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                PicasawebClient picasawebClient = AuthenticationManager.authorize(MainActivity.this);

                List<File> files = getFilesToUpload();

                totalFiles = files.size();
                for(int i = 0; i < totalFiles; i++) {
                    File file = files.get(i);
                    picasawebClient.httpPhotoPost(file);
                    if (SettingsStore.getSettingsStore().getBoolean(SettingsActivity.SETTING_DELETE_AFTER_UPLOAD, false)) {
                       file.delete();
                    } else {
                        uploadRecordDatabase.addFile(file);
                    }

                    publishProgress(i);
                    if(this.isCancelled()) {
                        break;
                    }
                }

            } catch (Exception e) {
                Logger.error(e.toString());
            }
            return null;
        }
    }

    private List<File> getFilesToUpload(){
        List<File> files = FilesystemScanner.getImagesOnExternalStorage();

        if(SettingsStore.getSettingsStore().getBoolean(SettingsActivity.SETTING_UPLOAD_VIDEOS, false)) {
            List<File> videos = FilesystemScanner.getVideosOnExternalStorage();
            files.addAll(videos);
        }
        uploadRecordDatabase.filterFileList(files);
        return files;
    }

    @Override
    protected void onResume() {
        super.onResume();
        toUploadCount = NOT_INITIALIZED;
        updateNumberOfPhotos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mUploadTask != null) {
            mUploadTask.cancel(true);
        }
        if (!goingToSettings) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            Logger.info("setting wifi enabled state to " + false);
            wifiManager.setWifiEnabled(false);
        }
        goingToSettings = false;
    }
}