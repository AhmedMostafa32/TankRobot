package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RateUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_us);
        RatingBar ratingBar;
        Button submitButton;
        ratingBar = findViewById(R.id.rating_bar);
        submitButton = findViewById(R.id.submit_button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBar.getRating();

                // Handle rating submission based on rating level
                if (rating <= 2) {
                    Toast.makeText(RateUsActivity.this, "We're sorry you had a bad experience. Please let us know how we can improve.", Toast.LENGTH_LONG).show();
                } else if (rating <= 4) {
                    Toast.makeText(RateUsActivity.this, "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RateUsActivity.this, "Thank you for the excellent rating!", Toast.LENGTH_LONG).show();
                }

                // Navigate back to MainActivity2
                Intent intent = new Intent(RateUsActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });

    }
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}