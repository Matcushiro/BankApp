package com.bankapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bankapp.R;
import com.bankapp.managers.AuthManager;
import com.bankapp.managers.DataManager;
import com.bankapp.models.Bank;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Bank bank = DataManager.getInstance(this).getBank();
        TextView tvBankName = findViewById(R.id.tvAuthBankName);
        if (bank != null) tvBankName.setText(bank.getName());

        EditText etLogin    = findViewById(R.id.etAuthLogin);
        EditText etPassword = findViewById(R.id.etAuthPassword);
        Button   btnLogin   = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> {
            String login = etLogin.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();
            if (login.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }
            AuthManager.LoginResult result = AuthManager.getInstance(this).login(login, pass);
            switch (result) {
                case SUCCESS:
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;
                case BANNED:
                    Toast.makeText(this, "Аккаунт заблокирован", Toast.LENGTH_LONG).show();
                    break;
                case INVALID_CREDENTIALS:
                    Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                    break;
                case USER_NOT_FOUND:
                    Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}