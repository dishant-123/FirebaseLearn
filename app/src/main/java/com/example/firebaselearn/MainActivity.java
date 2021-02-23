package com.example.firebaselearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityMainBinding binding;
    private ProgressDialog progressDialog;
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submitButton:
                doSubmitUserDetails();
                break;
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