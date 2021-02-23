package com.example.firebaselearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.firebaselearn.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    ActivitySignUpBinding binding;
    private ProgressDialog progressDialog;
    public SignUpActivity(){}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isUserAlreadySignIn();
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        initListener();
        setContentView(binding.getRoot());
    }

    private void isUserAlreadySignIn() {
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void initListener() {
        binding.signUpButton.setOnClickListener(this);
        binding.signInButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.signUpButton:
                if(isDataValid()) {
                    signUpWithEmailAndPassword();
                }
                break;
            case R.id.signInButton:
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                break;
        }
    }

    private boolean isDataValid() {
        if(binding.nameTxt.getText().toString().isEmpty()){
            binding.nameTxt.requestFocus();
            Toast.makeText(this, "Please provide name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(binding.passwordTxt.getText().toString().isEmpty()){
            binding.passwordTxt.requestFocus();
            Toast.makeText(this, "Please provide Password", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(binding.emailTxt.getText().toString().isEmpty()){
            binding.emailTxt.requestFocus();
            Toast.makeText(this, "Please provide email", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void signUpWithEmailAndPassword() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("We are creating your account");
        progressDialog.setCancelable(false);
        progressDialog.show();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                binding.emailTxt.getText().toString().trim(),
                binding.passwordTxt.getText().toString().trim()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    Map<String, Object> map = new HashMap<>();
                    map.put("name",binding.nameTxt.getText().toString().trim());
                    map.put("email",binding.emailTxt.getText().toString().trim());
                    map.put("password",binding.passwordTxt.getText().toString().trim());
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SignUpActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    Toast.makeText(SignUpActivity.this, "User SignUp Successfully", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SignUpActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}