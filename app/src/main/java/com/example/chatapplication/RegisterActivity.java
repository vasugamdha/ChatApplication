package com.example.chatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private Button btnCreate;
    private EditText etEmailReg,etPasswordReg;
    private TextView tvAlready;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private ProgressDialog lodingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        InitializeField();
        mAuth=FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();

        tvAlready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = etEmailReg.getText().toString();
        String password = etPasswordReg.getText().toString();
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Enter Require Field", Toast.LENGTH_SHORT).show();
        }
        else {
            lodingBar.setTitle("Create New Account");
            lodingBar.setMessage("Please wait, until it complete successfully");
            lodingBar.setCanceledOnTouchOutside(true);
            lodingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        String currentUserID=mAuth.getCurrentUser().getUid();
                        rootRef.child("Users").child(currentUserID).setValue("");
                        sendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                        lodingBar.dismiss();
                    }
                    else {
                        String msg= task.getException().toString();
                        Toast.makeText(RegisterActivity.this,"Error: "+msg, Toast.LENGTH_SHORT).show();
                        lodingBar.dismiss();
                    }
                }
            });
        }
    }

    private void InitializeField() {
        btnCreate=(Button)findViewById(R.id.btnCreate);
        etEmailReg=(EditText) findViewById(R.id.etEmailReg);
        etPasswordReg=(EditText)findViewById(R.id.etPasswordReg);
        tvAlready=(TextView)findViewById(R.id.tvAlready);
        lodingBar=new ProgressDialog(this);
    }

    private void sendUserToLoginActivity() {
        Intent intent =new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    private void sendUserToMainActivity() {
        Intent intent =new Intent(RegisterActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
