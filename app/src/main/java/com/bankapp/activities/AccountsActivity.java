package com.bankapp.activities;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bankapp.R;
import com.bankapp.managers.AuthManager;
import com.bankapp.managers.BankManager;
import com.bankapp.models.Account;
import com.bankapp.models.Transaction;
import com.bankapp.models.User;
import com.bankapp.utils.DateUtils;

import java.util.List;

public class AccountsActivity extends AppCompatActivity {

    private AuthManager authManager;
    private BankManager bankManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        authManager = AuthManager.getInstance(this);
        bankManager = BankManager.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btnAccountsBack);
        btnBack.setOnClickListener(v -> finish());

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        // Обновляем данные пользователя из хранилища
        authManager.refreshCurrentUser(this);
        currentUser = authManager.getCurrentUser();

        // Применяем начисление процентов по накопительному счёту
        bankManager.checkAndApplyInterest(currentUser);

        // Снова обновляем после возможного начисления процентов
        authManager.refreshCurrentUser(this);
        currentUser = authManager.getCurrentUser();

        // Перерисовываем карточки счетов
        LinearLayout container = findViewById(R.id.accountsContainer);
        container.removeAllViews();

        setupAccountCard(container, Account.TYPE_DEBIT, "💳 Дебетовый счёт");
        setupAccountCard(container, Account.TYPE_CREDIT, "💰 Кредитный счёт");
        setupAccountCard(container, Account.TYPE_SAVINGS, "🏦 Накопительный счёт");
    }

    private void setupAccountCard(LinearLayout container, String type, String title) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.item_account, container, false);

        TextView tvTitle = cardView.findViewById(R.id.tvAccTitle);
        TextView tvBalance = cardView.findViewById(R.id.tvAccBalance);
        TextView tvInfo = cardView.findViewById(R.id.tvAccInfo);
        Button btnOpen = cardView.findViewById(R.id.btnAccOpen);
        LinearLayout layoutBtns = cardView.findViewById(R.id.layoutAccButtons);
        Button btnDeposit = cardView.findViewById(R.id.btnAccDeposit);
        Button btnWithdraw = cardView.findViewById(R.id.btnAccWithdraw);
        Button btnHistory = cardView.findViewById(R.id.btnAccHistory);

        tvTitle.setText(title);

        Account account = currentUser.getAccountByType(type);

        if (account == null) {
            // Счёт не открыт — показываем кнопку открытия
            tvBalance.setText("Счёт не открыт");
            tvInfo.setVisibility(View.GONE);
            btnOpen.setVisibility(View.VISIBLE);
            layoutBtns.setVisibility(View.GONE);

            btnOpen.setOnClickListener(v -> {
                boolean ok = bankManager.openAccount(currentUser, type);
                if (ok) {
                    Toast.makeText(this, "Счёт успешно открыт!", Toast.LENGTH_SHORT).show();
                    refresh();
                } else {
                    Toast.makeText(this, "Ошибка: счёт уже существует", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Счёт открыт — показываем баланс и кнопки операций
            btnOpen.setVisibility(View.GONE);
            layoutBtns.setVisibility(View.VISIBLE);
            tvBalance.setText(String.format("%.2f ₽", account.getBalance()));

            // Формируем блок дополнительной информации
            StringBuilder info = new StringBuilder();
            switch (type) {
                case Account.TYPE_DEBIT:
                    info.append("Открыт: ").append(DateUtils.formatDate(account.getCreatedDate()));
                    break;
                case Account.TYPE_CREDIT:
                    info.append("Кредитный лимит: ")
                            .append(String.format("%.0f ₽", account.getCreditLimit()))
                            .append("\nДоступно к снятию: ")
                            .append(String.format("%.2f ₽", account.getAvailableCredit()))
                            .append("\nПроцентная ставка: ")
                            .append(String.format("%.1f%%", account.getInterestRate()));
                    break;
                case Account.TYPE_SAVINGS:
                    info.append("Процентная ставка: ")
                            .append(String.format("%.1f%% в год", account.getInterestRate()))
                            .append("\nОткрыт: ")
                            .append(DateUtils.formatDate(account.getCreatedDate()));
                    break;
            }

            if (info.length() > 0) {
                tvInfo.setText(info.toString());
                tvInfo.setVisibility(View.VISIBLE);
            }

            // ИЗМЕНЕНИЕ: Открываем диалог пополнения с правильными цветами
            btnDeposit.setOnClickListener(v -> showDepositWithdrawDialog(account, true));

            // ИЗМЕНЕНИЕ: Открываем диалог снятия с правильными цветами
            btnWithdraw.setOnClickListener(v -> showDepositWithdrawDialog(account, false));

            // ИЗМЕНЕНИЕ: Открываем историю с правильными цветами
            btnHistory.setOnClickListener(v -> showHistoryDialog(account));
        }

        container.addView(cardView);
    }

    /**
     * ИЗМЕНЕНИЕ: Диалог пополнения/снятия с явным указанием цветов текста.
     * Проблема: стандартный AlertDialog наследует тёмный фон темы,
     * но текст EditText мог быть тёмным (невидимым на тёмном фоне).
     */
    private void showDepositWithdrawDialog(Account account, boolean isDeposit) {
        String opTitle = isDeposit ? "💵 Пополнение счёта" : "📤 Снятие средств";

        // Создаём EditText программно с явными цветами
        EditText etAmount = new EditText(this);
        etAmount.setHint("Введите сумму");
        etAmount.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER |
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        // ИСПРАВЛЕНИЕ ЦВЕТА: белый текст и подсказка на тёмном фоне
        etAmount.setTextColor(Color.WHITE);
        etAmount.setHintTextColor(Color.parseColor("#B0BEC5"));
        etAmount.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700"))
        );

        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        etAmount.setPadding(pad, pad / 2, pad, pad / 2);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pad * 2, pad, pad * 2, 0);
        container.addView(etAmount);

        new AlertDialog.Builder(this, R.style.DarkAlertDialog)
                .setTitle(opTitle)
                .setView(container)
                .setPositiveButton(isDeposit ? "Пополнить" : "Снять", (d, w) -> {
                    String input = etAmount.getText().toString().trim();
                    if (input.isEmpty()) {
                        Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;
                    try {
                        amount = Double.parseDouble(input);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Неверный формат суммы", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (amount <= 0) {
                        Toast.makeText(this, "Сумма должна быть больше нуля", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ИСПРАВЛЕНИЕ: Используем OperationResult вместо boolean
                    BankManager.OperationResult result;
                    if (isDeposit) {
                        result = bankManager.deposit(currentUser, account.getType(), amount);
                    } else {
                        result = bankManager.withdraw(currentUser, account.getType(), amount);
                    }

                    // ИСПРАВЛЕНИЕ: Обрабатываем все возможные результаты
                    switch (result) {
                        case SUCCESS:
                            Toast.makeText(this,
                                    isDeposit ? "✅ Счёт пополнен!" : "✅ Средства сняты!",
                                    Toast.LENGTH_SHORT).show();
                            refresh();
                            break;
                        case INSUFFICIENT_FUNDS:
                            Toast.makeText(this, "❌ Недостаточно средств", Toast.LENGTH_SHORT).show();
                            break;
                        case INVALID_AMOUNT:
                            Toast.makeText(this, "❌ Некорректная сумма", Toast.LENGTH_SHORT).show();
                            break;
                        case ACCOUNT_NOT_FOUND:
                            Toast.makeText(this, "❌ Счёт не найден", Toast.LENGTH_SHORT).show();
                            break;
                        case ACCOUNT_INACTIVE:
                            Toast.makeText(this, "❌ Счёт неактивен", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(this, "❌ Ошибка операции", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showHistoryDialog(Account account) {
        List<Transaction> transactions = account.getTransactions();

        // Создаём ScrollView с LinearLayout внутри
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, pad / 2);

        if (transactions == null || transactions.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("История операций пуста");
            // ИСПРАВЛЕНИЕ ЦВЕТА: белый текст
            tvEmpty.setTextColor(Color.parseColor("#B0BEC5"));
            tvEmpty.setTextSize(14);
            tvEmpty.setPadding(pad, pad, pad, pad);
            layout.addView(tvEmpty);
        } else {
            // Показываем последние 20 операций (от новых к старым)
            List<Transaction> txList = transactions;
            int start = Math.max(0, txList.size() - 20);

            for (int i = txList.size() - 1; i >= start; i--) {
                Transaction tx = txList.get(i);

                // Внешний контейнер для одной операции
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(0, pad / 2, 0, pad / 2);

                // Разделитель между операциями (кроме первой)
                if (i < txList.size() - 1) {
                    View divider = new View(this);
                    divider.setBackgroundColor(Color.parseColor("#26323800"));
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    divider.setLayoutParams(dividerParams);
                    layout.addView(divider);
                }

                // Тип операции + сумма
                LinearLayout topRow = new LinearLayout(this);
                topRow.setOrientation(LinearLayout.HORIZONTAL);

                TextView tvType = new TextView(this);
                tvType.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                tvType.setText(getTransactionTypeLabel(tx.getType()));
                // ИСПРАВЛЕНИЕ ЦВЕТА: явный белый цвет
                tvType.setTextColor(Color.WHITE);
                tvType.setTextSize(14);
                tvType.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView tvAmount = new TextView(this);
                double amt = tx.getAmount();
                // ИСПРАВЛЕНИЕ ЦВЕТА: зелёный для пополнения, красный для снятия
                if (isPositiveTransaction(tx.getType())) {
                    tvAmount.setTextColor(Color.parseColor("#2ECC71")); // зелёный
                    tvAmount.setText(String.format("+%.2f ₽", amt));
                } else {
                    tvAmount.setTextColor(Color.parseColor("#E74C3C")); // красный
                    tvAmount.setText(String.format("-%.2f ₽", amt));
                }
                tvAmount.setTextSize(14);
                tvAmount.setTypeface(null, android.graphics.Typeface.BOLD);

                topRow.addView(tvType);
                topRow.addView(tvAmount);
                row.addView(topRow);

                // Описание операции
                if (tx.getDescription() != null && !tx.getDescription().isEmpty()) {
                    TextView tvDesc = new TextView(this);
                    tvDesc.setText(tx.getDescription());
                    // ИСПРАВЛЕНИЕ ЦВЕТА: светло-серый для описания
                    tvDesc.setTextColor(Color.parseColor("#B0BEC5"));
                    tvDesc.setTextSize(12);
                    row.addView(tvDesc);
                }

                // Дата
                TextView tvDate = new TextView(this);
                tvDate.setText(DateUtils.formatDate(tx.getDate()));
                // ИСПРАВЛЕНИЕ ЦВЕТА: серый для даты
                tvDate.setTextColor(Color.parseColor("#607D8B"));
                tvDate.setTextSize(11);
                row.addView(tvDate);

                layout.addView(row);
            }
        }

        scrollView.addView(layout);

        new AlertDialog.Builder(this, R.style.DarkAlertDialog)
                .setTitle("📋 История операций")
                .setView(scrollView)
                .setPositiveButton("Закрыть", null)
                .show();
    }

    private String getTransactionTypeLabel(String type) {
        if (type == null) return "Операция";
        switch (type) {
            case Transaction.TYPE_DEPOSIT:
                return "📥 Пополнение";
            case Transaction.TYPE_WITHDRAW:
                return "📤 Снятие";
            case Transaction.TYPE_TRANSFER:
                return "↔️ Перевод";
            case Transaction.TYPE_INTEREST:
                return "📈 Проценты";
            default:
                return type;
        }
    }

    private boolean isPositiveTransaction(String type) {
        return Transaction.TYPE_DEPOSIT.equals(type) || Transaction.TYPE_INTEREST.equals(type);
    }
}
