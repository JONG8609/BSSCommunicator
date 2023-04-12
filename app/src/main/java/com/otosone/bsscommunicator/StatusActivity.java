package com.otosone.bsscommunicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.Button;

import com.otosone.bsscommunicator.databinding.ActivityStatusBinding;

public class StatusActivity extends AppCompatActivity {

    private ActivityStatusBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        Databinding();


    }

    private void Databinding() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_status);


    }
}