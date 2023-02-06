package com.example.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.bookshelf.databinding.ActivityDashboardAdminBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {

    //view binding
    private ActivityDashboardAdminBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //arraylist to store category
    private ArrayList<ModelCategory> categoryArrayList;

    //adapter
    private AdapterCategory adapterCategory;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityDashboardAdminBinding.inflate(getLayoutInflater());
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
        checkUser();
        loadCategories();

        //edit text change listern, search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called as and when user type each letter
                try {
                    adapterCategory.getFilter().filter(s);
                }
                catch (Exception e){

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //handle click, logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                checkUser();
            }
        });
        //handle click, start category add screen
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, CategoryAddActivity.class));
            }
        });

        //handle click, start pdf add screen
        binding.addPdFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this, PdfAddActivity.class));
            }
        });
    }

    private void loadCategories() {
        //init arraylist
        categoryArrayList= new ArrayList<>();

        //get all categories from firebase> categories
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear arraylist before adding data into it
                categoryArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelCategory model= ds.getValue(ModelCategory.class);

                    //add to arraylist
                    categoryArrayList.add(model);
                }
                //setup adapter
                adapterCategory= new AdapterCategory(DashboardAdminActivity.this, categoryArrayList);
                //set adapter to recycleview
                binding.categoriesRv.setAdapter(adapterCategory);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser= firebaseAuth.getCurrentUser();
        if(firebaseUser == null){
            //not logged in, goto main screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else{
            //logged in, get user info
            String email= firebaseUser.getEmail();
            //set in textview off toolbar
            binding.subTitleTv.setText(email);
        }
    }
}