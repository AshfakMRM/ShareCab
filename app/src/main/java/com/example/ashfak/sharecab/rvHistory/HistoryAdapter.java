package com.example.ashfak.sharecab.rvHistory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ashfak.sharecab.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders> {

    private List<HistoryObject> itemList;
    private Context context;

    public HistoryAdapter(List<HistoryObject> itemList, Context context){
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryViewHolders onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_history, null,false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        HistoryViewHolders rv = new HistoryViewHolders(layoutView);
        return rv;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolders historyViewHolders, int i) {
        historyViewHolders.rideId.setText(itemList.get(i).getRideId());
        historyViewHolders.time.setText(itemList.get(i).getTime());
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}
