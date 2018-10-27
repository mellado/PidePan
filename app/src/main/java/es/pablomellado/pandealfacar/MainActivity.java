package es.pablomellado.pandealfacar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiActivity;


import java.util.List;

import es.pablomellado.pandealfacar.addresses.AddressActivity;
import es.pablomellado.pandealfacar.model.Address;
import es.pablomellado.pandealfacar.model.Client;
import es.pablomellado.pandealfacar.model.Order;
import es.pablomellado.pandealfacar.model.OrderedProduct;
import es.pablomellado.pandealfacar.model.Product;
import es.pablomellado.pandealfacar.order.OrderActivity;
import es.pablomellado.pandealfacar.order.SessionHelper;
import es.pablomellado.pandealfacar.orders_list.OrdersListActivity;
import es.pablomellado.pandealfacar.register.RegisterActivity;

public class MainActivity extends ConnectedActivity
    implements ProductListFragment.OnProductSelectedListener,

        // TODO: Remove before release
        FirebaseInterface.LastOrderCallBackListener,

        FirebaseInterface.ClientAddressesCallBackListener{

    public static final String LISTFRAGMENT_TAG = "productlist";
    private Toolbar toolbar;
    static final int REGISTER_ACTIVITY_REQUEST = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Manually confirm the phone number
        SharedPreferences sharedPref = this.getSharedPreferences(
                this.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        //editor.putBoolean(this.getString(R.string.phone_is_confirmed), true);
        //editor.putString(getString(R.string.my_phone_number),"666555222");

        //editor.remove(this.getString(R.string.phone_is_confirmed));
        //editor.remove(getString(R.string.my_phone_number));

        //editor.commit();

        //FirebaseInterface firebaseInterface = new FirebaseInterface(this);
        //firebaseInterface.getOrderList("666555222");

        //Client me = new Client("678872646","Cristina Luzón");
        //firebaseInterface.addClient(me);
        /*firebaseInterface.addClientAddress(me,
                new Address("Camino de Ronda 3, 6ºA","","Granada","18004"));
        firebaseInterface.addClientAddress(me,
                new Address("Camino de Ronda 4, 6ºA","","Granada","18004"));
*/
        /*OrderedProduct [] ops = new OrderedProduct[]{
                new OrderedProduct("barra", "Barra de Pan", 0.7, 5),
                new OrderedProduct("hogaza", "Hogaza Grande", 1.2, 5),

        };
        firebaseInterface.addOrder(new Order(me.getPhone(), new Address("Camino de Ronda 111, 11ºA","","Granada","18111")
            ,ops, "2017-04-25", "9:00-10:30"));*/



        //firebaseInterface.getLastOrder(me, this);

        //firebaseInterface.getProductList(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            setSupportActionBar(toolbar);
            setupUI(toolbar);
        }


        Fragment fragment;
        if (savedInstanceState == null) {
            fragment = new ProductListFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment, LISTFRAGMENT_TAG)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_place_order:
                // Empty cart
                if (SessionHelper.getCart().getTotalQuantity()==0) {
                    openOrderActivity();
                }
                else {
                    SharedPreferences sharedPref = this.getSharedPreferences(
                            this.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    if (!sharedPref.getBoolean(this.getString(R.string.phone_is_confirmed), false)) {
                        if (isConnected()) {
                            openRegisterActivity();
                        }else {
                             showNoConnectionDialog();
                        }
                    } else {
                        String phoneNo = sharedPref.getString(this.getString(R.string.my_phone_number), "");
                        if (phoneNo.length() > 0) {
                            if (isConnected()) {
                                FirebaseInterface firebaseInterface = new FirebaseInterface(this);
                                firebaseInterface.confirmClient(phoneNo);
                                if (SessionHelper.getCurrentAddress() == null) {
                                    // Get client addresses to assign the default one
                                    firebaseInterface.getClientAddresses(phoneNo, this);
                                } else {
                                    openOrderActivity();
                                }
                            }
                            else {
                                showNoConnectionDialog();
                            }
                        }
                    }
                }
                return true;

            case R.id.action_my_addresses:
                if (isConnected()) {
                    openAddressActivity();
                }else{
                    showNoConnectionDialog();
                }
                return true;

            case R.id.action_my_orders:
                if (isConnected()) {
                    openOrderListActivity();
                }else{
                    showNoConnectionDialog();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivityForResult(intent, REGISTER_ACTIVITY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REGISTER_ACTIVITY_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                openOrderActivity();
            }
        }
    }

    private void openOrderActivity() {
        Intent intent = new Intent(this, OrderActivity.class);
        startActivity(intent);
    }

    private void openAddressActivity(){
        Intent intent = new Intent(this, AddressActivity.class);
        startActivity(intent);
    }

    private void openOrderListActivity(){
        Intent intent = new Intent(this, OrdersListActivity.class);
        startActivity(intent);
    }



    // TODO: Remove before release
    public void gotLastOrder(Order lastOrder){
        // Update form with the received order
        Log.d("FRB","Got last order in main activity");
    }

    @Override
    public void errorGettingLastOrder() {

    }

    /*public void gotProductList(Product[] products){
        Log.d("","Got product list");
    }

    @Override
    public void errorGettingProductList() {

    }*/

    public void onProductSelected(ProductInfoRow productInfoRow){
        final ProductInfoFragment productInfoFrag = ProductInfoFragment.newInstance(productInfoRow);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, productInfoFrag);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();

    }

    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    hideSoftKeyboard();
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                findViewById(R.id.main_activity).getWindowToken(), 0);
    }

    @Override
    public void gotClientAddresses(Address[] addresses) {
        if (SessionHelper.getCurrentAddress() == null && addresses.length > 0){
            SessionHelper.setCurrentAddress(addresses[0]);
        }
        openOrderActivity();
    }

    @Override
    public void errorGettingAddresses() {
        // TODO: Show snackbar with error trying to open Order Activity

    }
}
