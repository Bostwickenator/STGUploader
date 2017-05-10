package org.bostwickenator.googlephotos;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;

import com.github.ma1co.pmcademo.app.Logger;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.sony.scalar.sysutil.ScalarInput;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.AuthorizationUIController;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.wuman.android.auth.oauth2.store.FileCredentialStore;

import java.io.IOException;
import java.util.Collections;

class AuthenticationManager {

    private static final String CLIENT_ID = "[id_here]";
    private static final String CLIENT_SECRET = "[secret_here]";

    private static PicasawebClient authenticatedClient;

    private static AuthorizationUIController controller;

    private static OAuthManager.OAuthFuture<Credential> future;

    /** Authorizes the installed application to access user's protected data. */
    public static PicasawebClient authorize(FragmentActivity context) throws Exception {

        if(authenticatedClient != null){
            return authenticatedClient;
        }

        FileCredentialStore credentialStore = new FileCredentialStore(FileGetter.getFile("C.DAT"), new JacksonFactory());

        AuthorizationFlow.Builder builder = new AuthorizationFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                AndroidHttp.newCompatibleTransport(),
                new JacksonFactory(),
                new GenericUrl("https://www.googleapis.com/oauth2/v4/token"),
                new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
                CLIENT_ID,
                "https://accounts.google.com/o/oauth2/v2/auth");
        builder.setCredentialStore(credentialStore);
        builder.setScopes(Collections.singletonList("https://picasaweb.google.com/data/"));
        AuthorizationFlow flow = builder.build();

        controller =
                new DialogFragmentController(context.getSupportFragmentManager(), true, true, true) {

                    @Override
                    public String getRedirectUri() throws IOException {
                        return "http://localhost/Callback";
                    }

                    @Override
                    public boolean isJavascriptEnabledForWebView() {
                        return true;
                    }

                    @Override
                    public boolean disableWebViewCache() {
                        return false;
                    }

                    @Override
                    public boolean removePreviousCookie() {
                        return false;
                    }


                    WebView wv;
                    @Override
                    public void applyWebViewOverrides(WebView webView) {
                        webView.getSettings().setLoadWithOverviewMode(true);
                        webView.getSettings().setUseWideViewPort(true);
                        webView.setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                //Logger.info("handling in authentication manager"); // avoid sycnhronized disk io in ui
                                if (wv==null) {
                                    wv = (WebView) v;
                                }
                                int scanCode = event.getScanCode();
                                if ((keyCode == KeyEvent.KEYCODE_BACK || scanCode == ScalarInput.ISV_KEY_MENU) && wv.canGoBack()) {
                                    wv.goBack();
                                    return true;
                                }
                                if( scanCode == ScalarInput.ISV_KEY_MENU) {
                                    try{
                                        stop();
                                        future.cancel(true);
                                    } catch (Exception e){
                                        Logger.error("We cannot go back!");
                                    }
                                    return true;
                                }
                                return false;
                            }
                        });
                    }

                };

        OAuthManager authManager = new OAuthManager(flow, controller);
        if(Looper.myLooper() == null) {
            Looper.prepare();
        }
        future = authManager.authorizeExplicitly("", null, new Handler());
        Credential creds = future.getResult();
        creds.refreshToken();
        Logger.info("Credentials obtained");
        authenticatedClient = new PicasawebClient(creds);
        return authenticatedClient;
    }

}
