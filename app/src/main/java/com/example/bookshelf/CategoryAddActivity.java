package com.example.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.bookshelf.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {

    //view binding
    private ActivityCategoryAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth= FirebaseAuth.getInstance();
        
        //configure progress dialog
        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        
        //handle click, begin upload category
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String category="";
    private void validateData() {
        /*Before adding validate data*/
        //get data
        category= binding.categoryEt.getText().toString().trim();
        //validate if not empty
        if(TextUtils.isEmpty(category)){
            Toast.makeText(this, "Please enter category", Toast.LENGTH_SHORT).show();
        }
        else{
            addCategoryFirebase();
        }

    }

    private void addCategoryFirebase() {
        //show progress
        progressDialog.setMessage("Adding Category...");
        progressDialog.show();

        //get timestamp
        long timestamp= System.currentTimeMillis();

        //setup info to add firebase database
        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("category", ""+category);
        hashMap.put("timestamp",timestamp);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //add to firebase database....Database Root > Categories > categoryID > category info
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //category add success
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, "Category added successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //category add failed
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}