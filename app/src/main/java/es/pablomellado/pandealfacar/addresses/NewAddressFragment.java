package es.pablomellado.pandealfacar.addresses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import es.pablomellado.pandealfacar.ConnectedFragment;
import es.pablomellado.pandealfacar.FirebaseInterface;
import es.pablomellado.pandealfacar.R;
import es.pablomellado.pandealfacar.model.Address;
import es.pablomellado.pandealfacar.order.OrderActivity;
import es.pablomellado.pandealfacar.order.SessionHelper;


public class NewAddressFragment extends ConnectedFragment {

    //private ListAddressFragment.OnFragmentInteractionListener mListener;
    private static final String ARGUMENT_IS_EDITING = "isEditing";
    private static final String ARGUMENT_ADDRESS = "address";
    private static final String ARGUMENT_SHOW_ADDRESS_NEEDED = "showAddressNeeded";
    private boolean isEditing = false;
    private Address mAddress = null;

    public NewAddressFragment() {
    }

    public static NewAddressFragment newInstance(boolean isEditing, Address address,
                                                 boolean showAddressNeeded) {

        final Bundle args = new Bundle();
        args.putBoolean(ARGUMENT_IS_EDITING, isEditing);
        args.putSerializable(ARGUMENT_ADDRESS, address);
        args.putBoolean(ARGUMENT_SHOW_ADDRESS_NEEDED, showAddressNeeded);

        final NewAddressFragment fragment = new NewAddressFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);
        CardView cvAddressNeeded = (CardView)view.findViewById(
                R.id.cardView_address_needed);



