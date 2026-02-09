package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseDeletedListener {
    private EditText etAmount, etDescription;
    private Spinner spinnerCategory, spinnerFilter;
    private Button btnSave, btnViewSummary;
    private TextView tvTotalAmount;
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private DatabaseHelper dbHelper;
    private ArrayList<Expense> expenseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        btnSave = findViewById(R.id.btnSave);
        btnViewSummary = findViewById(R.id.btnViewSummary);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        recyclerView = findViewById(R.id.recyclerView);

        dbHelper = new DatabaseHelper(this);

        // Setup category spinner
        String[] categories = {"Food", "Travel", "Shopping", "Bills", "Others"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Setup filter spinner
        String[] filterOptions = {"All Categories", "Food", "Travel", "Shopping", "Bills", "Others"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        expenseList = new ArrayList<>();
        adapter = new ExpenseAdapter(this, expenseList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadExpenses();
        updateTotalAmount();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExpense();
            }
        });

        btnViewSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
                startActivity(intent);
            }
        });

        // Filter listener
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterExpenses(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void saveExpense() {
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (amountStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        long id = dbHelper.addExpense(amount, description, category);

        if (id > 0) {
            Toast.makeText(this, "Expense saved successfully! ✓", Toast.LENGTH_SHORT).show();
            etAmount.setText("");
            etDescription.setText("");
            loadExpenses();
            updateTotalAmount();
            spinnerFilter.setSelection(0); // Reset filter
        } else {
            Toast.makeText(this, "Error saving expense", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadExpenses() {
        expenseList = dbHelper.getAllExpenses();
        adapter.updateList(expenseList);
    }

    private void filterExpenses(int position) {
        if (position == 0) {
            // All Categories
            loadExpenses();
        } else {
            String[] categories = {"Food", "Travel", "Shopping", "Bills", "Others"};
            String selectedCategory = categories[position - 1];
            expenseList = dbHelper.getExpensesByCategory(selectedCategory);
            adapter.updateList(expenseList);
        }
        updateTotalAmount();
    }

    private void updateTotalAmount() {
        double total = 0;
        for (Expense expense : expenseList) {
            total += expense.getAmount();
        }
        tvTotalAmount.setText("Total: ₹" + String.format("%.2f", total));
    }

    @Override
    public void onExpenseDeleted() {
        updateTotalAmount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses();
        updateTotalAmount();
    }
}