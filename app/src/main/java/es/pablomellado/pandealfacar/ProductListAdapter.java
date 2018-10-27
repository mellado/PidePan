package es.pablomellado.pandealfacar;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static es.pablomellado.pandealfacar.Utils.loadImage;

/**
 * Created by Pablo Mellado on 13/4/17.
 */


public class ProductListAdapter extends ArrayAdapter<ProductInfoRow> {
    private Context mContext;
    public ProductListAdapter(Context context, int resource, List<ProductInfoRow> objects) {
        super(context, resource, objects);
        mContext = context;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.grid_item_product, parent, false);
        }

        final ProductInfoRow item = this.getItem(position);
        ((TextView)convertView.findViewById(R.id.product_text)).setText(item.productInfo.getName());
        ((TextView)convertView.findViewById(R.id.product_price)).setText(String.format( "%.2f €",
                item.productInfo.getPrice()));



        ImageView productPicture = (ImageView)convertView.findViewById(R.id.product_picture);
        loadImage(mContext, item.productInfo.getImgUrl(), productPicture);


        return convertView;
    }

}


