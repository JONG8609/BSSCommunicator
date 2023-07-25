package com.otosone.bssmgr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.otosone.bssmgr.databinding.ActivityLoginBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText username_et, password_et;
    private Button login_btn;
    private TextView change_password_tv, forgot_password_tv;
    private ActivityLoginBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("com.otosone.bssmgr", Context.MODE_PRIVATE);

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
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }else {
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        change_password_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword(v);
            }
        });

        forgot_password_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword(v);
            }
        });
    }

    public void changePassword(View view) {
        String username = username_et.getText().toString().trim();

        if (username.equals("admin") || username.equals("habas_admin") || username.equals("habas_mgr")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.change_password_dialog, null);
            builder.setView(dialogView);

            TextView username_tv = dialogView.findViewById(R.id.username_tv);
            username_tv.setText(username);

            EditText current_password_et = dialogView.findViewById(R.id.current_password_et);
            EditText new_password_et = dialogView.findViewById(R.id.new_password_et);

            AlertDialog dialog = builder.create();

            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Change", (dialogInterface, which) -> {
            });

            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialogInterface, which) -> {
                dialog.dismiss();
            });

            dialog.setOnShowListener(dialogInterface -> {

                Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view1 -> {

                    String currentPassword = current_password_et.getText().toString().trim();
                    String newPassword = new_password_et.getText().toString().trim();

                    if (username.equals("admin") && currentPassword.equals(getPassword("admin"))) {
                        setPassword("admin", newPassword);
                        dialog.dismiss();
                    } else if (username.equals("habas_admin") && currentPassword.equals(getPassword("habas_admin"))) {
                        setPassword("habas_admin", newPassword);
                        dialog.dismiss();
                    } else if (username.equals("habas_mgr") && currentPassword.equals(getPassword("habas_mgr"))) {
                        setPassword("habas_mgr", newPassword);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid current password", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            dialog.show();
        } else {
            Toast.makeText(LoginActivity.this, "Invalid username", Toast.LENGTH_SHORT).show();
        }
    }

    public void forgotPassword(View view) {
        String username = username_et.getText().toString().trim();

        if (username.equals("admin") || username.equals("habas_admin") || username.equals("habas_mgr")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.reset_password_dialog, null);
            builder.setView(dialogView);

            EditText username_et = dialogView.findViewById(R.id.reset_username_et);
            EditText authentication_code_et = dialogView.findViewById(R.id.authentication_code_tv);

            AlertDialog dialog = builder.create();

            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Reset", (dialogInterface, which) -> {
                // this will be overridden below
            });

            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialogInterface, which) -> {
                dialog.dismiss();
            });

            dialog.setOnShowListener(dialogInterface -> {
                Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view1 -> {
                    String resetUsername = username_et.getText().toString().trim();
                    String authenticationCode = authentication_code_et.getText().toString().trim();

                    // get the current date in "MMdd" format
                    String currentDate = new SimpleDateFormat("MMdd", Locale.getDefault()).format(new Date());
                    String expectedAuthenticationCode = currentDate + "otos";

                    if (!resetUsername.isEmpty() && authenticationCode.equals(expectedAuthenticationCode)) {
                        // Reset the password based on username
                        if (resetUsername.equals("admin")) {
                            setPassword(resetUsername, "otosone");
                        } else if (resetUsername.equals("habas_admin") || resetUsername.equals("habas_mgr")) {
                            setPassword(resetUsername, "habas1234");
                        }
                        dialog.dismiss();
                    } else {
                        Toast.makeText(LoginActivity.this, "Please enter a valid ID and Authentication Code", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            dialog.show();
        } else {
            Toast.makeText(LoginActivity.this, "Invalid username", Toast.LENGTH_SHORT).show();
        }
    }




    private void initialize() {
        // add your initialization code here
    }

    private void dataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        username_et = binding.usernameEt;
        password_et = binding.passwordEt;
        login_btn = binding.loginBtn;
        change_password_tv = binding.changePasswordTv;
        forgot_password_tv = binding.forgotPasswordTv;
    }

    private int authenticateUser(String username, String password) {
        if ((username.equals("admin") && password.equals(getPassword("admin"))) ||
                (username.equals("habas_admin") && password.equals(getPassword("habas_admin"))) ||
                (username.equals("habas_mgr") && password.equals(getPassword("habas_mgr")))) {
            return 1;
        } else {
            return 0;
        }
    }

    private String getPassword(String username) {
        switch (username) {
            case "admin":
                return sharedPreferences.getString(username, "otosone");
            case "habas_admin":
            case "habas_mgr":
                return sharedPreferences.getString(username, "habas1234");
            default:
                return "";
        }
    }

    private void setPassword(String username, String password) {
        sharedPreferences.edit().putString(username, password).apply();
        Toast.makeText(LoginActivity.this, "Password changed", Toast.LENGTH_SHORT).show();
    }
}
