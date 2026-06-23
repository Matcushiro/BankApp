package com.bankapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bankapp.R;
import com.bankapp.managers.AuthManager;
import com.bankapp.managers.BankManager;
import com.bankapp.models.Account;
import com.bankapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class TransferActivity extends AppCompatActivity {

    private AuthManager authManager;
    private BankManager bankManager;
    private User currentUser;

    private String selectedFromType = Account.TYPE_DEBIT;
    private String selectedToType   = Account.TYPE_DEBIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        authManager = AuthManager.getInstance(this);
        bankManager = BankManager.getInstance(this);
        authManager.refreshCurrentUser(this);
        currentUser = authManager.getCurrentUser();

        ImageButton btnBack = findViewById(R.id.btnTransferBack);
        btnBack.setOnClickListener(v -> finish());

        setupSpinners();

        Button btnTransfer = findViewById(R.id.btnDoTransfer);
        btnTransfer.setOnClickListener(v -> performTransfer());
    }

    private void setupSpinners() {
        List<String> ownTypes = new ArrayList<>();
        for (Account a : currentUser.getAccounts()) ownTypes.add(a.getTypeDisplayName());

        String[] fromItems = ownTypes.toArray(new String[0]);
        List<String> fromTypeKeys = new ArrayList<>();
        for (Account a : currentUser.getAccounts()) fromTypeKeys.add(a.getType());

        Spinner spFrom = findViewById(R.id.spinnerFromAccount);
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fromItems);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrom.setAdapter(fromAdapter);
        spFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedFromType = fromTypeKeys.get(pos);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        String[] toTypes = { "Дебетовый", "Кредитный", "Накопительный" };
        String[] toTypeKeys = { Account.TYPE_DEBIT, Account.TYPE_CREDIT, Account.TYPE_SAVINGS };

        Spinner spTo = findViewById(R.id.spinnerToAccount);
        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, toTypes);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTo.setAdapter(toAdapter);
        spTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedToType = toTypeKeys[pos];
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void performTransfer() {
        EditText etUsername = findViewById(R.id.etTransferUsername);
        EditText etAmount   = findViewById(R.id.etTransferAmount);

        String toUsername = etUsername.getText().toString().trim();
        String amountStr  = etAmount.getText().toString().trim();

        if (toUsername.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try { amount = Double.parseDouble(amountStr); }
        catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show();
            return;
        }

        BankManager.OperationResult result =
                bankManager.transfer(currentUser, selectedFromType, toUsername, selectedToType, amount);

        switch (result) {
            case SUCCESS:
                Toast.makeText(this, "Перевод выполнен!", Toast.LENGTH_SHORT).show();
                etAmount.setText("");
                etUsername.setText("");
                authManager.refreshCurrentUser(this);
                currentUser = authManager.getCurrentUser();
                break;
            case INSUFFICIENT_FUNDS:
                Toast.makeText(this, "Недостаточно средств", Toast.LENGTH_SHORT).show();
                break;
            case ACCOUNT_NOT_FOUND:
                Toast.makeText(this, "Пользователь или счёт не найден", Toast.LENGTH_SHORT).show();
                break;
            case SAME_ACCOUNT:
                Toast.makeText(this, "Нельзя перевести самому себе на тот же счёт", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "Ошибка перевода", Toast.LENGTH_SHORT).show();
        }
    }
}