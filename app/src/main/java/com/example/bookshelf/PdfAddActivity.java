package com.example.bookshelf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookshelf.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {
    //setup view binding
    private ActivityPdfAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //firebase dialog
    private ProgressDialog progressDialog;

    //arraylist to hold pdf categories
    private ArrayList<ModelCategory> categoryArrayList;

    //uri of picked pdf
    private Uri pdfUri= null;

    private static final int PDF_PICK_CODE = 1000;

    //TAG for debugging
    private static final String TAG = "Add_PDF_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        //setup progress dialog
        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        //handle click, attach pdf
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });
        
        //handle click, pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });
        //handle click, upload pdf
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validate data
                validateData();

            }
        });
    }

    private String title= "", description= "", category= "";
    private void validateData() {
        //Step 1: Validate data
        Log.d(TAG, "validateData: validating data...");

        //get  data
        title= binding.titleEt.getText().toString().trim();
        description= binding.descriptionEt.getText().toString().trim();
        category= binding.categoryTv.getText().toString().trim();

        //validate data
        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(category)){
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show();
        } else if (pdfUri==null) {
            Toast.makeText(this, "Pick Pdf", Toast.LENGTH_SHORT).show();
        }
        else {
            //all data is valid, can upload now
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        //Step 2: Upload Pdf to firebase storage
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...");

        //show progress
        progressDialog.setMessage("Uploading Pdf...");
        progressDialog.show();

        //timestamp
        long timestamp= System.currentTimeMillis();

        //path of pdf in firebase storage
        String filePathAndName= "Books/"+timestamp;
        //storage reference
        StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "OnSuccess: PDF uploaded to storage...");
                        Log.d(TAG, "OnSuccess: getting pdf url");

                        //get pdf url
                        Task<Uri> uriTask= taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String uploadedPdfUrl= ""+uriTask.getResult();
                        
                        //upload to firebase database
                        uploadPdfInfoToDb(uploadedPdfUrl, timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                    Log.d(TAG, "OnFailure: PDF upload failed due to "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "PDF upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        //Step 3: Upload Pdf Info to firebase database
        Log.d(TAG, "uploadPdfToStorage: uploading Pdf info to firebase database...");

        progressDialog.setMessage("Uploading pdf info...");

        String uid= firebaseAuth.getUid();

        //setup data to upload
        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("category", ""+category);
        hashMap.put("url", ""+uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);

        //database reference: Database> books
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully uploaded...");
                        Toast.makeText(PdfAddActivity.this, "Successfully uploaded...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to database due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to upload to database due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories...");
        categoryArrayList= new ArrayList<>();

        //database reference to load categories....database>categories
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear(); //clear before adding data
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    ModelCategory model= ds.getValue(ModelCategory.class);
                    //add to arraylist
                    categoryArrayList.add(model);

                    Log.d(TAG, "onDataChange:"+model.getCategory());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        //get string array og category from arraylist
        String[] categoriesArray= new String[categoryArrayList.size()];
        for(int i= 0; i<categoryArrayList.size(); i++){
            categoriesArray[i]= categoryArrayList.get(i).getCategory();
        }
        //alert dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //handle item click
                        //get clicked item from list
                        String category= categoriesArray[i];
                        //set to category textview
                        binding.categoryTv.setText(category);

                        Log.d(TAG, "onClick: Selected Category:"+category);
                    }
                })
                .show();
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select pdf"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {
                Log.d(TAG, "onActivityResult: PDF picked");

                pdfUri= data.getData();

                Log.d(TAG, "onActivityResult: URI: +pdfUri");
            }
        }
        else{
            Log.d(TAG, "onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }
}