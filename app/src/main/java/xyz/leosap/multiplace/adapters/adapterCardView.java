package xyz.leosap.multiplace.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import xyz.leosap.multiplace.R;
import xyz.leosap.multiplace.objects.Place;

/**
 * Created by LeoSap on 30/11/2016.
 */

public class adapterCardView extends RecyclerView.Adapter<adapterCardView.MyViewHolder> {


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv1, tv2;
        public ImageView iv1;

        public MyViewHolder(View view) {
            super(view);

            tv1 = (TextView) view.findViewById(R.id.tv_nombre);
            tv2 = (TextView) view.findViewById(R.id.tv_ubicacion);
            iv1 = (ImageView) view.findViewById(R.id.imageView4);
        }
    }

    private Context context;
    private ArrayList<Place> places;

    public adapterCardView(Context context, ArrayList<Place> places){
        this.context = context;
    this.places = places;
    }


    @Override
    public adapterCardView.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_card_view, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(adapterCardView.MyViewHolder holder, int position) {
        Place place = places.get(position);
        holder.tv1.setText(place.getName());
        holder.tv2.setText(place.getLat() + "," + place.getLng());
        Log.d("LS img",place.getImage());
        Picasso.with(context)
                .load(R.drawable.icon)
                //.config(Bitmap.Config.RGB_565)
                .error(R.drawable.icon)
                .fit()
                .placeholder(R.drawable.icon)
                .centerInside()
                .into(holder.iv1);



    }

    @Override
    public int getItemCount() {
        return places.size();
    }
}
