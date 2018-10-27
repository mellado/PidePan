package es.pablomellado.pandealfacar;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by Pablo Mellado on 17/8/17.
 */

public abstract class ConnectedFragment extends Fragment {
    protected ConnectivityStatus mConnectivyStatusCallback;
    protected MaterialDialog.Builder mNoConnectionDialogBuilder;

    public ConnectedFragment(){
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ConnectivityStatus) {
            mConnectivyStatusCallback = (ConnectivityStatus) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ConnectivityStatus");
        }

        mNoConnectionDialogBuilder = new MaterialDialog.Builder(this.getContext())
                .content(R.string.no_internet_connection)
                .positiveText(R.string.OK);

    }

    protected boolean isConnected(){
        if(mConnectivyStatusCallback!=null) {
            return mConnectivyStatusCallback.isConnected();
        }else{
            throw new RuntimeException("mConnectivityStatusCallback was not initialiazed");
        }
    }
}
