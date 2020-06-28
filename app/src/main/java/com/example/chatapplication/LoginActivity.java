package com.example.chatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.mbms.MbmsErrors;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin,btnPhone;
    private EditText etEmail,etPassword;
    private TextView tvForget,tvNew;
    private FirebaseAuth mAuth;
    private ProgressDialog lodingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

        InitializeField();
        tvNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(intent);
            }
        });

        tvForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToForgotPasswordActivity();
            }
        });
    }
    private void AllowUserToLogin() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Enter Require Field", Toast.LENGTH_SHORT).show();
        }
        else {
            lodingBar.setTitle("Sign In");
            lodingBar.setMessage("Please wait....");
            lodingBar.setCanceledOnTouchOutside(true);
            lodingBar.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        lodingBar.dismiss();
                    }
                    else {
                        String msg= task.getException().toString();
                        Toast.makeText(LoginActivity.this,"Error: "+msg, Toast.LENGTH_SHORT).show();
                        lodingBar.dismiss();
                    }
                }
            });
        }
    }


    private void InitializeField() {
        btnLogin=(Button)findViewById(R.id.btnLogin);
        btnPhone=(Button)findViewById(R.id.btnPhone);
        etEmail=(EditText) findViewById(R.id.etEmail);
        etPassword=(EditText)findViewById(R.id.etPassword);
        tvForget=(TextView)findViewById(R.id.tvForget);
        tvNew=(TextView)findViewById(R.id.tvNew);
        lodingBar=new ProgressDialog(this);
    }

    private void sendUserToMainActivity() {
        Intent intent =new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void sendUserToRegisterActivity() {
        Intent intent =new Intent(LoginActivity.this,RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToForgotPasswordActivity() {
        Intent intent =new Intent(LoginActivity.this,ForgotPasswordActivity.class);
        startActivity(intent);
    }


}
