package com.example.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AboutUsActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        ImageButton img=findViewById(R.id.arrow);

     img.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             Intent intent1=new Intent(AboutUsActivity.this,MainActivity2.class);
             startActivity(intent1);
         }
     });
    }
    public void onBackPressed() {
        super.onBackPressed();
        Intent  intent=new Intent(AboutUsActivity.this,MainActivity2.class);
        startActivity(intent);
    }
}