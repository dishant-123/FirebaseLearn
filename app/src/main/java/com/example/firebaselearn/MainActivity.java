package com.example.firebaselearn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebaselearn.databinding.ActivityMainBinding;
import com.example.firebaselearn.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityMainBinding binding;
    private ProgressDialog progressDialog;
    private static final int REQUEST_GALLERY = 33;
    public MainActivity(){}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        getUserDetails();
        initListener();
        setContentView(binding.getRoot());
    }

    private void getUserDetails() {
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren()){
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            binding.userNameTxt.setText(userModel.getName());
                            binding.emailTxt.setText(userModel.getEmail());
                            binding.passwordTxt.setText(userModel.getPassword());
                            binding.ageTxt.setText(userModel.getAge());
                            binding.genderTxt.setText(userModel.getGender());

                            Glide
                                    .with(MainActivity.this)
                                    .load(userModel.getProfileImg())
                                    .into(binding.profileImage);
                            if(userModel.getProfileImg()!=null){
                                binding.deleteButton.setVisibility(View.VISIBLE);
                                binding.uploadButton.setVisibility(View.GONE);
                            }
                            else{
                                binding.deleteButton.setVisibility(View.GONE);
                                binding.uploadButton.setVisibility(View.VISIBLE);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logoutButton:
                doLogoutUser();
                break;
            case R.id.deleteAccountButton:
                doDeleteCurrentUser();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    

    private void doDeleteCurrentUser() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("We are deleting your Account");
        progressDialog.setCancelable(false);
        progressDialog.show();
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(null)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        FirebaseAuth.getInstance().getCurrentUser().delete()
                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                  @Override
                                  public void onComplete(@NonNull Task<Void> task) {
                                      if(task.isSuccessful()){
                                          progressDialog.dismiss();
                                          Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                                          startActivity(intent);
                                      }
                                      else{

                                      }
                                  }
                              });
                    }
                });

    }

    private void doLogoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
    }

    private void initListener() {
        binding.submitButton.setOnClickListener(this);
        binding.uploadButton.setOnClickListener(this);
        binding.deleteButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submitButton:
                doSubmitUserDetails();
                break;
            case R.id.uploadButton:
                getUserPermission();
                break;
            case R.id.deleteButton:
                doDeleteUserProfileImage();
                break;
        }
    }

    private void doDeleteUserProfileImage() {
        FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Profile IMage Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("profileImg")
                .setValue(null);
    }

    private void getUserPermission() {
        if ((ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            selectImage();
        } else {
            ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 22);
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_GALLERY){
            if(data.getData()!=null){
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading");
                progressDialog.setMessage("Uploading your profile Image");
                progressDialog.setCancelable(false);
                progressDialog.show();
                final StorageReference reference = FirebaseStorage.getInstance().getReference()
                        .child("profileImages")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                reference.putFile(data.getData())
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        FirebaseDatabase
                                                .getInstance()
                                                .getReference()
                                                .child("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("profileImg")
                                                .setValue(uri.toString());
                                    }
                                });
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Profile Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            else{
                Toast.makeText(this, "Please Select Image first", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doSubmitUserDetails() {
        Map<String, Object> map = new HashMap<>();
        map.put("name",binding.userNameTxt.getText().toString().trim());
        map.put("email",binding.emailTxt.getText().toString().trim());
        map.put("password",binding.passwordTxt.getText().toString().trim());
        map.put("gender",binding.genderTxt.getText().toString().trim());
        map.put("age",binding.ageTxt.getText().toString().trim());

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}