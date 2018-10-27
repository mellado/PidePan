package es.pablomellado.pandealfacar.order;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import es.pablomellado.pandealfacar.R;
import es.pablomellado.pandealfacar.model.Product;
import es.pablomellado.pandealfacar.order.ShoppingCartFragment.OnFragmentInteractionListener;
import es.pablomellado.pandealfacar.order.Cart.CartItem;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;

import static es.pablomellado.pandealfacar.Utils.loadImage;

/**
 * {@link RecyclerView.Adapter} that can display a {@link CartItem} and makes a call to the
 * specified {@link OnFragmentInteractionListener}.
 */
public class MyShoppingCartRecyclerViewAdapter extends RecyclerView.Adapter<MyShoppingCartRecyclerViewAdapter.ViewHolder> {

    private final int CART_ITEM = 0;
    private final int TOTALS = 1;

    private List<CartItem> mValues;
    private final OnFragmentInteractionListener mListener;
    private final ChangeItemQuantity mFragmentListener;

    private Context mContext;

    public interface ChangeItemQuantity {
        void changeItemQuantity(CartItem item, int oldQuantity);
        void updateMenu();
    }

    public MyShoppingCartRecyclerViewAdapter(List<CartItem> items,
                                             ShoppingCartFragment.OnFragmentInteractionListener listener,
                                             ChangeItemQuantity fragmentListener) {
        mValues = items;
        mListener = listener;
        mFragmentListener = fragmentListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        mContext = parent.getContext();
        if (viewType==CART_ITEM) {
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.fragment_shoppingcart, parent, false);
            view.setTag(null);
        }
        else {
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.fragment_shoppingcart_totals, parent, false);
            view.setTag("TOTALS");

        }
        return new ViewHolder(view);
    }

    public void notifyCartChanged(){
        super.notifyDataSetChanged();
        mValues = SessionHelper.getCart().getCartItemsList();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (getItemViewType(position)==CART_ITEM) {
            holder.mItem = mValues.get(position);
            final Product product = mValues.get(position).product;
            final int quantity = mValues.get(position).quantity;

            loadImage(mContext, product.getImgUrl(), holder.mImageView);

            holder.mNameView.setText(product.getName());
            holder.mTotalView.setText(NumberFormat.getCurrencyInstance().format(product.getPrice().multiply(new BigDecimal(quantity))));
            holder.mPriceView.setText(NumberFormat.getCurrencyInstance().format(product.getPrice()));
            holder.mQuantityView.setText(new Integer(quantity).toString());

            holder.mButtonDeleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SessionHelper.getCart().remove((Saleable) product);
                    removeAt(position);
                    mFragmentListener.updateMenu();
                }
            });

            holder.mQuantityView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFragmentListener.changeItemQuantity(holder.mItem, quantity);
                }
            });

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onListFragmentInteraction(holder.mItem);
                    }
                }
            });
        }
        else{
            BigDecimal total = SessionHelper.getCart().getTotalPrice();
            if (getItemCount()>1) {
                holder.mCardTotal.setVisibility(View.VISIBLE);
                holder.mCardEmptyCart.setVisibility(View.GONE);
                holder.mTotalCost.setText(NumberFormat.getCurrencyInstance().format(total));
            }
            else{
                holder.mCardTotal.setVisibility(View.GONE);
                holder.mCardEmptyCart.setVisibility(View.VISIBLE);
            }
        }
    }

    public void removeAt(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }
    @Override
    public int getItemCount() {
        return mValues.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getItemCount()-1) {
            return CART_ITEM;
        } else {
            return TOTALS;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImageView;
        public final TextView mNameView;
        public final TextView mPriceView;
        public final TextView mTotalView;
        public final TextView mQuantityView;
        public final Button mButtonDeleteView;
        public final TextView mTotalCost;
        public final TextView mTitleTotalCost;
        public final TextView mEmptyCart;
        public final CardView mCardTotal;
        public final CardView mCardEmptyCart;
        public CartItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            if (mView.getTag()=="TOTALS"){
                mImageView = null;
                mNameView = null;
                mPriceView = null;
                mTotalView = null;
                mQuantityView = null;
                mButtonDeleteView = null;
            }
            else {
                mImageView = (ImageView) view.findViewById(R.id.image);
                mNameView = (TextView) view.findViewById(R.id.name);
                mPriceView = (TextView) view.findViewById(R.id.price);
                mTotalView = (TextView) view.findViewById(R.id.total);
                mQuantityView = (TextView) view.findViewById(R.id.quantity);
                mButtonDeleteView = (Button) view.findViewById(R.id.button_delete);
            }
            mTotalCost = (TextView)view.findViewById(R.id.total_cost);
            mTitleTotalCost = (TextView)view.findViewById(R.id.title_total);
            mEmptyCart = (TextView)view.findViewById(R.id.empty_cart);
            mCardTotal = (CardView)view.findViewById(R.id.card_total);
            mCardEmptyCart = (CardView)view.findViewById(R.id.card_empty_cart);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
