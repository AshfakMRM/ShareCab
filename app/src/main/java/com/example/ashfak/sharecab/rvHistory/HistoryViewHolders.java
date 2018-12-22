package com.example.ashfak.sharecab.rvHistory;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.ashfak.sharecab.HistorySingleActivity;
import com.example.ashfak.sharecab.R;

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideId;
    public TextView time;

    public HistoryViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideId = (TextView)itemView.findViewById(R.id.tvRideId);
        time = (TextView)itemView.findViewById(R.id.tvTime);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId" , rideId.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);

    }
}
