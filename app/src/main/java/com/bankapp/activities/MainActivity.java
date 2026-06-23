package com.bankapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bankapp.R;
import com.bankapp.managers.AuthManager;
import com.bankapp.managers.BankManager;
import com.bankapp.managers.DataManager;
import com.bankapp.models.Bank;
import com.bankapp.models.User;

public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = AuthManager.getInstance(this);
        currentUser = authManager.getCurrentUser();

        Bank bank = DataManager.getInstance(this).getBank();

        TextView tvBankName    = findViewById(R.id.tvMainBankName);
        TextView tvWelcome     = findViewById(R.id.tvMainWelcome);
        TextView tvBalance     = findViewById(R.id.tvMainBalance);

        CardView cardAccounts  = findViewById(R.id.cardAccounts);
        CardView cardTransfer  = findViewById(R.id.cardTransfer);
        CardView cardProfile   = findViewById(R.id.cardProfile);
        CardView cardAdmin     = findViewById(R.id.cardAdmin);

        ImageButton btnHelp    = findViewById(R.id.btnHelp);
        Button btnLogout       = findViewById(R.id.btnLogout);

        if (bank != null) tvBankName.setText(bank.getName());
        tvWelcome.setText("Добро пожаловать, " + currentUser.getFullName() + "!");

        BankManager.getInstance(this).checkAndApplyInterest(currentUser);
        authManager.refreshCurrentUser(this);
        currentUser = authManager.getCurrentUser();

        updateBalance(tvBalance);

        if (!currentUser.isAdmin()) cardAdmin.setVisibility(android.view.View.GONE);

        cardAccounts.setOnClickListener(v ->
                startActivity(new Intent(this, AccountsActivity.class)));

        cardTransfer.setOnClickListener(v ->
                startActivity(new Intent(this, TransferActivity.class)));

        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        cardAdmin.setOnClickListener(v ->
                startActivity(new Intent(this, AdminPanelActivity.class)));

        btnHelp.setOnClickListener(v -> showHelpDialog());

        btnLogout.setOnClickListener(v -> {
            authManager.logout();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        authManager.refreshCurrentUser(this);
        currentUser = authManager.getCurrentUser();
        BankManager.getInstance(this).checkAndApplyInterest(currentUser);
        authManager.refreshCurrentUser(this);
        currentUser = authManager.getCurrentUser();
        TextView tvBalance = findViewById(R.id.tvMainBalance);
        updateBalance(tvBalance);
    }

    private void updateBalance(TextView tvBalance) {
        tvBalance.setText(String.format("Общий баланс: %.2f ₽", currentUser.getTotalBalance()));
    }

    private void showHelpDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_help);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        Button btnClose = dialog.findViewById(R.id.btnHelpClose);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}