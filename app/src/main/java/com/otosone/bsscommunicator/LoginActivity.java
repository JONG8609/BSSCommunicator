package com.otosone.bsscommunicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.viewbinding.ViewBinding;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.otosone.bsscommunicator.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private EditText username_et, password_et;
    private Button login_btn;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dataBinding();
        initialize();
        username_et.setText("admin");
        password_et.setText("otosone");
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = username_et.getText().toString().trim();
                String password = password_et.getText().toString().trim();

                if(authenticateUser(username, password) == 1){
                    Intent intent = new Intent(LoginActivity.this, ScanActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();

                }else {
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initialize() {

    }

    private void dataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        username_et = binding.usernameEt;
        password_et = binding.passwordEt;
        login_btn = binding.loginBtn;
    }
    private int authenticateUser(String username, String password) {
        if (username.equals("admin") && password.equals("otosone")) {
            return 1;
        } else {
            return 0;
        }
    }


}