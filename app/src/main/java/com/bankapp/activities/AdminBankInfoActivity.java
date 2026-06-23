package com.bankapp.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bankapp.R;
import com.bankapp.managers.DataManager;
import com.bankapp.models.Account;
import com.bankapp.models.Bank;
import com.bankapp.models.User;
import com.bankapp.utils.DateUtils;

import java.util.List;

public class AdminBankInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bank_info);

        DataManager dm = DataManager.getInstance(this);
        Bank bank = dm.getBank();
        List<User> users = dm.getAllUsers();

        ImageButton btnBack = findViewById(R.id.btnBankInfoBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvBankName    = findViewById(R.id.tvBankInfoName);
        TextView tvFounded     = findViewById(R.id.tvBankInfoFounded);
        TextView tvUsersCount  = findViewById(R.id.tvBankInfoUsers);
        TextView tvBalance     = findViewById(R.id.tvBankInfoBalance);
        TextView tvAccCount    = findViewById(R.id.tvBankInfoAccounts);
        TextView tvUserDetails = findViewById(R.id.tvBankInfoDetails);

        if (bank != null) {
            tvBankName.setText(bank.getName());
            tvFounded.setText("Дата основания: " + DateUtils.formatDate(bank.getFoundedDate()));
        }

        long realUsers = users.stream().filter(u -> !u.isSuperAdmin()).count();
        tvUsersCount.setText("Пользователей: " + realUsers);
        tvBalance.setText(String.format("Всего в банке: %.2f ₽", dm.getTotalBankBalance()));
        tvAccCount.setText("Счетов всего: " + dm.getTotalAccountsCount());

        StringBuilder details = new StringBuilder();
        for (User u : users) {
            details.append("─────────────────\n")
                    .append(u.getFullName()).append(" (@").append(u.getUsername()).append(")\n")
                    .append("Роль: ").append(u.isSuperAdmin() ? "Гл. Админ"
                            : u.isAdmin() ? "Админ" : "Пользователь");
            if (u.isBanned()) details.append(" [ЗАБАНЕН]");
            details.append("\n");
            for (Account a : u.getAccounts()) {
                details.append("  • ").append(a.getTypeDisplayName())
                        .append(": ").append(String.format("%.2f ₽\n", a.getBalance()));
            }
            details.append("\n");
        }
        tvUserDetails.setText(details.toString());
    }
}