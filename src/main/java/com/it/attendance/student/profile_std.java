package com.it.attendance.student;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.developer.kalert.KAlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.it.attendance.R;

import io.paperdb.Paper;

public class profile_std extends AppCompatActivity {
    ChipNavigationBar bottomNavigationView;
    FirebaseFirestore db;
    private FirebaseAuth auth;
    Context context;
    KAlertDialog pDialogSuccess, pDialogWarining, pDialogProgress;
    SwitchCompat switchCompat;
    private SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout UID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.std_profile);
        context = getApplicationContext();
        Paper.init(getApplicationContext());

        //initialize FireStore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        //initialize bottom navbar
        bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        bottomNavigationView.setItemSelected(R.id.profile,true);
        new Handler().postDelayed(() -> bottomNavigationView.setItemSelected(R.id.profile,false), 3000);

        //go to another page from navbar
        bottomNavigationView.setOnItemSelectedListener(i -> {
            if(i==R.id.home){
                startActivity(new Intent(getApplicationContext(), HomePage_std.class));
                overridePendingTransition(0, 0);
            }
            else{
                bottomNavigationView.setItemSelected(R.id.profile,true);
                new Handler().postDelayed(() -> bottomNavigationView.setItemSelected(R.id.home,false), 3000);

            }
        }
        );


        // dark or light mode
        switchCompat = findViewById(R.id.SwitchCompatbtn);
        boolean isDarkMode = Paper.book().read("DarkMode",false);
        if(isDarkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        Log.e("Yazan", "Dark mode is :"+ isDarkMode);
        switchCompat.setChecked(isDarkMode);
        switchCompat.setOnClickListener(v -> {
            if(switchCompat.isChecked()){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Paper.book().write("DarkMode", true);
                Log.e("banihaniYazan","dark mode active");
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Paper.book().write("DarkMode", false);
                Log.e("banihaniYazan","dark mode not active");

            }
        });




        //display user profile info

        //set user email
        String Email =Paper.book().read("Email");
        TextView eid = findViewById(R.id.email_id);
        eid.setText(Email);

        //set user id
        TextView userid = findViewById(R.id.u_id);
        String UserID = Email.substring(0, Email.indexOf("@"));
        userid.setText("ID: " + UserID);


        //set username
        TextView username = findViewById(R.id.user_name);
        String uname =Paper.book().read("name");
        username.setText(uname);

        //LogOut
        ImageView logout = findViewById(R.id.LogOut);
        logout.setOnClickListener(view -> {
            // Sign out the current user
            pDialogWarining = new KAlertDialog(this, KAlertDialog.WARNING_TYPE, false);
            pDialogWarining.setTitleText("Logout");
            pDialogWarining.confirmButtonColor(R.color.blue);
            pDialogWarining.cancelButtonColor(R.color.blue);
            pDialogWarining.setContentText("Are you sure you want to logout?");
            pDialogWarining.setCancelClickListener("Cancel", new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    pDialogWarining.dismissWithAnimation();
                }
            });
            pDialogWarining.setConfirmClickListener("Logout", new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    Paper.init(getApplicationContext());
                    Paper.book().write("isLoggedIn", "false");
                    Paper.book().write("DarkMode", false);

                    auth.signOut();
                    Intent intent = new Intent(getApplicationContext(), Login_std.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    finishAffinity();
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    pDialogWarining.dismissWithAnimation();
                }
            });
            pDialogWarining.show();

        });//end Logout image event


        //go back page
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(view -> {            // back to homepage
            onBackPressed();
        });


        //add university card
         UID = findViewById(R.id.UnversityID);
        boolean card = Paper.book().read("card");
        if(card){
            UID.setVisibility(View.GONE);
        }
        UID.setOnClickListener(view->{
            startActivity(new Intent(this, CardNumber_std.class));
            overridePendingTransition(0, 0);
            finish();
        });



        //change password
        LinearLayout changePass = findViewById(R.id.changePass);
        changePass.setOnClickListener(view -> {
            pDialogProgress = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE, false);
            pDialogProgress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialogProgress.setTitleText("Loading");
            pDialogProgress.setCancelable(false);
            pDialogProgress.show();

            pDialogSuccess = new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE, false);
            pDialogSuccess.setTitleText("Successfully");
            pDialogSuccess.setContentText("Check your mailbox to reset your password!");
            pDialogSuccess.confirmButtonColor(R.color.blue);
            pDialogSuccess.setConfirmClickListener("Ok", new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    pDialogSuccess.dismissWithAnimation();
                }
            });

            auth.sendPasswordResetEmail(auth.getCurrentUser().getEmail()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    pDialogProgress.dismissWithAnimation();
                    pDialogSuccess.show();
                } else {
                    pDialogProgress.dismissWithAnimation();
                    Toast.makeText(getApplicationContext(), "Error in sending password reset email", Toast.LENGTH_SHORT).show();

                }
            });//end sendPasswordResetEmail


        });


        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh your data here
                refreshData();
            }
        });


    }//end onCreate

    private void refreshData() {
        // Your data refresh logic here
        // Once data is refreshed, call swipeRefreshLayout.setRefreshing(false) to stop the refresh animation
        swipeRefreshLayout.setRefreshing(false);
        // Get a reference to the document to get if user have UID (card number) or not
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("students").document(mAuth.getCurrentUser().getEmail());
        // Get the document
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Paper.init(getApplicationContext());
                    // Extract data from the document

                    if(document.getString("card")!=null){
                        Paper.book().write("card",true);
                        UID.setVisibility(View.GONE);
                    }
                    else{
                        Paper.book().write("card",false);
                        UID.setVisibility(View.VISIBLE);
                    }
                    // Do something with the data
                    Log.d(ContentValues.TAG, "Document existed");

                } else {
                    Log.d(ContentValues.TAG, "No such document!");
                }
            } else {
                Log.w(ContentValues.TAG, "Error getting document", task.getException());
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, HomePage_std.class));
        overridePendingTransition(0, 0);
        finish();
    }



    }//end Class
