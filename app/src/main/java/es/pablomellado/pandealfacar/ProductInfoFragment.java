package es.pablomellado.pandealfacar;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import es.pablomellado.pandealfacar.dialog.QuantitySelect;
import es.pablomellado.pandealfacar.order.Cart;
import es.pablomellado.pandealfacar.order.SessionHelper;

import static es.pablomellado.pandealfacar.Utils.loadImage;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProductInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductInfoFragment extends Fragment
        implements NumberPicker.OnValueChangeListener, QuantitySelect.QuantitySelectListener{


    private ProductInfoRow productInfo;

    public ProductInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param productInfo info of the product to be displayed.
     * @return A new instance of fragment ProductInfoFragment.
     */
    public static ProductInfoFragment newInstance(ProductInfoRow productInfo) {
        ProductInfoFragment fragment = new ProductInfoFragment();
        fragment.setProduct(productInfo);

        return fragment;
    }

    public void setProduct(ProductInfoRow productInfoRow){
        this.productInfo = productInfoRow;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_product_info, container, false);
        TextView tvProductName = (TextView)root.findViewById(R.id.product_name);
        TextView tvProductPrice = (TextView)root.findViewById(R.id.product_price);
        ImageView ivProductImage = (ImageView)root.findViewById(R.id.product_image);

        tvProductName.setText(productInfo.productInfo.getName());
        tvProductPrice.setText(String.format("%.2f €", productInfo.productInfo.getPrice()));
        loadImage(getActivity(), productInfo.productInfo.getImgUrl(), ivProductImage);
        Button b = (Button) root.findViewById(R.id.product_add_button);
        b.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v) {
                onAddButtonPressed();
            }
        });
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        String toolbarTitle = productInfo.productInfo.getName();
        toolbar.setTitle(toolbarTitle);
        toolbar.setNavigationIcon(R.mipmap.ic_action_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        getActivity().onBackPressed();

                    }
                }
        );
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

        Log.i("value is",""+newVal);

    }

    public void onAddButtonPressed() {
        FragmentManager manager = this.getFragmentManager();
        QuantitySelect quantitySelect = new QuantitySelect();
        quantitySelect.setTargetFragment(this, 1);
        quantitySelect.show(manager, "fragment_select_quantity");
    }

    public void onFinishQuantitySelectDialog(int quantity){
        // Add to the basket
        Cart cart = SessionHelper.getCart();
        cart.add(productInfo.productInfo,quantity);
        productInfo.quantity = cart.getItemWithQuantity().get(productInfo.productInfo.getId());
        getActivity().onBackPressed();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

}
