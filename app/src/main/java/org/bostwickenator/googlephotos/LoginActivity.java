package org.bostwickenator.googlephotos;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.ma1co.pmcademo.app.BaseActivity;
import com.github.ma1co.pmcademo.app.Logger;

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
        new LoginTask().execute();
    }

    class LoginTask extends AsyncTask<Void, Void, Void> {

        PicasawebClient picasawebClient;
        Exception exception;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                picasawebClient = AuthenticationManager.authorize(LoginActivity.this);
            } catch (Exception e) {
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(picasawebClient != null) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
            if(exception != null) {
                Logger.error(exception.toString());
                labelLoginState.setText(R.string.loginFailed);
                buttonLogin.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            }
        }
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
