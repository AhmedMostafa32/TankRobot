package com.example.project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.databinding.ActivityMain3Binding;

public class MainActivity3 extends AppCompatActivity {

    private Button sign;
    private ImageButton arrow;
    private TextView passwordRequirements;
    private ActivityMain3Binding binding;
    private DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the DBHelper instance
        DB = new DBHelper(this);

        sign = binding.signupBtn;
        arrow = binding.arrow;
        passwordRequirements = findViewById(R.id.password_requirements);

        sign.setOnClickListener(v -> {
            String name = binding.name.getText().toString();
            String pass = binding.pass.getText().toString();
            String email = binding.email.getText().toString();
            String confirmPass = binding.confirmPass.getText().toString();

            if (name.isEmpty() || pass.isEmpty() || email.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please ensure all fields are completed.", Toast.LENGTH_SHORT).show();
            } else {
                if (!isValidEmail(email)) {
                    Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                } else if (!isValidPassword(pass)) {
                    passwordRequirements.setVisibility(TextView.VISIBLE);
                } else if (!pass.equals(confirmPass)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    boolean checkUser = DB.checkUserEmail(email);
                    if (!checkUser) {
                        boolean insert = DB.insertData(email, pass, confirmPass, name); // Passing confirmPass to insertData
                        if (insert) {
                            Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
                            ProgressDialog progressDialog = new ProgressDialog(this);
                            progressDialog.setMessage("Loading..."); // Setting Message
                            progressDialog.setTitle("Connecting Please Wait..."); // Setting Title
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                            progressDialog.show(); // Display Progress Dialog
                            progressDialog.setCancelable(false);

                            new Thread(() -> {
                                try {
                                    Thread.sleep(2500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                progressDialog.dismiss();
                            }).start();

                            Intent intent = new Intent(MainActivity3.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        arrow.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity3.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }
}
