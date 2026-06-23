package com.bankapp.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bankapp.R;
import com.bankapp.managers.AuthManager;
import com.bankapp.managers.DataManager;
import com.bankapp.models.Account;
import com.bankapp.models.Bank;
import com.bankapp.models.User;
import com.bankapp.utils.DateUtils;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        AuthManager authManager = AuthManager.getInstance(this);
        authManager.refreshCurrentUser(this);
        User user = authManager.getCurrentUser();
        Bank bank = DataManager.getInstance(this).getBank();

        ImageButton btnBack = findViewById(R.id.btnProfileBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvBankName    = findViewById(R.id.tvProfBankName);
        TextView tvBankFounded = findViewById(R.id.tvProfBankFounded);
        TextView tvFullName    = findViewById(R.id.tvProfFullName);
        TextView tvUsername    = findViewById(R.id.tvProfUsername);
        TextView tvEmail       = findViewById(R.id.tvProfEmail);
        TextView tvPhone       = findViewById(R.id.tvProfPhone);
        TextView tvRegDate     = findViewById(R.id.tvProfRegDate);
        TextView tvRole        = findViewById(R.id.tvProfRole);
        TextView tvAccounts    = findViewById(R.id.tvProfAccounts);
        TextView tvTotalBal    = findViewById(R.id.tvProfTotalBalance);

        if (bank != null) {
            tvBankName.setText(bank.getName());
            tvBankFounded.setText("Дата основания: " + DateUtils.formatDate(bank.getFoundedDate()));
        }

        tvFullName.setText(user.getFullName());
        tvUsername.setText("@" + user.getUsername());
        tvEmail.setText(user.getEmail().isEmpty() ? "—" : user.getEmail());
        tvPhone.setText(user.getPhone().isEmpty() ? "—" : user.getPhone());
        tvRegDate.setText("Зарегистрирован: " + DateUtils.formatDate(user.getRegistrationDate()));
        tvRole.setText(user.isSuperAdmin() ? "👑 Главный администратор"
                : user.isAdmin()     ? "🛡 Администратор"
                  : "👤 Пользователь");

        StringBuilder accInfo = new StringBuilder();
        if (user.getAccounts().isEmpty()) {
            accInfo.append("Счета не открыты");
        } else {
            for (Account a : user.getAccounts()) {
                accInfo.append("• ").append(a.getTypeDisplayName())
                        .append(": ").append(String.format("%.2f ₽", a.getBalance()));
                if (a.getType().equals(Account.TYPE_SAVINGS) && a.getNextInterestDate() != null) {
                    accInfo.append("\n  Следующее начисление: ")
                            .append(DateUtils.formatDate(a.getNextInterestDate()))
                            .append(" (через ").append(DateUtils.daysUntil(a.getNextInterestDate()))
                            .append(" дн.)");
                }
                if (a.getType().equals(Account.TYPE_CREDIT)) {
                    accInfo.append("\n  Лимит: ").append(String.format("%.0f ₽", a.getCreditLimit()))
                            .append(" | Доступно: ").append(String.format("%.2f ₽", a.getAvailableCredit()));
                }
                accInfo.append("\n\n");
            }
        }
        tvAccounts.setText(accInfo.toString());
        tvTotalBal.setText(String.format("Итого: %.2f ₽", user.getTotalBalance()));
    }
}