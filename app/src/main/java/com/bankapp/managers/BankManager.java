package com.bankapp.managers;

import android.content.Context;

import com.bankapp.models.Account;
import com.bankapp.models.Transaction;
import com.bankapp.models.User;

import java.util.Date;
import java.util.UUID;

public class BankManager {
    private static BankManager instance;
    private final DataManager dataManager;

    private BankManager(Context context) {
        dataManager = DataManager.getInstance(context);
    }

    public static synchronized BankManager getInstance(Context context) {
        if (instance == null) instance = new BankManager(context);
        return instance;
    }

    public enum OperationResult {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        ACCOUNT_NOT_FOUND,
        INVALID_AMOUNT,
        SAME_ACCOUNT,
        ACCOUNT_INACTIVE
    }

    // ─────────────────────────────────────────────────────────
    // Открытие счёта
    // ─────────────────────────────────────────────────────────
    public boolean openAccount(User user, String type) {
        // Нельзя открыть два счёта одного типа
        if (user.hasAccountOfType(type)) return false;

        String id = UUID.randomUUID().toString();
        Account account = new Account(id, user.getId(), type);
        user.getAccounts().add(account);
        dataManager.updateUser(user);
        return true;
    }

    // ─────────────────────────────────────────────────────────
    // Пополнение счёта
    // ─────────────────────────────────────────────────────────
    public OperationResult deposit(User user, String accountType, double amount) {
        if (amount <= 0) return OperationResult.INVALID_AMOUNT;

        // Ищем свежую копию пользователя из хранилища
        User freshUser = dataManager.getUserById(user.getId());
        if (freshUser == null) return OperationResult.ACCOUNT_NOT_FOUND;

        Account account = freshUser.getAccountByType(accountType);
        if (account == null)     return OperationResult.ACCOUNT_NOT_FOUND;
        if (!account.isActive()) return OperationResult.ACCOUNT_INACTIVE;

        // Выполняем операцию
        account.setBalance(account.getBalance() + amount);

        // Создаём запись транзакции
        Transaction tx = new Transaction(
                UUID.randomUUID().toString(),
                Transaction.TYPE_DEPOSIT,
                amount,
                "Пополнение счёта",
                null,
                account.getId()
        );
        account.getTransactions().add(tx);

        // Сохраняем
        dataManager.updateUser(freshUser);

        // Синхронизируем переданный объект user с обновлёнными данными
        syncUser(user, freshUser);

        return OperationResult.SUCCESS;
    }

    // ─────────────────────────────────────────────────────────
    // Снятие средств
    // ─────────────────────────────────────────────────────────
    public OperationResult withdraw(User user, String accountType, double amount) {
        if (amount <= 0) return OperationResult.INVALID_AMOUNT;

        // Ищем свежую копию пользователя из хранилища
        User freshUser = dataManager.getUserById(user.getId());
        if (freshUser == null) return OperationResult.ACCOUNT_NOT_FOUND;

        Account account = freshUser.getAccountByType(accountType);
        if (account == null)     return OperationResult.ACCOUNT_NOT_FOUND;
        if (!account.isActive()) return OperationResult.ACCOUNT_INACTIVE;

        // Для кредитного счёта доступна сумма: баланс + лимит
        double available;
        if (account.getType().equals(Account.TYPE_CREDIT)) {
            available = account.getBalance() + account.getCreditLimit();
        } else {
            available = account.getBalance();
        }

        if (amount > available) return OperationResult.INSUFFICIENT_FUNDS;

        // Выполняем операцию
        account.setBalance(account.getBalance() - amount);

        // Создаём запись транзакции
        Transaction tx = new Transaction(
                UUID.randomUUID().toString(),
                Transaction.TYPE_WITHDRAW,
                amount,
                "Снятие средств",
                account.getId(),
                null
        );
        account.getTransactions().add(tx);

        // Сохраняем
        dataManager.updateUser(freshUser);

        // Синхронизируем переданный объект user с обновлёнными данными
        syncUser(user, freshUser);

        return OperationResult.SUCCESS;
    }

