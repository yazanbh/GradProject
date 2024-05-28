package com.it.attendance.student;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.developer.kalert.KAlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.it.attendance.PasswordValidator;
import com.it.attendance.R;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

public class Signup_std extends AppCompatActivity {
    Button signup;
    EditText Name,Email,pass1,pass2;
    private FirebaseAuth mAuth;
    KAlertDialog pDialog;
    TextView req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_std);

        boolean isDarkMode = Paper.book().read("DarkMode",false);
        if(isDarkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //declare variables to save it on firebase
        Name=findViewById(R.id.signup_std_name);
        Email=findViewById(R.id.signup_std_email);
        pass1=findViewById(R.id.signup_std_pass1);
        pass2=findViewById(R.id.signup_std_pass2);
        signup=findViewById(R.id.signup_std_btn);
        signup.setOnClickListener(view -> {
            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            if(validate()){
                pDialog = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE,false);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                pDialog.setTitleText("Loading");
                pDialog.setCancelable(false);
                pDialog.show();
                mAuth.createUserWithEmailAndPassword(Email.getText().toString(), pass1.getText().toString())
                        .addOnSuccessListener(authResult -> {
                            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    // User successfully signed up.
                                    Toast.makeText(Signup_std.this, "user Registered Successfully, Please verify your email. ", Toast.LENGTH_SHORT).show();
                                }
                            });
                            //store into firestore
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            CollectionReference usersRef = db.collection("students");
                            DocumentReference userDocRef = usersRef.document(Email.getText().toString());
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("name", Name.getText().toString());
                            updates.put("email", Email.getText().toString());

                            userDocRef.set(updates).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "Device ID saved successfully");

                                } else {
                                    Log.d("TAG", "Error saving device ID: ", task.getException());
                                }
                            });
                            pDialog.dismissWithAnimation();
                            //move to login page
                            Intent intent = new Intent(Signup_std.this, Login_std.class);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            pDialog.dismissWithAnimation();
                            Toast.makeText(Signup_std.this, "cannot sign up", Toast.LENGTH_SHORT).show();
                        });
            }//end if


        });//end setOnClickListener

        req= findViewById(R.id.reqiurements);
        req.setOnClickListener(v -> {
            pDialog = new KAlertDialog(this, KAlertDialog.NORMAL_TYPE,false);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Password Requirements");
            pDialog.confirmButtonColor(R.color.blue);
            pDialog.setContentText("Password must be at least 8 characters long and include capital letters and punctuation (e.g., #, _, &).");
            pDialog.setCancelable(true);
            pDialog.setConfirmClickListener("OK", kAlertDialog -> {pDialog.dismissWithAnimation();
            });
            pDialog.show();
        });




    }//end onCreate
    public boolean validate(){
        boolean t=true;
        String username =Name.getText().toString();
        String pass=pass1.getText().toString();
        String confirmpass=pass2.getText().toString();
        String email=Email.getText().toString();
        if(username.isEmpty() || username.length()<10){
            Name.setError("your username is not valid");
            Name.requestFocus();
            t=false;
        }
        if (email.isEmpty() || !email.endsWith("@st.aabu.edu.jo")){
            Email.setError("Email is not valid");
            Email.requestFocus();
            t=false;
        }
        if(!PasswordValidator.isValidPassword(pass)){
            pass1.setError("Password is weak! Please use a stronger password.");
            pass1.requestFocus();
            t=false;
        }
        if(!confirmpass.equals(pass)){
            pass2.setError("password not match!");
            pass2.requestFocus();
            t=false;
        }
        if(pass.isEmpty() && confirmpass.isEmpty()){
            pass1.setError("password cannot be empty");
            pass2.setError("password cannot be empty");
            pass1.requestFocus();
            pass2.requestFocus();
            t=false;
        }
        return t;
    }//end function
}//end class