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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseDeletedListener {
    private EditText etAmount, etDescription;
    private Spinner spinnerCategory, spinnerFilter;
    private Button btnSave, btnViewSummary;
    private TextView tvTotalAmount, tvWelcome, tvUserEmail;
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private ArrayList<Expense> expenseList;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Check login
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        currentUserId = sessionManager.getUserId();

        // Views
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        btnSave = findViewById(R.id.btnSave);
        btnViewSummary = findViewById(R.id.btnViewSummary);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        recyclerView = findViewById(R.id.recyclerView);

        dbHelper = new DatabaseHelper(this);

        // Show user info
        tvWelcome.setText("Hey, " + sessionManager.getUsername() + "! 👋");
        tvUserEmail.setText(sessionManager.getEmail());

        // Category Spinner
        String[] categories = {"Food", "Travel", "Shopping", "Bills", "Others"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Filter Spinner
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

        btnSave.setOnClickListener(v -> saveExpense());
        btnViewSummary.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
            intent.putExtra("userId", currentUserId);
            startActivity(intent);
        });

        // Username edit - click on welcome text
        tvWelcome.setOnClickListener(v -> showEditUsernameDialog());

        // Logout button
        findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());

        // Filter
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterExpenses(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showEditUsernameDialog() {
        EditText etNewUsername = new EditText(this);
        etNewUsername.setHint("Enter new username");
        etNewUsername.setText(sessionManager.getUsername());
        etNewUsername.setPadding(32, 16, 32, 16);

        new AlertDialog.Builder(this)
                .setTitle("Edit Username ✏️")
                .setView(etNewUsername)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newUsername = etNewUsername.getText().toString().trim();
                    if (!newUsername.isEmpty() && newUsername.length() >= 3) {
                        dbHelper.updateUsername(currentUserId, newUsername);
                        sessionManager.updateUsername(newUsername);
                        tvWelcome.setText("Hey, " + newUsername + "! 👋");
                        Toast.makeText(this, "Username updated! ✅", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Username must be at least 3 characters!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveExpense() {
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (amountStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        long id = dbHelper.addExpense(amount, description, category, currentUserId);

        if (id > 0) {
            Toast.makeText(this, "Expense saved! ✅", Toast.LENGTH_SHORT).show();
            etAmount.setText("");
            etDescription.setText("");
            loadExpenses();
            updateTotalAmount();
            spinnerFilter.setSelection(0);
        } else {
            Toast.makeText(this, "Error saving expense!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadExpenses() {
        expenseList = dbHelper.getAllExpenses(currentUserId);
        adapter.updateList(expenseList);
    }

    private void filterExpenses(int position) {
        if (position == 0) {
            loadExpenses();
        } else {
            String[] categories = {"Food", "Travel", "Shopping", "Bills", "Others"};
            expenseList = dbHelper.getExpensesByCategory(categories[position - 1], currentUserId);
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