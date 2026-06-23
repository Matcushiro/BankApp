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
import com.bankapp.utils.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etUsername  = findViewById(R.id.etRegUsername);
        EditText etPassword  = findViewById(R.id.etRegPassword);
        EditText etFullName  = findViewById(R.id.etRegFullName);
        EditText etEmail     = findViewById(R.id.etRegEmail);
        EditText etPhone     = findViewById(R.id.etRegPhone);
        Button   btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin     = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();
            String phone    = etPhone.getText().toString().trim();

            if (!ValidationUtils.isValidUsername(username)) {
                Toast.makeText(this, "Логин: мин. 3 символа, только буквы/цифры/_", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ValidationUtils.isValidPassword(password)) {
                Toast.makeText(this, "Пароль: минимум 6 символов", Toast.LENGTH_SHORT).show();
                return;
            }
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Введите ФИО", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthManager.RegisterResult result =
                    AuthManager.getInstance(this).register(username, password, fullName, email, phone);

            switch (result) {
                case SUCCESS:
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;
                case USERNAME_TAKEN:
                    Toast.makeText(this, "Логин уже занят", Toast.LENGTH_SHORT).show();
                    break;
                case INVALID_DATA:
                    Toast.makeText(this, "Некорректные данные", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        tvLogin.setOnClickListener(v -> finish());
    }
}