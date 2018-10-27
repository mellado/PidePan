package es.pablomellado.pandealfacar.orders_list;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import es.pablomellado.pandealfacar.ConnectedFragment;
import es.pablomellado.pandealfacar.FirebaseInterface;
import es.pablomellado.pandealfacar.R;
import es.pablomellado.pandealfacar.model.Order;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListOrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListOrderFragment extends ConnectedFragment
    implements FirebaseInterface.OrderListCallBackListener{

    private Order[] mOrders = null;
    ListOrderRecyclerViewAdapter mListOrderRecycleViewAdapter;
    private MaterialDialog mProgressDialog = null;


    public ListOrderFragment() {
        // Required empty public constructor
    }

    public static ListOrderFragment newInstance() {
        ListOrderFragment fragment = new ListOrderFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_order, container, false);
        RecyclerView rcList = (RecyclerView)view.findViewById(R.id.list);
        // Set the adapter
        if (rcList instanceof RecyclerView) {
            Context context = view.getContext();
            rcList.setLayoutManager(new LinearLayoutManager(context));

            mListOrderRecycleViewAdapter = new ListOrderRecyclerViewAdapter();
            rcList.setAdapter(mListOrderRecycleViewAdapter);
            getClientOrders();

        }
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_action_back);
        toolbar.setTitle(R.string.orders_title);

        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        getActivity().onBackPressed();

                    }
                }
        );

    }

    void getClientOrders() {
        if (isConnected()) {
            FirebaseInterface firebaseInterface = new FirebaseInterface(this.getActivity());
            SharedPreferences sharedPref = getActivity().getSharedPreferences(
                    getActivity().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String myPhoneNumber = sharedPref.getString(getString(R.string.my_phone_number), "");
            if (myPhoneNumber.length() > 0) {
                firebaseInterface.getOrderList(myPhoneNumber, this);
                mProgressDialog = new MaterialDialog.Builder(getContext())
                        .title(R.string.getting_pending_orders)
                        .content(R.string.please_wait)
                        .progress(true, 0)
                        .show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), R.string.couldnt_connect,
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, 35000);
            }
        }else {
            mNoConnectionDialogBuilder
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            getClientOrders();
                        }
                    })
                    .show();
        }
    }

    @Override
    public void gotPendingOrderList(Order[] orders) {
        if(mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mOrders = orders;
        RecyclerView rcList =(RecyclerView) getActivity().findViewById(R.id.list);
        TextView tvEmptyPendingOrders = (TextView) getActivity().findViewById(R.id.empty_pending_orders);
        // Open new address form if there isnt any
        if (orders == null || (orders != null && orders.length == 0)){
            mOrders = null;
            rcList.setVisibility(View.GONE);
            tvEmptyPendingOrders.setVisibility(View.VISIBLE);


        }
        // Else open the place order ending form
        else{
            if (mListOrderRecycleViewAdapter != null){
                rcList.setVisibility(View.VISIBLE);
                tvEmptyPendingOrders.setVisibility(View.GONE);
                mListOrderRecycleViewAdapter.updateOrders(orders);
                mListOrderRecycleViewAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void errorGettingPendingOrderList() {
        if(mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        // TODO: Show snackbar with error
    }
}
