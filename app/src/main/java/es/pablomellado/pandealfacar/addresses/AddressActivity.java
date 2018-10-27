package es.pablomellado.pandealfacar.addresses;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import es.pablomellado.pandealfacar.ConnectedActivity;
import es.pablomellado.pandealfacar.R;
import es.pablomellado.pandealfacar.model.Address;
import es.pablomellado.pandealfacar.order.ShoppingCartFragment;

public class AddressActivity extends ConnectedActivity
    implements  ListAddressFragment.OnFragmentInteractionListener{

    public static final String NEWADDRESSFRAGMENT_TAG = "new_address_fragment";
    private static final String EDITADDRESSFRAGMENT_TAG = "edit_address_fragment";
    public static final String LISTADDRESSFRAGMENT_TAG = "list_address_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.addresses_title);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            loadListAddressFragment();
        }

    }
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

    }

    @Override
    public void loadListAddressFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, ListAddressFragment.newInstance(),
                        LISTADDRESSFRAGMENT_TAG)
                .commit();
    }

    @Override
    public void loadNewAddressFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container,
                        NewAddressFragment.newInstance(false, null, false),
                        NEWADDRESSFRAGMENT_TAG)
                .addToBackStack(NEWADDRESSFRAGMENT_TAG)
                .commit();
    }


    @Override
    public void loadEditAddressFragment(Address address) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container,
                        NewAddressFragment.newInstance(true, address, false),
                        EDITADDRESSFRAGMENT_TAG)
                .addToBackStack(EDITADDRESSFRAGMENT_TAG)
                .commit();
    }
}
