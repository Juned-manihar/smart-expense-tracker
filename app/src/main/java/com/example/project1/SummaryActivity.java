package com.example.project1;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

public class SummaryActivity extends AppCompatActivity {
    private TextView tvTotalExpense, tvCategoryBreakdown;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvCategoryBreakdown = findViewById(R.id.tvCategoryBreakdown);
        dbHelper = new DatabaseHelper(this);

        loadSummary();
    }

    private void loadSummary() {
        double totalExpense = dbHelper.getTotalExpenses();
        tvTotalExpense.setText("Total Expenses: ₹" + String.format("%.2f", totalExpense));

        HashMap<String, Double> categoryMap = dbHelper.getCategoryWiseExpenses();
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Category-wise Breakdown:\n\n");

        if (categoryMap.isEmpty()) {
            breakdown.append("No expenses recorded yet.");
        } else {
            for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
                breakdown.append(entry.getKey())
                        .append(": ₹")
                        .append(String.format("%.2f", entry.getValue()))
                        .append("\n");
            }
        }

        tvCategoryBreakdown.setText(breakdown.toString());
    }
}