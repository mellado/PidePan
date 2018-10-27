package es.pablomellado.pandealfacar.order;

import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;

import es.pablomellado.pandealfacar.ConnectedActivity;
import es.pablomellado.pandealfacar.R;
import es.pablomellado.pandealfacar.addresses.NewAddressFragment;
import es.pablomellado.pandealfacar.order.Cart.CartItem;

public class OrderActivity extends ConnectedActivity
    implements ShoppingCartFragment.OnFragmentInteractionListener,
                ConfirmOrderFragment.ConfirmOrderFragmentListener{

    public static final String CONFIRM_ORDER = "confirm_order_stage";
    public static final String NEW_ADDRESS = "new_address_fragment_tag";
    public static final String CARTFRAGMENT_TAG = "cartlist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.shopping_cart_title);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, ShoppingCartFragment.newInstance(1), CARTFRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return false;
    }

    public void onListFragmentInteraction(CartItem item){


    }

    public void openConfirmOrderFragment(){
        Fragment fragment = ConfirmOrderFragment.newInstance(SessionHelper.getCart());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, CONFIRM_ORDER)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void openNewAddressNeededFragment() {
        final NewAddressFragment fragment =  NewAddressFragment.newInstance(false, null, true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container,fragment,NEW_ADDRESS)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void loadNewAddressFragment() {
        final NewAddressFragment fragment =  NewAddressFragment.newInstance(false, null, false);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container,fragment,NEW_ADDRESS)
                .addToBackStack(null)
                .commit();
    }
}
