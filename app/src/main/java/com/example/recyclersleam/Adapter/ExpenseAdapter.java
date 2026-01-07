package com.example.recyclersleam.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.recyclersleam.Dao.LocationDao;
import com.example.recyclersleam.Location.LocationEntity;
import com.example.recyclersleam.Entity.Expense;
import com.example.recyclersleam.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;
    private LocationDao locationDao;

    public ExpenseAdapter(List<Expense> expenses, LocationDao locationDao) {
        this.expenses = expenses;
        this.locationDao = locationDao;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);

        holder.tvTitle.setText(expense.getTitle());
        holder.tvAmount.setText("Montant : " + expense.getAmount() + " DT");

        // Afficher l'image si elle existe
        String imagePath = expense.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                holder.ivPhoto.setImageBitmap(bitmap);
            } else {
                holder.ivPhoto.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } else {
            holder.ivPhoto.setImageResource(android.R.drawable.ic_menu_camera);
        }

        LocationEntity loc = locationDao.getLocationForExpense(expense.id);
        if (loc != null) {
            holder.tvLocationInfo.setText(loc.adresse != null ? loc.adresse : "Localisation captée");
        } else {
            holder.tvLocationInfo.setText("Pas de localisation");
        }
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvAmount;
        TextView tvLocationInfo;
        ImageView ivPhoto;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvAmount = itemView.findViewById(R.id.tvItemAmount);
            tvLocationInfo = itemView.findViewById(R.id.tvItemLocationInfo);
            ivPhoto = itemView.findViewById(R.id.ivItemExpensePhoto);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Expense expense = expenses.get(position);
                    // ViewExpenseActivity not ported yet, functionality disabled for now.
                    /*
                     * android.content.Intent intent = new android.content.Intent(v.getContext(),
                     * com.example.pocketmanager.Expenses.ViewExpenseActivity.class);
                     * intent.putExtra(com.example.pocketmanager.Expenses.ViewExpenseActivity.
                     * EXTRA_EXPENSE_ID,
                     * expense.getId());
                     * v.getContext().startActivity(intent);
                     */
                }
            });
        }
    }
}
