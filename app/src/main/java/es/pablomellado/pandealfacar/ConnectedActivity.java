package es.pablomellado.pandealfacar;

import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by Pablo Mellado on 17/8/17.
 */

public abstract class ConnectedActivity extends AppCompatActivity
        implements ConnectivityReceiver.ConnectivityReceiverListener,
            ConnectivityStatus {
    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        Global.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }
    @Override
    public boolean isConnected() {
        return ConnectivityReceiver.isConnected();
    }

    protected void showNoConnectionDialog(){
        new MaterialDialog.Builder(this)
                .content(R.string.no_internet_connection)
                .positiveText(R.string.OK)
                .show();
    }
}
