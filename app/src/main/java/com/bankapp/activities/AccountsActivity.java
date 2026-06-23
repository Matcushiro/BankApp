package com.bankapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

        // Проверяем начисление процентов по накопительному счёту
        bankManager.checkAndApplyInterest(currentUser);

        // Снова обновляем после возможного начисления процентов
        authManager.refreshCurrentUser(this);
        currentUser = authManager.getCurrentUser();

        // Перерисовываем карточки счетов
        LinearLayout container = findViewById(R.id.accountsContainer);
        container.removeAllViews();

        setupAccountCard(container, Account.TYPE_DEBIT,   "💳 Дебетовый счёт");
        setupAccountCard(container, Account.TYPE_CREDIT,  "💰 Кредитный счёт");
        setupAccountCard(container, Account.TYPE_SAVINGS, "🏦 Накопительный счёт");
    }

    private void setupAccountCard(LinearLayout container, String type, String title) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.item_account, container, false);

        TextView  tvTitle        = cardView.findViewById(R.id.tvAccTitle);
        TextView  tvBalance      = cardView.findViewById(R.id.tvAccBalance);
        TextView  tvInfo         = cardView.findViewById(R.id.tvAccInfo);
        Button    btnOpen        = cardView.findViewById(R.id.btnAccOpen);
        LinearLayout layoutBtns = cardView.findViewById(R.id.layoutAccButtons);
        Button    btnDeposit     = cardView.findViewById(R.id.btnAccDeposit);
        Button    btnWithdraw    = cardView.findViewById(R.id.btnAccWithdraw);
        Button    btnHistory     = cardView.findViewById(R.id.btnAccHistory);

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
                            .append((int)(account.getInterestRate() * 100)).append("% годовых")
                            .append("\nОткрыт: ").append(DateUtils.formatDate(account.getCreatedDate()));
                    break;

                case Account.TYPE_SAVINGS:
                    info.append("Процентная ставка: ")
                            .append((int)(account.getInterestRate() * 100)).append("% в месяц")
                            .append("\nСледующее начисление: ")
                            .append(DateUtils.formatDate(account.getNextInterestDate()))
                            .append("\nОсталось дней: ")
                            .append(DateUtils.daysUntil(account.getNextInterestDate()))
                            .append("\nОткрыт: ").append(DateUtils.formatDate(account.getCreatedDate()));
                    break;
            }

            tvInfo.setText(info.toString());
            tvInfo.setVisibility(View.VISIBLE);

            // Назначаем обработчики кнопок
            btnDeposit.setOnClickListener(v ->
                    showAmountDialog("Пополнение счёта", type, true));

            btnWithdraw.setOnClickListener(v ->
                    showAmountDialog("Снятие средств", type, false));

            btnHistory.setOnClickListener(v ->
                    showHistory(account));
        }

        container.addView(cardView);
    }

    /**
     * Диалог ввода суммы для пополнения или снятия
     */
    private void showAmountDialog(String title, String accountType, boolean isDeposit) {
        // Создаём поле ввода суммы
        final EditText etAmount = new EditText(this);
        etAmount.setHint("Введите сумму");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setTextColor(getResources().getColor(R.color.text_primary, null));
        etAmount.setHintTextColor(getResources().getColor(R.color.text_hint, null));
        etAmount.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getResources().getColor(R.color.accent_gold, null)));

        int padding = (int)(16 * getResources().getDisplayMetrics().density);
        etAmount.setPadding(padding, padding, padding, padding);

        // Оборачиваем в LinearLayout для отступов
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int margin = (int)(20 * getResources().getDisplayMetrics().density);
        container.setPadding(margin, margin / 2, margin, 0);
        container.addView(etAmount);

        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(title)
                .setView(container)
                .setPositiveButton("Подтвердить", (dialog, which) -> {
                    String input = etAmount.getText().toString().trim();
                    if (input.isEmpty()) {
                        Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        double amount = Double.parseDouble(input);
                        if (amount <= 0) {
                            Toast.makeText(this, "Сумма должна быть больше нуля", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        BankManager.OperationResult result;
                        if (isDeposit) {
                            result = bankManager.deposit(currentUser, accountType, amount);
                        } else {
                            result = bankManager.withdraw(currentUser, accountType, amount);
                        }
                        handleOperationResult(result);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    /**
     * Диалог истории операций по счёту
     */
    private void showHistory(Account account) {
        List<Transaction> transactions = account.getTransactions();

        if (transactions == null || transactions.isEmpty()) {
            new AlertDialog.Builder(this, R.style.DarkDialog)
                    .setTitle("История операций")
                    .setMessage("История операций пуста.\nВыполните первую операцию!")
                    .setPositiveButton("Закрыть", null)
                    .show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        // Показываем от новых к старым
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction tx = transactions.get(i);
            String sign = tx.getType().equals(Transaction.TYPE_DEPOSIT)
                    || (tx.getType().equals(Transaction.TYPE_TRANSFER)
                    && tx.getToAccountId() != null
                    && tx.getToAccountId().equals(account.getId()))
                    || tx.getType().equals(Transaction.TYPE_INTEREST)
                    ? "+" : "-";

            sb.append(DateUtils.formatDateTime(tx.getDate())).append("\n")
                    .append(tx.getTypeDisplayName()).append(": ")
                    .append(sign).append(String.format("%.2f ₽", tx.getAmount())).append("\n")
                    .append(tx.getDescription()).append("\n")
                    .append("─────────────────\n");
        }

        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle("История: " + account.getTypeDisplayName())
                .setMessage(sb.toString())
                .setPositiveButton("Закрыть", null)
                .show();
    }

    /**
     * Обработка результата банковской операции
     */
    private void handleOperationResult(BankManager.OperationResult result) {
        switch (result) {
            case SUCCESS:
                Toast.makeText(this, "✅ Операция выполнена успешно!", Toast.LENGTH_SHORT).show();
                refresh(); // Обновляем отображение после операции
                break;
            case INSUFFICIENT_FUNDS:
                Toast.makeText(this, "❌ Недостаточно средств на счёте", Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "❌ Произошла ошибка", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}