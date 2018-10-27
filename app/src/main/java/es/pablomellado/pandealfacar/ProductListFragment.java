package es.pablomellado.pandealfacar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import es.pablomellado.pandealfacar.model.Product;
import es.pablomellado.pandealfacar.order.SessionHelper;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProductListFragment.OnProductSelectedListener} interface
 * to handle interaction events.
 * Use the {@link ProductListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductListFragment extends ConnectedFragment
        implements FirebaseInterface.ProductListCallBackListener {

    OnProductSelectedListener mCallback;
    private ViewGroup mLayoutContext;
    private MaterialDialog mProgressDialog = null;
    private Menu mMenu;

    public interface OnProductSelectedListener {
        public void onProductSelected(ProductInfoRow productInfoRow);
    }

    public ProductListFragment() {
        // Required empty public constructor
    }

    public static ProductListFragment newInstance() {
        ProductListFragment fragment = new ProductListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mLayoutContext = (ViewGroup) inflater.inflate(R.layout.fragment_product_list, container,
                false);
        setHasOptionsMenu(true);

        final int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.getContext());
        if (result == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
            new MaterialDialog.Builder(this.getContext())
                    .content(R.string.update_needed_google_play_services)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW)
                                    .setData(Uri.parse("market://details?id=com.google.android.gms"));
                            startActivity(goToMarket);
                        }
                    })
                    .positiveText(R.string.OK)
                    .show();
        } else {
            tryToGetProducts();

        }

        return mLayoutContext;
    }

    private void showProgressDialog(){

        mProgressDialog = new MaterialDialog.Builder(getContext())
                .title(R.string.getting_products)
                .content(R.string.please_wait)
                .progress(true, 0)
                .show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(mProgressDialog!=null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.couldnt_connect,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        }, 35000);

    }

    private void tryToGetProducts(){
        if (isConnected()) {
            FirebaseInterface firebaseInterface = new FirebaseInterface(this.getActivity());

            firebaseInterface.getProductList(this);
            showProgressDialog();
        }
        else {
            mNoConnectionDialogBuilder
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            tryToGetProducts();
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationIcon(null);
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean confirmed = sharedPref.getBoolean(getString(R.string.phone_is_confirmed), false);

        if (!confirmed){
            menu.findItem(R.id.action_my_addresses).setVisible(false);
            menu.findItem(R.id.action_my_orders).setVisible(false);
        }
        else{
            menu.findItem(R.id.action_my_addresses).setVisible(true);
            menu.findItem(R.id.action_my_orders).setVisible(true);
        }
    }

    public void gotProductList(Product[] products){
        if(mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        GridView gridview = (GridView) mLayoutContext.findViewById(R.id.gridview);
        ArrayList<ProductInfoRow> productList = new ArrayList<ProductInfoRow>();

        for (Product p: products){
            productList.add(new ProductInfoRow(p,0));
        }

        gridview.setAdapter(new ProductListAdapter(getActivity(), R.layout.grid_item_product, productList));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                ProductInfoRow pif = (ProductInfoRow)parent.getAdapter().getItem(position);
                if (mCallback!=null) {
                    mCallback.onProductSelected(pif);
                }
            }
        });

    }

    @Override
    public void errorGettingProductList() {
        if(mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProductSelectedListener) {
            mCallback = (OnProductSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnProductSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMenu!=null) {
            mMenu.clear();
            getActivity().getMenuInflater().inflate(R.menu.menu_main, mMenu);
        }

    }
}
