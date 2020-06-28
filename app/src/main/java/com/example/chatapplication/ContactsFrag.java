package com.example.chatapplication;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFrag extends Fragment {

    private View contactView;
    private RecyclerView contactsRecyclerlist;
    private DatabaseReference contactsRef , userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public ContactsFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactView= inflater.inflate(R.layout.fragment_contacts, container, false);

        contactsRecyclerlist=(RecyclerView)contactView.findViewById(R.id.contactsRecyclerView);
        contactsRecyclerlist.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        return contactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {
                String usersIds =getRef(position).getKey();

                userRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();

                                if (state.equals("online")) {
                                    holder.online_status.setVisibility(View.VISIBLE);
                                } else if (state.equals("offline")) {
                                    holder.online_status.setVisibility(View.INVISIBLE);
                                } else {
                                    holder.online_status.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                holder.online_status.setVisibility(View.INVISIBLE);
                            }


                            if (dataSnapshot.hasChild("image")) {
                                final String profileImage = dataSnapshot.child("image").getValue().toString();
                                String userName = dataSnapshot.child("name").getValue().toString();
                                String userStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(userName);
                                holder.userStatus.setText(userStatus);
                                Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                            } else {
                                String userName = dataSnapshot.child("name").getValue().toString();
                                String userStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(userName);
                                holder.userStatus.setText(userStatus);
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
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout,viewGroup,false);
                return new ContactsViewHolder(view);
            }
        };

        contactsRecyclerlist.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage,online_status;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=(TextView)itemView.findViewById(R.id.user_name);
            userStatus=(TextView)itemView.findViewById(R.id.user_status);
            profileImage=(CircleImageView)itemView.findViewById(R.id.user_profile_image);
            online_status=(CircleImageView)itemView.findViewById(R.id.online);
        }
    }
}
