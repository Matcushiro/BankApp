package com.bankapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bankapp.R;
import com.bankapp.managers.DataManager;
import com.bankapp.models.Bank;

public class BankNameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_name);

        EditText etBankName = findViewById(R.id.etBankName);
        Button   btnConfirm = findViewById(R.id.btnConfirmBankName);

        btnConfirm.setOnClickListener(v -> {
            String name = etBankName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Введите название банка", Toast.LENGTH_SHORT).show();
                return;
            }
            Bank bank = new Bank(name);
            DataManager.getInstance(this).setBank(bank);
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });
    }
}