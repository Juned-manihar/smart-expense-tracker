package com.example.expensetracker;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

public class SummaryActivity extends AppCompatActivity {
    private TextView tvTotalExpense, tvCategoryBreakdown, tvUserInfo;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvCategoryBreakdown = findViewById(R.id.tvCategoryBreakdown);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        TextView btnBack = findViewById(R.id.btnBack);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        int userId = sessionManager.getUserId();
        tvUserInfo.setText("📊 " + sessionManager.getUsername() + "'s Expense Summary");

        btnBack.setOnClickListener(v -> finish());

        loadSummary(userId);
    }

    private void loadSummary(int userId) {
        double totalExpense = dbHelper.getTotalExpenses(userId);
        tvTotalExpense.setText("Total Expenses: ₹" + String.format("%.2f", totalExpense));

        HashMap<String, Double> categoryMap = dbHelper.getCategoryWiseExpenses(userId);
        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Category-wise Breakdown:\n\n");

        if (categoryMap.isEmpty()) {
            breakdown.append("No expenses recorded yet.");
        } else {
            for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
                breakdown.append("• ").append(entry.getKey())
                        .append(": ₹")
                        .append(String.format("%.2f", entry.getValue()))
                        .append("\n");
            }
        }
        tvCategoryBreakdown.setText(breakdown.toString());
    }
}