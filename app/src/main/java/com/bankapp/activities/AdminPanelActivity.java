package com.bankapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bankapp.R;

public class AdminPanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        ImageButton btnBack = findViewById(R.id.btnAdminBack);
        btnBack.setOnClickListener(v -> finish());

        CardView cardUsers    = findViewById(R.id.cardAdminUsers);
        CardView cardBankInfo = findViewById(R.id.cardAdminBankInfo);

        cardUsers.setOnClickListener(v ->
                startActivity(new Intent(this, AdminUsersActivity.class)));
        cardBankInfo.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBankInfoActivity.class)));
    }
}