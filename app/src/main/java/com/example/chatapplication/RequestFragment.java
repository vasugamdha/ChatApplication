package com.example.chatapplication;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference chatRequestRef,userRef,contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    static ProgressDialog loadingbar;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView= inflater.inflate(R.layout.fragment_request, container, false);

        chatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

        myRequestList=(RecyclerView)requestFragmentView.findViewById(R.id.chat_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestFragmentView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestRef.child(currentUserID),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, final int position, @NonNull Contacts model) {

                    final String list_user_id = getRef(position).getKey();

                    DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                    getTypeRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                String type = dataSnapshot.getValue().toString();

                                if (type.equals("received")){
                                    holder.acceptButton.setText("Accept");
                                    holder.declineButton.setText("decline");
                                    holder.acceptButton.setEnabled(true);
                                    holder.itemView.findViewById(R.id.accept_button).setVisibility(View.VISIBLE);
                                    holder.itemView.findViewById(R.id.decline_button).setVisibility(View.VISIBLE);
                                    userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild("image")){
                                                final String RprofileImage=dataSnapshot.child("image").getValue().toString();
                                                Picasso.get().load(RprofileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                            }

                                            final String RuserName=dataSnapshot.child("name").getValue().toString();
                                            final String RuserStatus=dataSnapshot.child("status").getValue().toString();

                                            holder.userName.setText(RuserName);
                                            holder.userStatus.setText(RuserStatus);

                                            holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    contactsRef.child(currentUserID).child(list_user_id).child("Contacts")
                                                            .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                contactsRef.child(list_user_id).child(currentUserID).child("Contacts")
                                                                        .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            chatRequestRef.child(currentUserID).child(list_user_id)
                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        chatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), holder.userName.getText()+" Added to your contact successfully", Toast.LENGTH_SHORT).show();
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
                                            });

                                            holder.declineButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    chatRequestRef.child(currentUserID).child(list_user_id)
                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                chatRequestRef.child(list_user_id).child(currentUserID)
                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            Toast.makeText(getContext(), holder.userName.getText()+" deleted from your contact", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            });

                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    CharSequence options[]=new CharSequence[]{
                                                      "Accept",
                                                      "Decline"
                                                    };

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle(RuserName+" Chat Request");

                                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            if (which==0){
                                                                contactsRef.child(currentUserID).child(list_user_id).child("Contacts")
                                                                        .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            contactsRef.child(list_user_id).child(currentUserID).child("Contacts")
                                                                                    .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        chatRequestRef.child(currentUserID).child(list_user_id)
                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    chatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()){
                                                                                                                Toast.makeText(getContext(), holder.userName.getText()+" Added to your contact successfully", Toast.LENGTH_SHORT).show();
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
                                                            else if (which==1){
                                                                chatRequestRef.child(currentUserID).child(list_user_id)
                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            chatRequestRef.child(list_user_id).child(currentUserID)
                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        Toast.makeText(getContext(), holder.userName.getText()+" deleted from your contact", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });

                                                    builder.show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                                else{
                                    holder.acceptButton.setText("Request Sent");
                                    holder.declineButton.setText("delete");
                                    holder.itemView.findViewById(R.id.accept_button).setVisibility(View.VISIBLE);
                                    holder.acceptButton.setEnabled(false);
                                    holder.itemView.findViewById(R.id.decline_button).setVisibility(View.VISIBLE);
                                    userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild("image")){
                                                final String RprofileImage=dataSnapshot.child("image").getValue().toString();
                                                Picasso.get().load(RprofileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                            }

                                            final String RuserName=dataSnapshot.child("name").getValue().toString();
                                            final String RuserStatus=dataSnapshot.child("status").getValue().toString();

                                            holder.userName.setText(RuserName);
                                            holder.userStatus.setText(RuserStatus);

                                            holder.declineButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    chatRequestRef.child(currentUserID).child(list_user_id)
                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                chatRequestRef.child(list_user_id).child(currentUserID)
                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            Toast.makeText(getContext(), "request of " + holder.userName.getText()+" cancelled", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout,viewGroup,false);
                RequestViewHolder viewHolder = new RequestViewHolder(view);
                return  viewHolder;

            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;
        Button acceptButton,declineButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=(TextView)itemView.findViewById(R.id.user_name);
            userStatus=(TextView)itemView.findViewById(R.id.user_status);
            profileImage=(CircleImageView) itemView.findViewById(R.id.user_profile_image);
            acceptButton=(Button)itemView.findViewById(R.id.accept_button);
            declineButton=(Button)itemView.findViewById(R.id.decline_button);
        }
    }



}
