package com.bankapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bankapp.R;
import com.bankapp.managers.AuthManager;
import com.bankapp.managers.DataManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvLogo = findViewById(R.id.tvSplashLogo);
        TextView tvSub  = findViewById(R.id.tvSplashSub);

        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(1500);
        tvLogo.startAnimation(fade);
        tvSub.startAnimation(fade);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            DataManager dm = DataManager.getInstance(this);
            // ИЗМЕНЕНИЕ: Выходим из сессии при каждом запуске приложения
            AuthManager.getInstance(this).logout();

            Intent intent;
            if (!dm.isBankInitialized()) {
                intent = new Intent(this, BankNameActivity.class);
            } else {
                // Всегда требуем повторного входа при запуске
                intent = new Intent(this, AuthActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }
}
