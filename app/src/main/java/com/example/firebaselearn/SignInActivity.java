package com.example.firebaselearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.firebaselearn.databinding.ActivitySignInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    ActivitySignInBinding binding;
    private ProgressDialog progressDialog;
    public SignInActivity(){}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        initListener();
        setContentView(binding.getRoot());
    }

    private void initListener() {
        binding.signInButton.setOnClickListener(this);
        binding.signUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.signInButton:
                if(isDataValid()) {
                    doSignInWithEmailAndPassword();
                }
                break;
            case R.id.signUpButton:
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
                break;
        }
    }

    private boolean isDataValid() {
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

    private void doSignInWithEmailAndPassword() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Loging to your account");
        progressDialog.setCancelable(false);
        progressDialog.show();
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                binding.emailTxt.getText().toString().trim(),
                binding.passwordTxt.getText().toString().trim()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(SignInActivity.this, "User SignIn Successfully", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SignInActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}