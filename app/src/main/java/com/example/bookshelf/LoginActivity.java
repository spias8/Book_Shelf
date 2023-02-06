package com.example.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bookshelf.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    //view binding
    private ActivityLoginBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //handle click, on go to previous
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //init firebase auth
        firebaseAuth= FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go to register screen
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

            }
        });

        //handle click, begin login
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        //handle click, open forgot password activity
        binding.forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }
    private String email= "", password="";
    private void validateData() {
        /*Before login. lets do some data validation*/

        //get data
        email= binding.emailEt.getText().toString().trim();
        password= binding.passwordEt.getText().toString().trim();

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show();
        }
        else{
            //data is validate, begin login
            loginUser();
        }
    }

    private void loginUser() {
        //show progress
        progressDialog.setMessage("Loggin In...");
        progressDialog.show();

        //login user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //login success, check if user is user or admin
                        checkUser();
                        
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //login failed
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Checking User...");
        //check if user is user or admin fro  realtime database
        //get current user
        FirebaseUser firebaseUser= firebaseAuth.getCurrentUser();

        //check in database
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        //get user type
                        String userType= ""+snapshot.child("userType").getValue();
                        //check user type
                        if (userType.equals("user")){
                            //this is simple user, open user dashboard
                            startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                            finish();
                        }
                        else if(userType.equals("admin")){
                            //this is admin, open user dashboard
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
        }
    }
/*We didn't implemented the Forgot Password | Password recovery yet
* You can also implemented that code in anay firebase project/app
* * Let's starts...*/
