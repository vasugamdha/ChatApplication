package com.example.chatapplication;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserID, current_state, senderUserId;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button sendMessageRequestButton,declineRequestButton;
    private DatabaseReference userRef,requestRef,contactRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        loadingBar=new ProgressDialog(this);


        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();

        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        recieverUserID=getIntent().getExtras().get("visit_user_id").toString();

        userProfileImage=(CircleImageView)findViewById(R.id.user_profile_image);
        userProfileName=(TextView) findViewById(R.id.user_profile_name);
        userProfileStatus=(TextView) findViewById(R.id.user_profile_status);
        sendMessageRequestButton=(Button)findViewById(R.id.sens_message_touser_button);
        declineRequestButton=(Button)findViewById(R.id.decline_request_button);

        current_state="new";

        retriveUserInformation();

    }

    private void retriveUserInformation() {
        userRef.child(recieverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();
                }
                else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {

        requestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(recieverUserID)){
                    String request_type =dataSnapshot.child(recieverUserID).child("request_type").getValue().toString();

                    if (request_type.equals("sent")){
                        current_state="request_sent";
                        sendMessageRequestButton.setText("Cancel chat request");
                    }
                    else if (request_type.equals("received")){
                        current_state="request_received";
                        sendMessageRequestButton.setText("Accept Request");
                        declineRequestButton.setVisibility(View.VISIBLE);

                        declineRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });
                    }
                }
                else {
                    contactRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(recieverUserID)){
                                current_state="friends";
                                sendMessageRequestButton.setText("Remove from friend");
                                declineRequestButton.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!senderUserId.equals(recieverUserID)){

            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadingBar.setTitle("Working On Request..");
                    loadingBar.setMessage("Please wait..");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    if (current_state.equals("new")){
                        sendChatRequest();
                        loadingBar.dismiss();
                    }
                    else if (current_state.equals("request_sent")){
                        cancelChatRequest();
                        loadingBar.dismiss();
                    }
                    else if (current_state.equals("request_received")){
                        acceptChatRequest();
                        loadingBar.dismiss();
                    }
                    else if (current_state.equals("friends")){
                        removeSpecificContact();
                        loadingBar.dismiss();
                    }
                }
            });
        }
        else {
            sendMessageRequestButton.setVisibility(View.GONE);
        }
    }

    private void removeSpecificContact() {
        contactRef.child(senderUserId).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    contactRef.child(recieverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                current_state="new";
                                sendMessageRequestButton.setText("Send message");
                                declineRequestButton.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptChatRequest() {
        contactRef.child(senderUserId).child(recieverUserID).child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactRef.child(recieverUserID).child(senderUserId).child("Contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                requestRef.child(senderUserId).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            requestRef.child(recieverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        current_state = "friends";
                                                                        sendMessageRequestButton.setText("Remove From Friend");
                                                                        declineRequestButton.setVisibility(View.GONE);
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest() {

        requestRef.child(senderUserId).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    requestRef.child(recieverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                current_state="new";
                                sendMessageRequestButton.setText("Send message");
                                declineRequestButton.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {
        requestRef.child(senderUserId).child(recieverUserID).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    requestRef.child(recieverUserID).child(senderUserId).child("request_type")
                            .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                current_state="request_sent";
                                sendMessageRequestButton.setText("Cancel chat request");
                            }
                        }
                    });
                }
            }
        });
    }
}
