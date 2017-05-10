package org.bostwickenator.googlephotos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.github.ma1co.pmcademo.app.BaseActivity;

public class SettingsActivity extends BaseActivity {

    public static final String SETTING_UPLOAD_VIDEOS = "upload_videos";
    public static final String SETTING_DELETE_AFTER_UPLOAD = "delete_after_upload";

    private SettingsStore mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = SettingsStore.getSettingsStore();
        setContentView(R.layout.activity_settings);


        View buttonClearDatabase = findViewById(R.id.buttonClearUploadDatabase);

        buttonClearDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadRecordDatabase.getInstance().clearDatabase();
            }
        });

        setupCheckbox(R.id.checkBoxDeleteAfterUpload, SETTING_DELETE_AFTER_UPLOAD);
        setupCheckbox(R.id.checkBoxUploadVideos, SETTING_UPLOAD_VIDEOS);
    }

    private void setupCheckbox(int id, final String setting) {
        CheckBox checkBox = (CheckBox) findViewById(id);
        checkBox.setChecked(mSharedPreferences.getBoolean(setting, false));

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSharedPreferences.putBoolean(setting, isChecked);
            }
        });
    }
}
