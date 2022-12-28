package com.attendancetaker;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.attendancetaker.R;
import com.example.attendancetaker.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    static private ActivityMainBinding binding;

    public static void goToCheckIn() {
        View check_in_button = binding.getRoot().findViewById(R.id.navigation_check_in);
        check_in_button.performClick();
    }

    public static void goToLogin() {
        View home_button = binding.getRoot().findViewById(R.id.navigation_home);
        home_button.performClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_check_in, R.id.navigations_history)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

}