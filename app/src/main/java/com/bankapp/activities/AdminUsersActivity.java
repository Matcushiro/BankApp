package com.bankapp.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bankapp.R;
import com.bankapp.managers.AuthManager;
import com.bankapp.managers.BankManager;
import com.bankapp.managers.DataManager;
import com.bankapp.models.Account;
import com.bankapp.models.User;
import com.bankapp.utils.Constants;
import com.bankapp.utils.DateUtils;

import java.util.List;
import java.util.UUID;

public class AdminUsersActivity extends AppCompatActivity {

    private DataManager dataManager;
    private AuthManager authManager;
    private User currentAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        dataManager  = DataManager.getInstance(this);
        authManager  = AuthManager.getInstance(this);
        authManager.refreshCurrentUser(this);
        currentAdmin = authManager.getCurrentUser();

        ImageButton btnBack       = findViewById(R.id.btnAdminUsersBack);
        Button      btnCreateUser = findViewById(R.id.btnCreateUser);

        btnBack.setOnClickListener(v -> finish());
        btnCreateUser.setOnClickListener(v -> showCreateUserDialog());

        loadUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        authManager.refreshCurrentUser(this);
        currentAdmin = authManager.getCurrentUser();
        loadUsers();
    }

    private void loadUsers() {
        LinearLayout container = findViewById(R.id.usersContainer);
        container.removeAllViews();

        List<User> users = dataManager.getAllUsers();
        for (User user : users) {
            if (user.getId().equals(currentAdmin.getId())) continue;

            View item = LayoutInflater.from(this)
                    .inflate(R.layout.item_user_admin, container, false);

            TextView tvName  = item.findViewById(R.id.tvUserItemName);
            TextView tvInfo  = item.findViewById(R.id.tvUserItemInfo);
            Button btnBan    = item.findViewById(R.id.btnUserBan);
            Button btnAdmin  = item.findViewById(R.id.btnUserAdmin);
            Button btnDelete = item.findViewById(R.id.btnUserDelete);

            tvName.setText(user.getFullName() + " (@" + user.getUsername() + ")");

            StringBuilder info = new StringBuilder();
            info.append("Роль: ").append(user.isSuperAdmin() ? "👑 Гл. Админ"
                            : user.isAdmin() ? "🛡 Админ" : "👤 Польз.")
                    .append(" | Рег.: ").append(DateUtils.formatDate(user.getRegistrationDate()))
                    .append("\nСчетов: ").append(user.getAccounts().size())
                    .append(" | Баланс: ").append(String.format("%.2f ₽", user.getTotalBalance()));
            if (user.isBanned()) info.append(" | 🔒 ЗАБАНЕН");
            tvInfo.setText(info.toString());

            // Super admin's users can only be managed by superadmin
            boolean canManage = currentAdmin.isSuperAdmin()
                    || (user.getCreatedByAdminId() != null
                    && user.getCreatedByAdminId().equals(currentAdmin.getId()))
                    || (!user.isAdmin() && !user.isSuperAdmin()
                    && (user.getCreatedByAdminId() == null || user.getCreatedByAdminId().isEmpty()));

            if (!canManage) {
                btnBan.setEnabled(false);
                btnAdmin.setEnabled(false);
                btnDelete.setEnabled(false);
            }

            // Cannot demote/promote another super admin
            if (user.isSuperAdmin() && !currentAdmin.isSuperAdmin()) {
                btnAdmin.setEnabled(false);
                btnBan.setEnabled(false);
                btnDelete.setEnabled(false);
            }

            btnBan.setText(user.isBanned() ? "Разбанить" : "Забанить");
            btnBan.setOnClickListener(v -> {
                user.setBanned(!user.isBanned());
                dataManager.updateUser(user);
                loadUsers();
                Toast.makeText(this,
                        user.isBanned() ? "Пользователь забанен" : "Бан снят",
                        Toast.LENGTH_SHORT).show();
            });

            btnAdmin.setText(user.isAdmin() ? "Снять права" : "Сделать админом");
            btnAdmin.setOnClickListener(v -> {
                if (!currentAdmin.isSuperAdmin() && user.isAdmin()) {
                    Toast.makeText(this, "Нет прав снять администратора", Toast.LENGTH_SHORT).show();
                    return;
                }
                user.setAdmin(!user.isAdmin());
                if (user.isAdmin()) user.setCreatedByAdminId(currentAdmin.getId());
                dataManager.updateUser(user);
                loadUsers();
            });

            btnDelete.setOnClickListener(v ->
                    new AlertDialog.Builder(this, R.style.DarkDialog)
                            .setTitle("Удалить пользователя?")
                            .setMessage("Это действие необратимо. Удалить " + user.getFullName() + "?")
                            .setPositiveButton("Удалить", (d, w) -> {
                                dataManager.deleteUser(user.getId());
                                loadUsers();
                                Toast.makeText(this, "Пользователь удалён", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Отмена", null)
                            .show());

            // Manage accounts button
            Button btnAccounts = item.findViewById(R.id.btnUserAccounts);
            btnAccounts.setOnClickListener(v -> showManageAccountsDialog(user));

            container.addView(item);
        }
    }

    private void showManageAccountsDialog(User user) {
        String[] types = {"Дебетовый", "Кредитный", "Накопительный"};
        String[] typeKeys = {Account.TYPE_DEBIT, Account.TYPE_CREDIT, Account.TYPE_SAVINGS};

        StringBuilder msg = new StringBuilder("Счета пользователя:\n");
        for (Account a : user.getAccounts())
            msg.append("• ").append(a.getTypeDisplayName())
                    .append(": ").append(String.format("%.2f ₽\n", a.getBalance()));
        if (user.getAccounts().isEmpty()) msg.append("(нет счетов)\n");

        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle("Счета: " + user.getUsername())
                .setMessage(msg.toString())
                .setPositiveButton("Открыть счёт", (d, w) -> {
                    new AlertDialog.Builder(this, R.style.DarkDialog)
                            .setTitle("Открыть счёт")
                            .setItems(types, (d2, which) -> {
                                boolean ok = BankManager.getInstance(this)
                                        .openAccount(user, typeKeys[which]);
                                Toast.makeText(this,
                                        ok ? "Счёт открыт" : "Счёт уже существует",
                                        Toast.LENGTH_SHORT).show();
                                loadUsers();
                            })
                            .show();
                })
                .setNegativeButton("Закрыть", null)
                .show();
    }

    private void showCreateUserDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_create_user, null);
        EditText etUsername = view.findViewById(R.id.etCreateUsername);
        EditText etPassword = view.findViewById(R.id.etCreatePassword);
        EditText etFullName = view.findViewById(R.id.etCreateFullName);
        EditText etEmail    = view.findViewById(R.id.etCreateEmail);
        EditText etPhone    = view.findViewById(R.id.etCreatePhone);

        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle("Создать пользователя")
                .setView(view)
                .setPositiveButton("Создать", (d, w) -> {
                    String username = etUsername.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    String fullName = etFullName.getText().toString().trim();
                    String email    = etEmail.getText().toString().trim();
                    String phone    = etPhone.getText().toString().trim();

                    if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                        Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (dataManager.getUserByUsername(username) != null) {
                        Toast.makeText(this, "Логин уже занят", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String id = UUID.randomUUID().toString();
                    User newUser = new User(id, username,
                            AuthManager.hashPassword(password),
                            fullName, email, phone, false, false);
                    newUser.setCreatedByAdminId(currentAdmin.getId());
                    dataManager.addUser(newUser);
                    loadUsers();
                    Toast.makeText(this, "Пользователь создан", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}