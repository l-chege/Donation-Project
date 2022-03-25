package com.example.donationproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SelectRegistrationActivity extends AppCompatActivity {

    private Button donorButton, managerButton, volunteerButton;
    private TextView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_registration);

        donorButton = findViewById(R.id.donorButton);
        managerButton = findViewById(R.id.managerButton);
        volunteerButton = findViewById(R.id.volunteerButton);
        backButton = findViewById(R.id.backButton);

        donorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        SelectRegistrationActivity.this, DonorRegistrationActivity.class);
                startActivity(intent);

            }
        });

        managerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        SelectRegistrationActivity.this, ManagerRegistrationActivity.class);
                startActivity(intent);

            }
        });

        volunteerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        SelectRegistrationActivity.this, VolunteerRegistrationActivity.class);
                startActivity(intent);

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        SelectRegistrationActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });
    }
}