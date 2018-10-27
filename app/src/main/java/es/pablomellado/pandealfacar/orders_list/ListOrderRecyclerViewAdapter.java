package es.pablomellado.pandealfacar.orders_list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.pablomellado.pandealfacar.R;
import es.pablomellado.pandealfacar.model.Order;
import es.pablomellado.pandealfacar.order.Cart;

/**
 * Created by Pablo Mellado on 11/8/17.
 */

public class ListOrderRecyclerViewAdapter
        extends RecyclerView.Adapter<ListOrderRecyclerViewAdapter.ViewHolder> {

    private Context mContext;

    private List<Order> mValues;

    public ListOrderRecyclerViewAdapter(){

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        mContext = parent.getContext();
        view = LayoutInflater.from(mContext)
                .inflate(R.layout.fragment_order_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Order currentOrder = mValues.get(position);
        holder.mDeliveryDate.setText(currentOrder.getDeliverDate());
        holder.mContact.setText(currentOrder.getAddress().getContact());
        holder.mLine1.setText(currentOrder.getAddress().getLine1());
        holder.mLine2.setText(currentOrder.getAddress().getLine2());
        holder.mPostalCode.setText(currentOrder.getAddress().getPostalcode());
        holder.mCity.setText(currentOrder.getAddress().getCity());

        BigDecimal totalPrice = new BigDecimal(0.0);
        TableLayout table = holder.mProductsTable;
        List<Cart.CartItem> cartItems = currentOrder.getOrderedProducts();
        for (int i = 0; i < cartItems.size(); i++) {

            TableRow row = new TableRow(mContext);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
            row.setLayoutParams(lp);
            // create a new TextView for showing xml data
            Cart.CartItem item = cartItems.get(i);
            String cell1 = Integer.toString(item.quantity) + " x " + item.product.getName();
            TextView t = new TextView(mContext);
            // set the text to "text xx"
            t.setText(cell1);
            // add the TextView  to the new TableRow
            row.addView(t);
            BigDecimal partialTotal = item.product.getPrice()
                    .multiply(new BigDecimal(item.quantity));
            TextView u = new TextView(mContext);
            u.setText(NumberFormat.getCurrencyInstance().format(partialTotal));
            u.setGravity(Gravity.RIGHT);
            row.addView(u);

            // add the TableRow to the TableLayout
            table.addView(row, i);
            totalPrice = totalPrice.add(partialTotal);
        }


        String cell2 = mContext.getString(R.string.total_cost);
        cell2 += " " + NumberFormat.getCurrencyInstance().
                format(totalPrice);
        holder.mTotalOrder.setText(cell2);

    }

    public void updateOrders(Order[] orders){
        mValues = new ArrayList<Order>(Arrays.asList(orders));
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
        public final TextView mDeliveryDate;
        public final TextView mContact;
        public final TextView mLine1;
        public final TextView mLine2;
        public final TextView mPostalCode;
        public final TextView mCity;
        public final TableLayout mProductsTable;
        public final TextView mTotalOrder;

        public ViewHolder(View view) {
            super(view);
            mView = view;

            mDeliveryDate = (TextView) view.findViewById(R.id.tvDeliveryDate);
            mContact = (TextView) view.findViewById(R.id.tvContact);
            mLine1 = (TextView) view.findViewById(R.id.tvLine1);
            mLine2 = (TextView) view.findViewById(R.id.tvLine2);
            mPostalCode = (TextView) view.findViewById(R.id.tvPostalCode);
            mCity = (TextView) view.findViewById(R.id.tvCity);
            mProductsTable = (TableLayout) view.findViewById(R.id.tlProductsTable);
            mTotalOrder = (TextView) view.findViewById(R.id.tvTotalOrder);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContact.getText() + "'";
        }
    }
}
