package es.pablomellado.pandealfacar.orders_list;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import es.pablomellado.pandealfacar.ConnectedActivity;
import es.pablomellado.pandealfacar.R;
import es.pablomellado.pandealfacar.addresses.ListAddressFragment;
import es.pablomellado.pandealfacar.model.Order;

public class OrdersListActivity extends ConnectedActivity {

    public static final String LISTORDERFRAGMENT_TAG = "list_orders_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.orders_title);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            loadListOrderFragment();
        }

    }
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

    }

    public void loadListOrderFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, ListOrderFragment.newInstance(),
                        LISTORDERFRAGMENT_TAG)
                .commit();
    }
}
