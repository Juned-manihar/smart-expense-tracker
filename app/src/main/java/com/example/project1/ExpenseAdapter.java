package com.example.project1;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private Context context;
    private ArrayList<Expense> expenseList;
    private DatabaseHelper dbHelper;
    private OnExpenseDeletedListener deleteListener;

    public interface OnExpenseDeletedListener {
        void onExpenseDeleted();
    }

    public ExpenseAdapter(Context context, ArrayList<Expense> expenseList, OnExpenseDeletedListener listener) {
        this.context = context;
        this.expenseList = expenseList;
        this.dbHelper = new DatabaseHelper(context);
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.tvAmount.setText("₹" + String.format("%.2f", expense.getAmount()));
        holder.tvDescription.setText(expense.getDescription());
        holder.tvCategory.setText(expense.getCategory());
        holder.tvDate.setText(expense.getDate() + " " + expense.getTime());

        // Set category color
        int color = getCategoryColor(expense.getCategory());
        holder.tvCategory.setBackgroundColor(color);

        // Long press to delete
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(expense, position);
            return true;
        });
    }

    private int getCategoryColor(String category) {
        switch (category) {
            case "Food": return 0xFFFF9800; // Orange
            case "Travel": return 0xFF2196F3; // Blue
            case "Shopping": return 0xFFE91E63; // Pink
            case "Bills": return 0xFFF44336; // Red
            case "Others": return 0xFF9E9E9E; // Grey
            default: return 0xFF9E9E9E;
        }
    }

    private void showDeleteDialog(Expense expense, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteExpense(expense.getId());
                    expenseList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, expenseList.size());
                    Toast.makeText(context, "Expense deleted", Toast.LENGTH_SHORT).show();
                    if (deleteListener != null) {
                        deleteListener.onExpenseDeleted();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvDescription, tvCategory, tvDate;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    public void updateList(ArrayList<Expense> newList) {
        expenseList = newList;
        notifyDataSetChanged();
    }
}