package com.example.chatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateButton;
    private EditText etUser,etStatus;
    private CircleImageView userProfile;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    private Toolbar mySettingtoolbar;

    private static final int gallerypick=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        loadingBar=new ProgressDialog(this);


        initialization();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateAccountSetting();
            }
        });
        
        retrieveUserInformation();

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent =new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallerypick);
            }
        });
    }

    private void retrieveUserInformation() {
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))  && (dataSnapshot.hasChild("image"))){
                        String name=dataSnapshot.child("name").getValue().toString();
                        String status=dataSnapshot.child("status").getValue().toString();
                        String image=dataSnapshot.child("image").getValue().toString();

                        etUser.setText(name);
                        etStatus.setText(status);

                        Picasso.get().load(image).into(userProfile);
                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                    String name=dataSnapshot.child("name").getValue().toString();
                    String status=dataSnapshot.child("status").getValue().toString();

                    etUser.setText(name);
                    etStatus.setText(status);
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Please Update Your Information First", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void UpdateAccountSetting() {
        String user=etUser.getText().toString();
        String status=etStatus.getText().toString();

        if (user.isEmpty() || status.isEmpty()){
            Toast.makeText(this, "Please enter require fields", Toast.LENGTH_SHORT).show();
        }

        else {
            HashMap<String,String> profileMap = new HashMap<>();
            profileMap.put("UID",currentUserID);
            profileMap.put("name",user);
            profileMap.put("status",status);

            rootRef.child("Users").child(currentUserID).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String error =task.getException().toString();
                        Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
    }

    private void initialization() {
        updateButton=(Button)findViewById(R.id.update_setting_button);
        etUser=(EditText)findViewById(R.id.set_user_name);
        etStatus=(EditText)findViewById(R.id.set_profile_status);
        userProfile=(CircleImageView)findViewById(R.id.set_profile_image);

        mySettingtoolbar = (Toolbar)findViewById(R.id.setting_toolbar);
        setSupportActionBar(mySettingtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==gallerypick  && resultCode==RESULT_OK  &&  data!=null){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK){
                Uri resultUri= result.getUri();

                final StorageReference filepath = UserProfileImageRef.child(currentUserID+".jpg");
                loadingBar.setTitle("Image Uploading..");
                loadingBar.setMessage("Please wait..");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            loadingBar.dismiss();
                            final String downloadUrl =task.getResult().getDownloadUrl().toString();

                            rootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(SettingsActivity.this, "Image save in database successfully... ", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        String msg= task.getException().toString();
                                        Toast.makeText(SettingsActivity.this,"Error: "+msg, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }
                        else {
                            loadingBar.dismiss();
                            String msg= task.getException().toString();
                            Toast.makeText(SettingsActivity.this,"Error: "+msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

    }

    private void sendUserToMainActivity() {
        Intent intent =new Intent(SettingsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
