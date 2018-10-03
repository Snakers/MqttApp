package com.deeptech.blocker.tawsiat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;

public class RecyAdabter extends  RecyclerView.Adapter<RecyAdabter.MyViewHolder> {
    private static Context context;
    ArrayList<SenderInfo> senderInfo;
    public RecyAdabter(Context context, ArrayList<SenderInfo> senderInfo) {
        this.context=context;
        this.senderInfo=senderInfo;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        SenderInfo sender = senderInfo.get(position);
if(sender.isArabic()==false){
    holder.data.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

    holder.data.setText(sender.getData());

    Log.d("testing2","else");

}else if(sender.isArabic()){
    holder.data.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

    holder.data.setText(sender.getData());

    Log.d("testing2","else if");
}
holder.name.setText(sender.getName());
holder.date.setText(sender.getDate());
    }



    @Override
    public int getItemCount() {
        return senderInfo.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
    private TextView name,data,date;
        public MyViewHolder(View itemView) {
            super(itemView);
name=(TextView)itemView.findViewById(R.id.name);
data=(TextView)itemView.findViewById(R.id.data);
date=(TextView)itemView.findViewById(R.id.date);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}