package com.it.attendance.lecturer;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.it.attendance.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.paperdb.Paper;


public class profile extends AppCompatActivity {
    ChipNavigationBar bottomNavigationView;
    FirebaseFirestore db;
    private FirebaseAuth auth;
    Context context;
    KAlertDialog pDialogSuccess,pDialogWarining,pDialogProgress,pDialog;
    SwitchCompat switchCompat;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecturer_profile);

        context =getApplicationContext();
        Paper.init(getApplicationContext());

        //initialize FireStore
        db = FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();

        //initialize bottom navbar
        bottomNavigationView = findViewById(R.id.BottomNavigationView);
        bottomNavigationView.setItemSelected(R.id.profile,true);
        new Handler().postDelayed(() -> bottomNavigationView.setItemSelected(R.id.profile,false), 3000);

        //go to another page from navbar
        bottomNavigationView.setOnItemSelectedListener(i -> {
                    if(i==R.id.home){
                        startActivity(new Intent(getApplicationContext(), lecturer_Home_Page.class));
                        overridePendingTransition(0, 0);
                    }
                    else if (i==R.id.fab) {
                        OpenDialog();
                        bottomNavigationView.setItemSelected(R.id.fab,false);
                    }
                    else{
                        bottomNavigationView.setItemSelected(R.id.profile,true);
                        new Handler().postDelayed(() -> bottomNavigationView.setItemSelected(R.id.profile,false), 3000);

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

            v.setFocusableInTouchMode(true);

            if(switchCompat.isChecked()){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Intent intent = getIntent();
                finishAffinity();
                finish();
                startActivity(intent);
                overridePendingTransition(0,0);
                Paper.book().write("DarkMode", true);
                Log.e("banihaniYazan","dark mode active");
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Paper.book().write("DarkMode", false);
                Intent intent = getIntent();
                finishAffinity();
                finish();
                startActivity(intent);
                overridePendingTransition(0,0);

                Log.e("banihaniYazan","dark mode not active");

            }
        });


        //display user profile info
            String name = Paper.book().read("name");
            String id = Paper.book().read("Email");
            //get email for the current user from FirebaseAuth
            TextView username = findViewById(R.id.user_name);
            username.setText(name);
            TextView userid = findViewById(R.id.u_id);
            assert id != null;
            String UID = id.substring(0, id.indexOf("@"));
            userid.setText("ID: " + UID);
            TextView eid = findViewById(R.id.email_id);
            eid.setText(id);

            //LogOut
            ImageView logout = findViewById(R.id.LogOut);
            logout.setOnClickListener(view -> {
                // Sign out the current user
                pDialogWarining = new KAlertDialog(this, KAlertDialog.WARNING_TYPE, Paper.book().read("DarkMode",false));
                pDialogWarining.setTitleText("Logout");
                pDialogWarining.confirmButtonColor(R.color.blue);
                pDialogWarining.cancelButtonColor(R.color.blue);

                pDialogWarining.setContentText("Are you sure you want to logout?");
                pDialogWarining.setCancelClickListener("Cancel", kAlertDialog -> pDialogWarining.dismissWithAnimation());
                pDialogWarining.setConfirmClickListener("Logout", kAlertDialog -> {
                    Paper.init(getApplicationContext());
                    Paper.book().write("isLoggedIn", "false");
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Paper.book().write("DarkMode", false);
                    auth.signOut();
                    Intent intent = new Intent(getApplicationContext(), Login_lecturer.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    finishAffinity();
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    pDialogWarining.dismissWithAnimation();
                });
                pDialogWarining.show();

            });//end Logout image event


            //go back page
            ImageView back = findViewById(R.id.back);
            back.setOnClickListener(view -> {            // back to homepage
                onBackPressed();
            });


            //change password
            LinearLayout changePass = findViewById(R.id.changePass);
            changePass.setOnClickListener(view -> {
                pDialogProgress = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE, Paper.book().read("DarkMode",false));
                pDialogProgress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                pDialogProgress.setTitleText("Loading");
                pDialogProgress.setCancelable(false);
                pDialogProgress.show();

                pDialogSuccess = new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE, Paper.book().read("DarkMode",false));
                pDialogSuccess.setTitleText("Successfully");
                pDialogSuccess.setContentText("Check your mailbox to reset your password!");
                pDialogSuccess.confirmButtonColor(R.color.blue);
                pDialogSuccess.setConfirmClickListener("Ok", kAlertDialog -> pDialogSuccess.dismissWithAnimation());

                auth.sendPasswordResetEmail(Objects.requireNonNull(auth.getCurrentUser().getEmail())).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pDialogProgress.dismissWithAnimation();
                        pDialogSuccess.show();
                    } else {
                        pDialogProgress.dismissWithAnimation();
                        Toast.makeText(getApplicationContext(), "Error in sending password reset email", Toast.LENGTH_SHORT).show();

                    }
                });//end sendPasswordResetEmail


            });



    }//end onCreate




    private void OpenDialog(){
        Dialog dialog = new  Dialog(this,android.R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.lecturer_insert_class_dialog);
        dialog.setCanceledOnTouchOutside(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        EditText mCName = dialog.findViewById(R.id.CourseName);
        EditText mCID = dialog.findViewById(R.id.CourseID);
        EditText mSec = dialog.findViewById(R.id.CourseSection);
        TextView create = dialog.findViewById(R.id.button_createClass);

        //start onClick
        create.setOnClickListener(v -> {
            boolean validate = true;
            if (mCName.getText().toString().equals("")) {
                mCName.setError("Course name cannot be empty");
                mCName.requestFocus();
                validate=false;
            }
            if (mCID.getText().toString().equals("")||mCID.getText().toString().length()<6){
                mCID.setError("Course ID is not valid");
                mCID.requestFocus();
                validate=false;
            }
            if( mSec.getText().toString().equals("")){
                mSec.setError("Section cannot be empty");
                mSec.requestFocus();
                validate=false;
            }
            if(validate){
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);


                pDialog = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE,false);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                pDialog.setTitleText("Loading");
                pDialog.setCancelable(false);
                dialog.dismiss();
                pDialog.show();


                String cnum=mCID.getText().toString()+"sec"+mSec.getText().toString();
                //store course name and course number into firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference usersRef = db.collection("course");
                DocumentReference userDocRef = usersRef.document(cnum);//course number

                //check if course is already in the database or not
                userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Document exists!
                                Log.d(TAG, "Document exists!");
                                // Access the document data:
                                pDialog.dismissWithAnimation();                                // ...
                                Toast.makeText(getApplicationContext(), "The course already exists", Toast.LENGTH_SHORT).show();

                            } else {
                                // Document does not exist!
                                Log.d(TAG, "Document does not exist!");
                                //get email for the current user from FirebaseAuth to filter the courses in home page
                                String CourseCreatedBy= FirebaseAuth.getInstance().getCurrentUser().getEmail();

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("cNumber", mCID.getText().toString());
                                updates.put("cName", mCName.getText().toString());
                                updates.put("cSection", mSec.getText().toString());
                                updates.put("CreatedBy", CourseCreatedBy);
                                updates.put("enrollStudents",new ArrayList<String>());

                                //create the course
                                userDocRef.set(updates).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {

                                        pDialog.dismissWithAnimation();
                                        Log.d("TAG", "Course saved successfully");
                                        Toast.makeText(getApplicationContext(), "Successfully created course", Toast.LENGTH_SHORT).show();


                                    } else {
                                        Log.d("TAG", "Error saving Course : ", task1.getException());
                                        Toast.makeText(getApplicationContext(), "Error creation course", Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }
                        } else {
                            // Handle errors
                            Log.d(TAG, "Error getting document: ", task.getException());
                        }
                    }
                });


            }//end if

        });//end create.setOnClick

    }// end opendialog


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("profile","back pressed");
        finish();
        startActivity(new Intent(this, lecturer_Home_Page.class));
        overridePendingTransition(0, 0);

    }

}//end Class