        Bundle args = getArguments();
        if (args!=null) {
            if(args.getBoolean(ARGUMENT_IS_EDITING)) {
                mAddress = (Address) args
                        .getSerializable(ARGUMENT_ADDRESS);
                loadEditingAddress(view);
                isEditing = true;
            }
            if (args.getBoolean(ARGUMENT_SHOW_ADDRESS_NEEDED)){
                cvAddressNeeded.setVisibility(View.VISIBLE);
            }
            else {
                cvAddressNeeded.setVisibility(View.GONE);
            }
        }
        else{
            throw new RuntimeException("NewAddressFragment must be created with the newInstance method to get the arguments.");
        }
        setHasOptionsMenu(true);
        return view;
    }

    private boolean isEditingAddressChanged(View view){
        EditText contactEditText = (EditText) view.findViewById(R.id.address_name);
        EditText addressLine1EditText = (EditText) view.findViewById(R.id.address_line1);
        EditText addressLine2EditText = (EditText) view.findViewById(R.id.address_line2);
        EditText cityEditText = (EditText) view.findViewById(R.id.address_city);
        EditText postalCodeEditText = (EditText) view.findViewById(R.id.address_postal_code);

        EditText[] arrEditTexts = new EditText[]{contactEditText, addressLine1EditText,
            addressLine2EditText, cityEditText, postalCodeEditText};
        String[] arrOriginalAddress = new String[]{mAddress.getContact(), mAddress.getLine1(),
            mAddress.getLine2(), mAddress.getCity(), mAddress.getPostalcode()};
        for (int i=0; i<arrEditTexts.length; i++){
            if (!arrEditTexts[i].getText().toString().trim().equals(arrOriginalAddress[i].trim())){
                return true;
            }
        }
        return false;
    }

    private void loadEditingAddress(View view){
        EditText contactEditText = (EditText) view.findViewById(R.id.address_name);
        EditText addressLine1EditText = (EditText) view.findViewById(R.id.address_line1);
        EditText addressLine2EditText = (EditText) view.findViewById(R.id.address_line2);
        EditText cityEditText = (EditText) view.findViewById(R.id.address_city);
        EditText postalCodeEditText = (EditText) view.findViewById(R.id.address_postal_code);

        contactEditText.setText(mAddress.getContact());
        addressLine1EditText.setText(mAddress.getLine1());
        addressLine2EditText.setText(mAddress.getLine2());
        cityEditText.setText(mAddress.getCity());
        postalCodeEditText.setText(mAddress.getPostalcode());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if(isEditing){
            toolbar.setTitle(R.string.edit_address_title);
        }
        else {
            toolbar.setTitle(R.string.new_address_title);
        }
        toolbar.setNavigationIcon(R.mipmap.ic_action_back);
        inflater.inflate(R.menu.menu_new_address, menu);

        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (isEditing){
                            if (isEditingAddressChanged(getView())){
                                new MaterialDialog.Builder(getContext())
                                        .content(R.string.confirm_discard_changes)
                                        .positiveText(android.R.string.yes)
                                        .negativeText(android.R.string.no)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                hideKeyboard(getContext());
                                                getActivity().onBackPressed();
                                            }
                                        })
                                        .show();
                            }else {
                                hideKeyboard(getContext());
                                getActivity().onBackPressed();
                            }

                        }
                        else{
                            if (isEmptyAddress()) {
                                hideKeyboard(getContext());
                                getActivity().onBackPressed();
                            }else {
                                new MaterialDialog.Builder(getContext())
                                        .content(R.string.confirm_discard_new_address)
                                        .positiveText(android.R.string.yes)
                                        .negativeText(android.R.string.no)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                hideKeyboard(getContext());
                                                getActivity().onBackPressed();
                                            }
                                        })
                                        .show();
                            }
                        }

                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_save:
                if (isConnected()){
                    // Check if the address is fine
                    if(checkValidAddress()){
                        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                                getActivity().getString(R.string.preference_file_key),
                                Context.MODE_PRIVATE);
                        String myPhoneNumber= sharedPref.getString(getString(R.string.my_phone_number),
                                "");
                        if (myPhoneNumber.length()>0) {
                            hideKeyboard(getContext());
                            FirebaseInterface firebaseInterface = new FirebaseInterface(this.getActivity());
                            Address formAddress = getAddress();
                            if(isEditing){
                                formAddress.setKey(mAddress.getKey());
                                firebaseInterface.editClientAddress(myPhoneNumber, formAddress);
                            }
                            else {
                                String key = firebaseInterface.addClientAddress(myPhoneNumber,
                                        formAddress);
                                formAddress.setKey(key);
                            }
                            SessionHelper.setCurrentAddress(formAddress);
                        }
                        getFragmentManager().popBackStack();
                    }
                }else {
                    mNoConnectionDialogBuilder
                            .show();
                }
                return true;

            default:
                break;
        }

        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof ListAddressFragment.OnFragmentInteractionListener) {
            mListener = (ListAddressFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    private boolean isEmptyAddress(){
        EditText contactEditText = (EditText) getView().findViewById(R.id.address_name);
        EditText addressLine1EditText = (EditText) getView().findViewById(R.id.address_line1);
        EditText addressLine2EditText = (EditText) getView().findViewById(R.id.address_line2);
        EditText cityEditText = (EditText) getView().findViewById(R.id.address_city);
        EditText postalCodeEditText = (EditText) getView().findViewById(R.id.address_postal_code);

        for (EditText editText: new EditText[]{postalCodeEditText, cityEditText,
                addressLine2EditText, addressLine1EditText, contactEditText, }){
            if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
                return false;
            }
        }
        return true;
    }

    private boolean checkValidAddress(){
        boolean cancel = false;
        View focusView = null;
        EditText contactEditText = (EditText) getView().findViewById(R.id.address_name);
        EditText addressLine1EditText = (EditText) getView().findViewById(R.id.address_line1);
        EditText addressLine2EditText = (EditText) getView().findViewById(R.id.address_line2);
        EditText cityEditText = (EditText) getView().findViewById(R.id.address_city);
        EditText postalCodeEditText = (EditText) getView().findViewById(R.id.address_postal_code);

        for (EditText editText: new EditText[]{postalCodeEditText, cityEditText,
                addressLine1EditText, contactEditText, }){
            if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                editText.setError(getString(R.string.error_field_required));
                focusView = editText;
                cancel = true;
            }
        }
        if (postalCodeEditText.getError()==null) {
            try {
                int postalCode = Integer.parseInt(postalCodeEditText.getText().toString());
                if (postalCode < 18001 || postalCode > 18015) {
                    postalCodeEditText.setError(getString(R.string.error_incorrect_postalcode));
                    focusView = postalCodeEditText;
                    cancel = true;
                }
            } catch (NumberFormatException e) {
                postalCodeEditText.setError(getString(R.string.error_incorrect_postalcode_no_number));
                focusView = postalCodeEditText;
                cancel = true;
            }


        }

        if (!cityEditText.getText().toString().trim().toLowerCase().equals("granada") &&
                !cityEditText.getText().toString().trim().equals("")   ) {
            new MaterialDialog.Builder(this.getContext())
                    .content(R.string.text_only_granada)
                    .positiveText(R.string.OK)
                    .show();
            cityEditText.setText("Granada");
            addressLine1EditText.setText("");
            addressLine2EditText.setText("");
            postalCodeEditText.setText("");
            if (contactEditText.getError()==null) {
                focusView = addressLine1EditText;
            }else {
                focusView = contactEditText;
            }
            cancel = true;
        }

        if (cancel){
            focusView.requestFocus();
            return false;
        }
        else{
            return true;
        }
    }

    private Address getAddress(){
        EditText contactEditText = (EditText) getView().findViewById(R.id.address_name);
        EditText addressLine1EditText = (EditText) getView().findViewById(R.id.address_line1);
        EditText addressLine2EditText = (EditText) getView().findViewById(R.id.address_line2);
        EditText cityEditText = (EditText) getView().findViewById(R.id.address_city);
        EditText postalCodeEditText = (EditText) getView().findViewById(R.id.address_postal_code);

        String contact = contactEditText.getText().toString();
        String line1 = addressLine1EditText.getText().toString();
        String line2 = addressLine2EditText.getText().toString();
        String city = cityEditText.getText().toString();
        String postalCode = postalCodeEditText.getText().toString();
        Address result = new Address(contact, line1, line2, city, postalCode);
        return result;
    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

}
