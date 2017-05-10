package org.bostwickenator.googlephotos;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.ma1co.pmcademo.app.Logger;

public class WifiSetupActivity extends Activity {

    private ConnectivityManager connectivityManager;
    private WifiManager wifiManager;
    private TextView textViewWifiState;
    private BroadcastReceiver receiver;
    private Resources res;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setup);
        res = getResources();

        AppInit.initApp();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if(hasConnection()){ //Short circuit everything if we have some kind of upstream connection. Useful in emulators
            startLoginActivity();
        } else {

            textViewWifiState = (TextView) findViewById(R.id.textViewWifiState);

            setWifiEnabled(true);

            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateWifiSwitch();
                }
            };

            findViewById(R.id.buttonWifiSettings).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.info("starting wifi settings activity");
                    boolean wifiEnabled = isWifiEnabled();
                    setWifiEnabled(true);
                    startActivityForResult(new Intent("com.sony.scalar.app.wifisettings.WifiSettings"), wifiEnabled ? 1 : 0);
                }
            });
        }
    }

    private boolean hasConnection() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(hasConnection()){ //Short circuit everything if we have some kind of upstream connection.
            startLoginActivity();
        } else {
            IntentFilter f = new IntentFilter();
            f.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            f.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            f.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(receiver, f);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e){
            // noop
        }
    }

    private void setWifiEnabled(boolean enabled) {
        Logger.info("setting wifi enabled state to " + enabled);
        wifiManager.setWifiEnabled(enabled);
    }

    private boolean isWifiEnabled() {
        int state = wifiManager.getWifiState();
        return state == WifiManager.WIFI_STATE_ENABLING || state == WifiManager.WIFI_STATE_ENABLED;
    }

    private void updateWifiSwitch() {
        boolean wifiEnabled = isWifiEnabled();
        String summary;
        if (wifiEnabled) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (networkInfo.isConnected()) {
                summary = res.getString(R.string.connectionStateConnectedTo) + " " + wifiInfo.getSSID();
                startLoginActivity();
            } else {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                switch (state) {
                    case SCANNING:
                        summary = res.getString(R.string.connectionStateWifiScanning);
                        break;
                    case AUTHENTICATING:
                    case CONNECTING:
                    case OBTAINING_IPADDR:
                        summary = res.getString(R.string.connectionStateConnecting);
                        break;
                    default:
                        res.getString(0, "test");
                        summary = res.getString(R.string.connectionStateWifiEnabled);
                }
            }
        } else {
            summary = res.getString(R.string.connectionStateWifiDisabled);
        }
        textViewWifiState.setText(summary);
    }

    private void startLoginActivity(){
        try {
            unregisterReceiver(receiver);
        } catch (Exception e){
            // noop
        }
        this.startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int wifiEnabled, int result, Intent intent) {
        Logger.info("returned from wifi settings");
        super.onActivityResult(wifiEnabled, result, intent);
        setWifiEnabled(wifiEnabled == 1);
    }
}
