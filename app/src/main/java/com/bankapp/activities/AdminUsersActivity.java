package com.bankapp.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bankapp.R;
import com.bankapp.managers.AuthManager;
import com.bankapp.managers.BankManager;
import com.bankapp.managers.DataManager;
import com.bankapp.models.Account;
import com.bankapp.models.User;
import com.bankapp.utils.Constants;
import com.bankapp.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminUsersActivity extends AppCompatActivity {

    private DataManager dataManager;
    private AuthManager authManager;
    private User currentAdmin;

    // ИЗМЕНЕНИЕ: UI элементы для Spinner-подхода
    private Spinner spinnerUsers;
    private CardView cardUserInfo;
    private TextView tvNoUserSelected;

    private TextView tvUserDetailName;
    private TextView tvUserDetailUsername;
    private TextView tvUserDetailEmail;
    private TextView tvUserDetailPhone;
    private TextView tvUserDetailRole;
    private TextView tvUserDetailRegDate;
    private TextView tvUserDetailAccounts;
    private TextView tvUserDetailBalance;
    private TextView tvUserDetailBanned;

    private Button btnUserBan;
    private Button btnUserAdmin;
    private Button btnUserAccounts;
    private Button btnUserDelete;

    private List<User> userList = new ArrayList<>();
    private User selectedUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        dataManager = DataManager.getInstance(this);
        authManager = AuthManager.getInstance(this);
        authManager.refreshCurrentUser(this);
        currentAdmin = authManager.getCurrentUser();

        ImageButton btnBack = findViewById(R.id.btnAdminUsersBack);
        Button btnCreateUser = findViewById(R.id.btnCreateUser);

        // ИЗМЕНЕНИЕ: Spinner и блок информации
        spinnerUsers      = findViewById(R.id.spinnerUsers);
        cardUserInfo      = findViewById(R.id.cardUserInfo);
        tvNoUserSelected  = findViewById(R.id.tvNoUserSelected);

        tvUserDetailName     = findViewById(R.id.tvUserDetailName);
        tvUserDetailUsername = findViewById(R.id.tvUserDetailUsername);
        tvUserDetailEmail    = findViewById(R.id.tvUserDetailEmail);
        tvUserDetailPhone    = findViewById(R.id.tvUserDetailPhone);
        tvUserDetailRole     = findViewById(R.id.tvUserDetailRole);
        tvUserDetailRegDate  = findViewById(R.id.tvUserDetailRegDate);
        tvUserDetailAccounts = findViewById(R.id.tvUserDetailAccounts);
        tvUserDetailBalance  = findViewById(R.id.tvUserDetailBalance);
        tvUserDetailBanned   = findViewById(R.id.tvUserDetailBanned);

        btnUserBan      = findViewById(R.id.btnUserBan);
        btnUserAdmin    = findViewById(R.id.btnUserAdmin);
        btnUserAccounts = findViewById(R.id.btnUserAccounts);
        btnUserDelete   = findViewById(R.id.btnUserDelete);

        btnBack.setOnClickListener(v -> finish());
        btnCreateUser.setOnClickListener(v -> showCreateUserDialog());

        loadUsersToSpinner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        authManager.refreshCurrentUser(this);
        currentAdmin = authManager.getCurrentUser();
        loadUsersToSpinner();
    }

    /**
     * ИЗМЕНЕНИЕ: Заполняем Spinner списком пользователей.
     * При выборе элемента — показываем информацию о нём.
     */
    private void loadUsersToSpinner() {
        List<User> allUsers = dataManager.getAllUsers();
        userList.clear();

        for (User u : allUsers) {
            // Скрываем текущего администратора из списка
            if (u.getId().equals(currentAdmin.getId())) continue;
            userList.add(u);
        }

        // Создаём список строк для Spinner
        List<String> names = new ArrayList<>();
        names.add("— Выберите пользователя —");
        for (User u : userList) {
            names.add(u.getFullName() + " (@" + u.getUsername() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                names
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUsers.setAdapter(adapter);

        // Сохраняем позицию выбранного пользователя после обновления
        int selectedPosition = 0;
        if (selectedUser != null) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getId().equals(selectedUser.getId())) {
                    selectedPosition = i + 1; // +1 из-за заголовка "Выберите"
                    break;
                }
            }
        }

        spinnerUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Выбран заголовок — скрываем карточку
                    selectedUser = null;
                    cardUserInfo.setVisibility(View.GONE);
                    tvNoUserSelected.setVisibility(View.VISIBLE);
                } else {
                    // Выбран конкретный пользователь
                    selectedUser = userList.get(position - 1);
                    showUserDetails(selectedUser);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUser = null;
                cardUserInfo.setVisibility(View.GONE);
                tvNoUserSelected.setVisibility(View.VISIBLE);
            }
        });

        // Восстанавливаем позицию
        if (selectedPosition > 0) {
            spinnerUsers.setSelection(selectedPosition);
        }
    }

    /**
     * ИЗМЕНЕНИЕ: Показываем подробную информацию о выбранном пользователе в карточке.
     */
    private void showUserDetails(User user) {
        cardUserInfo.setVisibility(View.VISIBLE);
        tvNoUserSelected.setVisibility(View.GONE);

        tvUserDetailName.setText(user.getFullName());
        tvUserDetailUsername.setText("@" + user.getUsername());
        tvUserDetailEmail.setText(user.getEmail() != null && !user.getEmail().isEmpty()
                ? user.getEmail() : "—");
        tvUserDetailPhone.setText(user.getPhone() != null && !user.getPhone().isEmpty()
                ? user.getPhone() : "—");

        // Роль
        String roleText;
        if (user.isSuperAdmin()) roleText = "👑 Гл. Администратор";
        else if (user.isAdmin()) roleText = "🛡 Администратор";
        else roleText = "👤 Пользователь";
        tvUserDetailRole.setText(roleText);

        tvUserDetailRegDate.setText(DateUtils.formatDate(user.getRegistrationDate()));
        tvUserDetailAccounts.setText(String.valueOf(user.getAccounts().size()));
        tvUserDetailBalance.setText(String.format("%.2f ₽", user.getTotalBalance()));

        // Статус бана
        if (user.isBanned()) {
            tvUserDetailBanned.setVisibility(View.VISIBLE);
        } else {
            tvUserDetailBanned.setVisibility(View.GONE);
        }

        // Права на управление
        boolean canManage = currentAdmin.isSuperAdmin()
                || (user.getCreatedByAdminId() != null
                && user.getCreatedByAdminId().equals(currentAdmin.getId()))
                || (!user.isAdmin() && !user.isSuperAdmin()
                && (user.getCreatedByAdminId() == null || user.getCreatedByAdminId().isEmpty()));

        boolean isProtected = user.isSuperAdmin() && !currentAdmin.isSuperAdmin();

        btnUserBan.setEnabled(canManage && !isProtected);
        btnUserAdmin.setEnabled(canManage && !isProtected);
        btnUserDelete.setEnabled(canManage && !isProtected);

        // Текст кнопки бан
        btnUserBan.setText(user.isBanned() ? "Разбанить" : "Забанить");
        btnUserBan.setOnClickListener(v -> {
            user.setBanned(!user.isBanned());
            dataManager.updateUser(user);
            Toast.makeText(this,
                    user.isBanned() ? "Пользователь забанен" : "Бан снят",
                    Toast.LENGTH_SHORT).show();
            loadUsersToSpinner();
        });

        // Текст кнопки назначения админа
        btnUserAdmin.setText(user.isAdmin() ? "Снять права" : "Сделать админом");
        btnUserAdmin.setOnClickListener(v -> {
            if (!currentAdmin.isSuperAdmin() && user.isAdmin()) {
                Toast.makeText(this, "Нет прав снять администратора", Toast.LENGTH_SHORT).show();
                return;
            }
            user.setAdmin(!user.isAdmin());
            if (user.isAdmin()) user.setCreatedByAdminId(currentAdmin.getId());
            dataManager.updateUser(user);
            loadUsersToSpinner();
        });

        // Счета
        btnUserAccounts.setOnClickListener(v -> showUserAccountsDialog(user));

        // Удаление
        btnUserDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Удалить пользователя")
                    .setMessage("Вы уверены, что хотите удалить " + user.getFullName() + "?")
                    .setPositiveButton("Удалить", (d, w) -> {
                        dataManager.deleteUser(user.getId());
                        selectedUser = null;
                        loadUsersToSpinner();
                        Toast.makeText(this, "Пользователь удалён", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    private void showUserAccountsDialog(User user) {
        StringBuilder sb = new StringBuilder();
        if (user.getAccounts() == null || user.getAccounts().isEmpty()) {
            sb.append("У пользователя нет счетов.");
        } else {
            for (Account acc : user.getAccounts()) {
                sb.append("• ").append(acc.getType())
                        .append(": ").append(String.format("%.2f ₽", acc.getBalance()))
                        .append("\n");
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Счета: " + user.getFullName())
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showCreateUserDialog() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_create_user, null);

        EditText etUsername = view.findViewById(R.id.etCreateUsername);
        EditText etPassword = view.findViewById(R.id.etCreatePassword);
        EditText etFullName = view.findViewById(R.id.etCreateFullName);
        EditText etEmail    = view.findViewById(R.id.etCreateEmail);
        EditText etPhone    = view.findViewById(R.id.etCreatePhone);

        new AlertDialog.Builder(this)
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
                    User newUser = new User(
                            id, username, AuthManager.hashPassword(password),
                            fullName,
                            email.isEmpty() ? "" : email,
                            phone.isEmpty() ? "" : phone,
                            false, false
                    );
                    newUser.setCreatedByAdminId(currentAdmin.getId());
                    dataManager.addUser(newUser);
                    Toast.makeText(this, "Пользователь создан!", Toast.LENGTH_SHORT).show();
                    loadUsersToSpinner();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
