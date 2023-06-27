package com.busanit.androidchallenge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> implements OnInfoButtonClickListener {

    ArrayList<Item> items = new ArrayList<>();
    OnInfoButtonClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.items,parent,false);
        return new ViewHolder(itemView,this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        int pos = position;
        holder.title.setText(item.getTitle());
        holder.distance.setText(item.getDist());
        String url = item.getFirstimage2();
        Glide.with(holder.imageView.getContext()).load(url).fallback(R.drawable.kto).error(R.drawable.kto).into(holder.imageView);
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public Item getItem(int position){
        return items.get(position);
    }

    public void setListener(OnInfoButtonClickListener listener){
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onButtonClick(ViewHolder holder, View view, int position) {
        if(listener!=null){
            listener.onButtonClick(holder, view, position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title, distance;
        ImageView imageView;
        Button btnInfo;

        public ViewHolder(@NonNull View itemView, final OnInfoButtonClickListener listener) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            distance = itemView.findViewById(R.id.distance);
            imageView = itemView.findViewById(R.id.imageView);
            btnInfo = itemView.findViewById(R.id.btnInfo);
            btnInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener!=null){
                        listener.onButtonClick(ViewHolder.this, v, position);
                    }
                }
            });
        }
    }
}
