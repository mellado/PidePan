package es.pablomellado.pandealfacar.dialog;


import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import es.pablomellado.pandealfacar.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuantitySelect extends DialogFragment {

    private NumberPicker mNumberPicker;
    private int mLastPosition=-1;


    public interface QuantitySelectListener {
        void onFinishQuantitySelectDialog(int quantity);
    }


    public QuantitySelect() {
        // Required empty public constructor
    }


    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_quantity_select, container, false);
    }*/


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mLastPosition = bundle.getInt("quantity", mLastPosition);
        }

        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.select_quantity_dialog_title)
                .customView(R.layout.dialog_quantity_select, true)
                .autoDismiss(false)
                .positiveText(R.string.select_quantity_dialog_positive_text)
                .negativeText(android.R.string.cancel)
                .build();

        materialDialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();

                ((QuantitySelectListener)getTargetFragment()).onFinishQuantitySelectDialog(mNumberPicker.getValue());
            }
        });

        materialDialog.getBuilder().onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
           }
        });

        mNumberPicker = (NumberPicker) materialDialog.getCustomView().findViewById(R.id.numberPicker);
        mNumberPicker.setMaxValue(99);
        mNumberPicker.setMinValue(1);
        setLastPosition();

        return materialDialog;
    }


    @Override
    public int show(FragmentTransaction ft, String label) {
        return super.show(ft, label);
    }

    public void setLastPosition () {
        if (mLastPosition!=-1) {
            mNumberPicker.setValue(mLastPosition);
        }
        else{
            mNumberPicker.setValue(mNumberPicker.getMinValue());
        }

    }

}
