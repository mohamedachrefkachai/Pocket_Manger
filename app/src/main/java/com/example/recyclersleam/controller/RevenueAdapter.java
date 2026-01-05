package com.example.recyclersleam.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclersleam.Entity.Revenue;
import com.example.recyclersleam.R;

import java.util.List;

public class RevenueAdapter extends RecyclerView.Adapter<RevenueAdapter.RevenueViewHolder> {

    private final List<Revenue> revenues;
    private final OnRevenueClickListener listener;

    public interface OnRevenueClickListener {
        void onDeleteClick(Revenue revenue);

        void onItemClick(Revenue revenue);
    }

    public RevenueAdapter(List<Revenue> revenues, OnRevenueClickListener listener) {
        this.revenues = revenues;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RevenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_revenue, parent, false);
        return new RevenueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RevenueViewHolder holder, int position) {
        Revenue r = revenues.get(position);
        holder.tvSource.setText(r.getSource());
        holder.tvDate.setText(r.getDate());
        holder.tvAmount.setText("+ " + r.getAmount() + " TND");

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(r));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(r));
    }

    @Override
    public int getItemCount() {
        return revenues.size();
    }

    static class RevenueViewHolder extends RecyclerView.ViewHolder {
        TextView tvSource, tvDate, tvAmount;
        ImageView btnDelete;

        public RevenueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSource = itemView.findViewById(R.id.tvSource);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            btnDelete = itemView.findViewById(R.id.btnDeleteRevenue);
        }
    }
}
