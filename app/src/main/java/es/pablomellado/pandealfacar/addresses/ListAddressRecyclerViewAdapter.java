package es.pablomellado.pandealfacar.addresses;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.pablomellado.pandealfacar.R;
import es.pablomellado.pandealfacar.model.Address;


/**
 * Created by Pablo Mellado on 5/7/17.
 */

public class ListAddressRecyclerViewAdapter
        extends RecyclerView.Adapter<ListAddressRecyclerViewAdapter.ViewHolder> {

    private Context mContext;

    private List<Address> mValues;
    private AdapterInterface mListener;

    public interface AdapterInterface{
        public void removeAddressAt(int position);
        public void editAddressAt(int position);

    }

    public ListAddressRecyclerViewAdapter(AdapterInterface listener){
        mListener = listener;

    }

    public void updateAddresses(Address[] addresses){
        mValues = new ArrayList<Address>(Arrays.asList(addresses));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        mContext = parent.getContext();
        view = LayoutInflater.from(mContext)
                    .inflate(R.layout.fragment_address_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Address currentAddress = mValues.get(position);
        holder.mContact.setText(currentAddress.getContact());
        holder.mLine1.setText(currentAddress.getLine1());
        holder.mLine2.setText(currentAddress.getLine2());
        holder.mPostalCode.setText(currentAddress.getPostalcode());
        holder.mCity.setText(currentAddress.getCity());

        holder.mButtonDeleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.removeAddressAt(position);
            }
        });

        holder.mButtonEditView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.editAddressAt(position);
            }
        });


    }

    @Override
    public int getItemCount() {
        if (mValues!= null) {
            return mValues.size();
        }
        else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContact;
        public final TextView mLine1;
        public final TextView mLine2;
        public final TextView mPostalCode;
        public final TextView mCity;
        public final Button mButtonEditView;
        public final Button mButtonDeleteView;

        public ViewHolder(View view) {
            super(view);
            mView = view;

            mContact = (TextView) view.findViewById(R.id.tvContact);
            mLine1 = (TextView) view.findViewById(R.id.tvLine1);
            mLine2 = (TextView) view.findViewById(R.id.tvLine2);
            mPostalCode = (TextView) view.findViewById(R.id.tvPostalCode);
            mCity = (TextView) view.findViewById(R.id.tvCity);
            mButtonDeleteView = (Button) view.findViewById(R.id.button_delete);
            mButtonEditView = (Button) view.findViewById(R.id.button_edit);


        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContact.getText() + "'";
        }
    }
}
