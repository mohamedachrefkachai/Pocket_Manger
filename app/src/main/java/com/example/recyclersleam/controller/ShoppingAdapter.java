package com.example.recyclersleam.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclersleam.Entity.ShoppingItem;
import com.example.recyclersleam.R;

import java.util.ArrayList;
import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder> {

    public interface OnItemClickListener {
        void onDeleteClick(ShoppingItem item);

        void onCheckChange(ShoppingItem item, boolean isChecked);
    }

    private List<ShoppingItem> mList = new ArrayList<>();
    private OnItemClickListener mListener;

    public ShoppingAdapter(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void setList(List<ShoppingItem> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShoppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping, parent, false);
        return new ShoppingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingViewHolder holder, int position) {
        ShoppingItem item = mList.get(position);
        holder.tvItemName.setText(item.getName());
        holder.cbBought.setChecked(item.isBought());

        holder.btnDelete.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDeleteClick(item);
            }
        });

        holder.cbBought.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mListener != null) {
                mListener.onCheckChange(item, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ShoppingViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbBought;
        TextView tvItemName;
        ImageView btnDelete;

        public ShoppingViewHolder(@NonNull View itemView) {
            super(itemView);
            cbBought = itemView.findViewById(R.id.cbBought);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
