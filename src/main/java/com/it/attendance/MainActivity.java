package com.it.attendance;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.developer.kalert.KAlertDialog;
import com.it.attendance.lecturer.Login_lecturer;
import com.it.attendance.student.Login_std;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce = false;
    CardView teacher,std;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_your_role);


        boolean isDarkMode = Paper.book().read("DarkMode",false);
        if(isDarkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        
        // Check internet connection initially
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        }


        std=findViewById(R.id.std);
        teacher=findViewById(R.id.teacher);

        std.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Login_std.class);
            startActivity(intent);
        });

        teacher.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Login_lecturer.class);
            startActivity(intent);
        });


    }//end onCreate

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finishAffinity();
            finish();
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
            // The above code sets doubleBackToExitPressedOnce back to false after 2 seconds (2000 milliseconds).
            // Adjust the delay as needed.
        }
    }//end onBackPressed

    private boolean isNetworkAvailable() {
        Context context = getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        new KAlertDialog(this, KAlertDialog.ERROR_TYPE,false)
                .setTitleText("No Internet Connection")
                .setContentText("Please connect to an internet network to proceed.")

                .setConfirmClickListener("OK", new KAlertDialog.KAlertClickListener() {
                    @Override
                    public void onClick(KAlertDialog kAlertDialog) {
                        finish();
                    }
                })
                .show();
    }

}//end class