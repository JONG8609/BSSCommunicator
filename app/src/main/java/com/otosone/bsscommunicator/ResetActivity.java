package com.otosone.bsscommunicator;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.navigation.NavigationView;
import com.otosone.bsscommunicator.databinding.ActivityResetBinding;

public class ResetActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ActivityResetBinding binding;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    ImageView resetMenuIv;

    private Context context;
    Button reset_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBinding();
        context = this;

        navigationView.setNavigationItemSelectedListener(this);

        resetMenuIv.setOnClickListener(v -> openNavigationDrawer());

       reset_btn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               AlertDialog.Builder builder = new AlertDialog.Builder(context);
               builder.setTitle("Reset Confirmation");
               builder.setMessage("Would you like to run the command?");

               builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       // Perform the reset action here
                   }
               });

               builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       // Close the dialog and do nothing
                       dialog.dismiss();
                   }
               });

               AlertDialog alertDialog = builder.create();
               alertDialog.show();
           }
       });

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d("Drawer", "Navigation item selected");
        Intent intent;

        switch (item.getItemId()) {
            case R.id.nav_status:
                intent = new Intent(this, StatusActivity.class);
                break;
            case R.id.nav_reset:
                intent = new Intent(this, ResetActivity.class);
                break;
            case R.id.nav_charging:
                intent = new Intent(this, ChargingActivity.class);
                break;
            case R.id.nav_door:
                intent = new Intent(this, DoorActivity.class);
                break;
            case R.id.nav_station:
                intent = new Intent(this, StationActivity.class);
                break;
            case R.id.nav_fan_and_heater:
                intent = new Intent(this, FanAndHeaterActivity.class);
                break;
            case R.id.nav_charger:
                intent = new Intent(this, ChargerActivity.class);
                break;
            default:
                return false;
        }

        startActivity(intent);
        finish();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void openNavigationDrawer() {
        Log.d("Drawer", "Opening navigation drawer");
        if (drawer != null) {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    private void DataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset);
        reset_btn = binding.resetBtn;
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        resetMenuIv = binding.resetMenuIv;
    }
}
