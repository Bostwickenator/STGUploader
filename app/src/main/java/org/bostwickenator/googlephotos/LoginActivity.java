package org.bostwickenator.googlephotos;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ma1co.pmcademo.app.BaseActivity;
import com.github.ma1co.pmcademo.app.Logger;

import java.io.File;

public class LoginActivity extends BaseActivity {

    private View buttonLogin;
    private View progress;
    private TextView labelLoginState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonLogin = findViewById(R.id.buttonLogin);
        progress = findViewById(R.id.progressBarLogin);

        labelLoginState = (TextView) findViewById(R.id.textViewLoginStatus);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoginTask().execute();
            }
        });


        if(checkIfCredentialsUpToDate()) {
            new LoginTask().execute();
        }
    }

    class LoginTask extends AsyncTask<Void, Void, Void> {

        GooglePhotosClient googlePhotosClient;
        Exception exception;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                googlePhotosClient = AuthenticationManager.authorize(LoginActivity.this);
            } catch (Exception e) {
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(googlePhotosClient != null) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
            if(exception != null) {
                Logger.error(exception.toString());
                setErrorState(R.string.loginFailed);
                buttonLogin.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setErrorState(int id){
        labelLoginState.setText(id);
        progress.setVisibility(View.GONE);
    }

    private boolean checkIfCredentialsUpToDate(){
        File c = FileGetter.getFile("C.DAT");
        if (c != null && c.exists()) {
            if(c.lastModified() < 1552867200000L) {
                setErrorState(R.string.oldCredentials);
                return false;
            } else {
                return true;
            }
        } else {
            setErrorState(R.string.noCredentials);
        }
        return false;
    }

    /*@Override
    protected boolean onMenuKeyUp() {
        if(AuthenticationManager.controller != null){
            try {
                AuthenticationManager.controller.stop();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                onBackPressed();
                return true;
            }
        }
        return super.onMenuKeyUp();
    }*/
}