    // ─────────────────────────────────────────────────────────
    // Перевод между пользователями
    // ─────────────────────────────────────────────────────────
    public OperationResult transfer(User fromUser, String fromType,
                                    String toUsername, String toType,
                                    double amount) {
        if (amount <= 0) return OperationResult.INVALID_AMOUNT;

        // Свежие копии из хранилища
        User freshFromUser = dataManager.getUserById(fromUser.getId());
        if (freshFromUser == null) return OperationResult.ACCOUNT_NOT_FOUND;

        Account fromAcc = freshFromUser.getAccountByType(fromType);
        if (fromAcc == null)     return OperationResult.ACCOUNT_NOT_FOUND;
        if (!fromAcc.isActive()) return OperationResult.ACCOUNT_INACTIVE;

        User toUser = dataManager.getUserByUsername(toUsername);
        if (toUser == null) return OperationResult.ACCOUNT_NOT_FOUND;

        Account toAcc = toUser.getAccountByType(toType);
        if (toAcc == null)     return OperationResult.ACCOUNT_NOT_FOUND;
        if (!toAcc.isActive()) return OperationResult.ACCOUNT_INACTIVE;

        // Нельзя переводить на тот же счёт
        if (fromAcc.getId().equals(toAcc.getId())) return OperationResult.SAME_ACCOUNT;

        // Проверяем доступный баланс
        double available = fromAcc.getType().equals(Account.TYPE_CREDIT)
                ? fromAcc.getBalance() + fromAcc.getCreditLimit()
                : fromAcc.getBalance();

        if (amount > available) return OperationResult.INSUFFICIENT_FUNDS;

        String txId = UUID.randomUUID().toString();

        // Списываем у отправителя
        fromAcc.setBalance(fromAcc.getBalance() - amount);
        Transaction txFrom = new Transaction(
                txId, Transaction.TYPE_TRANSFER, amount,
                "Перевод → " + toUser.getUsername(),
                fromAcc.getId(), toAcc.getId()
        );
        fromAcc.getTransactions().add(txFrom);

        // Зачисляем получателю
        toAcc.setBalance(toAcc.getBalance() + amount);
        Transaction txTo = new Transaction(
                txId, Transaction.TYPE_TRANSFER, amount,
                "Перевод ← " + freshFromUser.getUsername(),
                fromAcc.getId(), toAcc.getId()
        );
        toAcc.getTransactions().add(txTo);

        // Сохраняем обоих
        dataManager.updateUser(freshFromUser);
        dataManager.updateUser(toUser);

        // Синхронизируем переданный объект
        syncUser(fromUser, freshFromUser);

        return OperationResult.SUCCESS;
    }

    // ─────────────────────────────────────────────────────────
    // Начисление процентов по накопительному счёту
    // ─────────────────────────────────────────────────────────
    public void checkAndApplyInterest(User user) {
        if (user == null) return;

        User freshUser = dataManager.getUserById(user.getId());
        if (freshUser == null) return;

        boolean changed = false;
        for (Account acc : freshUser.getAccounts()) {
            if (!acc.getType().equals(Account.TYPE_SAVINGS)) continue;
            if (acc.getNextInterestDate() == null) continue;

            if (new Date().after(acc.getNextInterestDate())) {
                double interest = acc.getBalance() * acc.getInterestRate();
                acc.setBalance(acc.getBalance() + interest);
                acc.setNextInterestDate(new Date(
                        System.currentTimeMillis() + Account.SAVINGS_PERIOD_MS));

                Transaction tx = new Transaction(
                        UUID.randomUUID().toString(),
                        Transaction.TYPE_INTEREST,
                        interest,
                        "Начисление процентов " + (int)(acc.getInterestRate() * 100) + "% в месяц",
                        null,
                        acc.getId()
                );
                acc.getTransactions().add(tx);
                changed = true;
            }
        }

        if (changed) {
            dataManager.updateUser(freshUser);
            syncUser(user, freshUser);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Вспомогательный метод: синхронизация данных пользователя
    // ─────────────────────────────────────────────────────────
    private void syncUser(User target, User source) {
        target.setAccounts(source.getAccounts());
        target.setAdmin(source.isAdmin());
        target.setSuperAdmin(source.isSuperAdmin());
        target.setBanned(source.isBanned());
    }
}
