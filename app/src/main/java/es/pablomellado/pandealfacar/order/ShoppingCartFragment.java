package es.pablomellado.pandealfacar.order;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import es.pablomellado.pandealfacar.R;
import es.pablomellado.pandealfacar.dialog.QuantitySelect;
import es.pablomellado.pandealfacar.order.Cart.CartItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ShoppingCartFragment extends Fragment
    implements MyShoppingCartRecyclerViewAdapter.ChangeItemQuantity,
        QuantitySelect.QuantitySelectListener{

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnFragmentInteractionListener mListener;
    private CartItem mCurrentCartItem;
    private MyShoppingCartRecyclerViewAdapter mShoppingCartRecycleViewAdapter;
    private Menu mFragmentMenu;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ShoppingCartFragment() {
    }

    @SuppressWarnings("unused")
    public static ShoppingCartFragment newInstance(int columnCount) {
        ShoppingCartFragment fragment = new ShoppingCartFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shoppingcart_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mShoppingCartRecycleViewAdapter = new MyShoppingCartRecyclerViewAdapter(SessionHelper.getCart().getCartItemsList(),
                    mListener, (MyShoppingCartRecyclerViewAdapter.ChangeItemQuantity)this);
            recyclerView.setAdapter(mShoppingCartRecycleViewAdapter);

        }
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.shopping_cart_title);
        toolbar.setNavigationIcon(R.mipmap.ic_action_back);
        inflater.inflate(R.menu.menu_order, menu);

        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        getActivity().onBackPressed();

                    }
                }
        );
        mFragmentMenu = menu;
        toggleOrderMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_place_order:
                mListener.openConfirmOrderFragment();

                return true;

            default:
                break;
        }

        return false;
    }

    private void toggleOrderMenu(){
        if(SessionHelper.getCart().getTotalQuantity() == 0){
            mFragmentMenu.findItem(R.id.action_place_order).setVisible(false);
        }
        else{
            mFragmentMenu.findItem(R.id.action_place_order).setVisible(true);
        }
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



    public void changeItemQuantity(CartItem cartItem, int oldQuantity){
        FragmentManager manager = this.getFragmentManager();
        QuantitySelect quantitySelect = new QuantitySelect();
        quantitySelect.setTargetFragment(this, 1);
        Bundle bundle = new Bundle();
        bundle.putInt("quantity", oldQuantity);
        quantitySelect.setArguments(bundle);
        quantitySelect.show(manager, "fragment_select_quantity");
        mCurrentCartItem = cartItem;
    }

    @Override
    public void updateMenu() {
        toggleOrderMenu();
    }

    public void onFinishQuantitySelectDialog(int newQuantity){
        // Add to the basket
        Cart cart = SessionHelper.getCart();
        cart.update(mCurrentCartItem.product, newQuantity);
        mShoppingCartRecycleViewAdapter.notifyCartChanged();
    }

    public interface OnFragmentInteractionListener {
        void onListFragmentInteraction(CartItem item);
        void openConfirmOrderFragment();
    }
}
