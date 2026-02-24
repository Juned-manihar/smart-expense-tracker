package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmailOrUsername, etPassword;
    private Button btnLogin;
    private TextView tvGoToSignup;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Already logged in? Go to main
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmailOrUsername = findViewById(R.id.etEmailOrUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToSignup = findViewById(R.id.tvGoToSignup);
        dbHelper = new DatabaseHelper(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tvGoToSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    private void loginUser() {
        String emailOrUsername = etEmailOrUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (emailOrUsername.isEmpty()) {
            etEmailOrUsername.setError("Email or Username required!");
            etEmailOrUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password required!");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters!");
            etPassword.requestFocus();
            return;
        }

        // Login check
        int userId = dbHelper.loginUser(emailOrUsername, password);

        if (userId != -1) {
            String username = dbHelper.getUsername(userId);
            String email = dbHelper.getEmail(userId);

            // Save session
            sessionManager.createSession(userId, username, email);

            Toast.makeText(this, "Welcome back, " + username + "! 👋",
                    Toast.LENGTH_SHORT).show();

            // Go to main screen
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials! Try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}