package com.it.attendance.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.it.attendance.MainActivity;
import com.it.attendance.R;
import com.it.attendance.forgot_password;

import io.paperdb.Paper;

public class Login_std extends AppCompatActivity {
    TextView btn, forgot_pass, sign_up;
    EditText emailEditText , passwordEditText;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_student);

        emailEditText = findViewById(R.id.std_email);
        passwordEditText = findViewById(R.id.std_password);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //move from login page to sign up page
        sign_up = findViewById(R.id.std_signup);
        sign_up.setOnClickListener(view -> {
            Intent intent = new Intent(Login_std.this, Signup_std.class);
            startActivity(intent);
        });


        //move from login page to start student or lecturer page after validation
        btn=findViewById(R.id.std_signin);
        btn.setOnClickListener(view -> {
            LoginUser();
        });

        //move from login page to forgot password page
        forgot_pass=findViewById(R.id.std_ForgotPassword);
        forgot_pass.setOnClickListener(view -> {
            Intent intent = new Intent(Login_std.this, forgot_password.class);
            intent.putExtra("source_page", "StudentLogin");
            startActivity(intent);
        });



    }//end onCreate
    private void LoginUser(){
        // get string of email and password and remove any space
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();


        if(validate()){
            // Perform Firebase Authentication
            mAuth.signInWithEmailAndPassword(email.trim(), password.trim())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                               // Toast.makeText(Login_std.this, "Authentication successfully.", Toast.LENGTH_SHORT).show();

                                Paper.init(getApplicationContext());
                                Paper.book().destroy();

                                Paper.book().write("Email", email);
                                Paper.book().write("Password", password);
                                Paper.book().write("isLoggedIn", "true");
                                Paper.book().write("type", "student");

                                Intent intent = new Intent(Login_std.this, HomePage_std.class);
                                startActivity(intent);

                            }
                            else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(Login_std.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            /*
                            //check if email is verified or not
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    // User is verified, proceed with the app

                                    // copy the code from up to the comment and paste it here


                                }
                                else {
                                    // User is not verified, show a message or disable features
                                    Toast.makeText(Login_std.this, "Please verify your email address before continuing.", Toast.LENGTH_SHORT).show();
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(Login_std.this, "Verification email sent again!", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(Login_std.this, "Error sending verification email!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }*/



                        }
                    });
        }//end if

    }//end login function

    public boolean validate(){
        boolean t=true;
        String pass=passwordEditText.getText().toString();
        String emaill=emailEditText.getText().toString();
        if (emaill.isEmpty() || !emaill.trim().endsWith("@st.aabu.edu.jo")){
            emailEditText.setError("Email is not valid");
            emailEditText.requestFocus();
            t=false;
        }
        if(pass.isEmpty() || pass.length()<7){
            passwordEditText.setError("password must be 7 character at least");
            passwordEditText.requestFocus();
            t=false;
        }

        return t;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finishAffinity();
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();

    }

}//end class