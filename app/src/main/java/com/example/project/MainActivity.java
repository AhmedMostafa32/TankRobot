package com.example.project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private Animation rotateAnimation;
    private ImageView imageView;
    private Button login;
    private TextView signup;
    private ActivityMainBinding binding;
    private DBHelper DB;
    private CheckBox rememberMeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imageView = binding.truu;
        rotateAnimation();

        login = binding.login;
        signup = binding.btnsign;
        rememberMeCheckBox = binding.checkbox;

        DB = new DBHelper(this);

        // Retrieve stored email and password
        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String storedEmail = preferences.getString("email", "");
        String storedPassword = preferences.getString("password", "");
        boolean rememberMe = preferences.getBoolean("rememberMe", false);

        if (rememberMe) {
            binding.email.setText(storedEmail);
            binding.pass.setText(storedPassword);
            rememberMeCheckBox.setChecked(true);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.email.getText().toString();
                String password = binding.pass.getText().toString();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please ensure all fields are completed.", Toast.LENGTH_SHORT).show();
                } else {
                    boolean checkPassword = DB.checkUsernameEmailPassword(password, email);
                    if (checkPassword) {
                        Toast.makeText(MainActivity.this, "Sign in successfully", Toast.LENGTH_SHORT).show();
                        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Loading...");
                        progressDialog.setTitle("Connecting Please Wait...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        progressDialog.setCancelable(false);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                progressDialog.dismiss();
                            }
                        }).start();

                        // Save email and password if "Remember me" is checked
                        SharedPreferences.Editor editor = preferences.edit();
                        if (rememberMeCheckBox.isChecked()) {
                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.putBoolean("rememberMe", true);
                        } else {
                            editor.clear();
                        }
                        editor.apply();

                        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "The user entered is invalid.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.showPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    binding.pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIntent = new Intent(MainActivity.this, MainActivity3.class);
                startActivity(signIntent);
            }
        });
    }

    private void rotateAnimation() {
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        imageView.startAnimation(rotateAnimation);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}