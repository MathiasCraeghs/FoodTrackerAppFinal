package com.mathiascraeghs.foodtracker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Craeghs Mathias on 14/03/2018.
 */

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantAdapterViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    public RestaurantAdapter(Context context, Cursor cursor){

        this.mContext = context;
        this.mCursor = cursor;

    }

    @Override
    public RestaurantAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.restaurant_recycler_view_list;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        RestaurantAdapterViewHolder viewHolder = new RestaurantAdapterViewHolder(view);

        return viewHolder;


    }

    @Override
    public void onBindViewHolder(RestaurantAdapterViewHolder holder, int position) {
      if(!mCursor.moveToPosition(position)) return;
        String name;
        int score;
        name= mCursor.getString(mCursor.getColumnIndex("name"));
        score=  mCursor.getInt(mCursor.getColumnIndex("rating"));
        holder.listItemNumberView.setText(name);
        if(score ==0){
            holder.scoreView.setImageResource(R.drawable.medium);
        }
        else if(score <= 1){
            holder.scoreView.setImageResource(R.drawable.worst);
        }
        else if(score>1 && score <=2){
            holder.scoreView.setImageResource(R.drawable.bad);

        }
        else if(score>2 && score <=3){
            holder.scoreView.setImageResource(R.drawable.medium);
        }
        else if(score>3 &&score <=4){
            holder.scoreView.setImageResource(R.drawable.good);
        }
        else{
            holder.scoreView.setImageResource(R.drawable.best);
        }

    }

    @Override
    public int getItemCount() {

       if(mCursor == null) return 0;
        return mCursor.getCount();
    }


    class RestaurantAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      private  TextView listItemNumberView;
      private ImageView scoreView;

        public RestaurantAdapterViewHolder(View view) {
            super(view);

            listItemNumberView = (TextView) view.findViewById(R.id.tv_item_number);
            scoreView = (ImageView) view.findViewById(R.id.tv_id_score);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition= getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            String address = mCursor.getString(mCursor.getColumnIndex("vicinity"));
            String nameRest = mCursor.getString(mCursor.getColumnIndex("name"));
            String score =mCursor.getString(mCursor.getColumnIndex("rating"));
            String location = mCursor.getString(mCursor.getColumnIndexOrThrow("geometry"));


            int latitudeStart= location.indexOf("lat") + ("lat\":").length();
            int latitudeEnd= location.indexOf(",");
            String lat = location.substring(latitudeStart,latitudeEnd);

            int longitudeStart = location.indexOf("lng") +("lng\";").length();
            int longitudeEnd = location.indexOf("}");
            String lng = location.substring(longitudeStart,longitudeEnd);

           Log.i("info",nameRest.toString());
           Log.i("info",address.toString());
           Log.i("info",String.valueOf(score));
           Log.i("info",location.toString());
           Log.i("info",String.valueOf(latitudeStart));
           Log.i("info",String.valueOf(latitudeEnd));
           Log.i("info",lat.toString());
           Log.i("info",lng.toString());


            Intent intent = new Intent(mContext, ChildActivity.class);

            intent.putExtra(mContext.getString(R.string.id_key), adapterPosition);
            intent.putExtra(mContext.getString(R.string.name_key),nameRest);
            intent.putExtra(mContext.getString(R.string.address_key),address);
            intent.putExtra(mContext.getString(R.string.score_key),score);
            intent.putExtra(mContext.getString(R.string.lat_key),lat);
            intent.putExtra(mContext.getString(R.string.lng_key),lng);

            mContext.startActivity(intent);
        }
    }


}
