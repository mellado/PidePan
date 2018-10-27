package es.pablomellado.pandealfacar.addresses;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import es.pablomellado.pandealfacar.ConnectedFragment;
import es.pablomellado.pandealfacar.FirebaseInterface;
import es.pablomellado.pandealfacar.R;
import es.pablomellado.pandealfacar.model.Address;
import es.pablomellado.pandealfacar.order.MyShoppingCartRecyclerViewAdapter;
import es.pablomellado.pandealfacar.order.SessionHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListAddressFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListAddressFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListAddressFragment
        extends ConnectedFragment
        implements FirebaseInterface.ClientAddressesCallBackListener,
            ListAddressRecyclerViewAdapter.AdapterInterface{


    private Address[] mAddresses = null;

    ListAddressRecyclerViewAdapter mListAddressRecycleViewAdapter;
    private OnFragmentInteractionListener mListener;
    private MaterialDialog mProgressDialog = null;


    public ListAddressFragment() {
        // Required empty public constructor
    }



    public static ListAddressFragment newInstance() {
        ListAddressFragment fragment = new ListAddressFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    void getClientAddresses() {
        if (isConnected()) {
            FirebaseInterface firebaseInterface = new FirebaseInterface(this.getActivity());
            SharedPreferences sharedPref = getActivity().getSharedPreferences(
                    getActivity().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String myPhoneNumber = sharedPref.getString(getString(R.string.my_phone_number), "");
            if (myPhoneNumber.length() > 0) {
                firebaseInterface.getClientAddresses(myPhoneNumber, this);
                mProgressDialog = new MaterialDialog.Builder(getContext())
                        .title(R.string.getting_addresses)
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
                        getClientAddresses();
                    }
                })
                .show();
        }
    }

    public void gotClientAddresses(Address[] addresses){
        if(mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mAddresses = addresses;
        RecyclerView rcList =(RecyclerView) getActivity().findViewById(R.id.list);
        TextView tvEmptyAddresses = (TextView) getActivity().findViewById(R.id.empty_addresses);
        // Open new address form if there isnt any
        if (addresses.length==0){
            mAddresses = null;
            rcList.setVisibility(View.GONE);
            tvEmptyAddresses.setVisibility(View.VISIBLE);


        }
        else{
            if (mListAddressRecycleViewAdapter != null){
                rcList.setVisibility(View.VISIBLE);
                tvEmptyAddresses.setVisibility(View.GONE);
                mListAddressRecycleViewAdapter.updateAddresses(addresses);
                mListAddressRecycleViewAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void errorGettingAddresses() {
        if(mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_address, container, false);
        RecyclerView rcList = (RecyclerView)view.findViewById(R.id.list);
        // Set the adapter
        if (rcList instanceof RecyclerView) {
            Context context = view.getContext();
            rcList.setLayoutManager(new LinearLayoutManager(context));

            mListAddressRecycleViewAdapter = new ListAddressRecyclerViewAdapter((ListAddressRecyclerViewAdapter.AdapterInterface)this);
            rcList.setAdapter(mListAddressRecycleViewAdapter);
            getClientAddresses();

        }
        setHasOptionsMenu(true);

        FloatingActionButton fabNewAddress = (FloatingActionButton) view.findViewById(R.id.fab_new_address);
        fabNewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.loadNewAddressFragment();
            }
        });


        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_action_back);
        toolbar.setTitle(R.string.addresses_title);

        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        getActivity().onBackPressed();

                    }
                }
        );

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void showToast(String message) {

        Toast toast = Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void removeAddressAt(final int position) {
        if (isConnected()){
            final FirebaseInterface firebaseInterface = new FirebaseInterface(this.getActivity());

            if (mAddresses!=null) {
                new MaterialDialog.Builder(this.getContext())
                        .content(R.string.confirm_delete_address)
                        .positiveText(android.R.string.yes)
                        .negativeText(android.R.string.no)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                SharedPreferences sharedPref = getActivity().getSharedPreferences(
                                        getActivity().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                                String myPhoneNumber = sharedPref.getString(getString(R.string.my_phone_number), "");
                                if (myPhoneNumber.length() > 0 && mAddresses.length>position) {

                                    //Update the current address if the actual was deleted
                                    if (mAddresses.length==1){
                                        SessionHelper.setCurrentAddress(null);
                                    }
                                    else {
                                        if (SessionHelper.getCurrentAddress()==null ||
                                                mAddresses[position].getKey().compareTo(
                                                SessionHelper.getCurrentAddress().getKey()) == 0) {
                                            for (int i=0;i<mAddresses.length;i++){
                                                if (i!=position){
                                                    SessionHelper.setCurrentAddress(mAddresses[i]);
                                                    break;
                                                }
                                            }

                                        }
                                    }

                                    firebaseInterface.deleteClientAddress(myPhoneNumber, mAddresses[position]);
                                    getClientAddresses();

                                }
                            }
                        })
                        .show();
            }
        }else {
            mNoConnectionDialogBuilder
                    .show();
        }

    }

    @Override
    public void editAddressAt(int position) {
        // Tell parent activity to change to edit address
        mListener.loadEditAddressFragment(mAddresses[position]);

    }

    public void notifyDataSetChanged() {
        mListAddressRecycleViewAdapter.notifyDataSetChanged();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void loadNewAddressFragment();
        void loadListAddressFragment();
        void loadEditAddressFragment(Address address);
    }
}
