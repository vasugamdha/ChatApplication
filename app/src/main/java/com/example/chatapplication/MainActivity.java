    package com.example.chatapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

    public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private FirebaseUser user;
    private TabLayout myTabLayout;
    private TabAccessorAdapter myTabAccessorAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        rootRef= FirebaseDatabase.getInstance().getReference();

        mToolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatApplication");

        myViewPager=(ViewPager) findViewById(R.id.main_tabs_pager);
        myTabAccessorAdapter= new TabAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAccessorAdapter);

        myTabLayout=(TabLayout)findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }

        @Override
        protected void onStart() {
            super.onStart();
            if(user==null){
                sendUserToLoginActivity();
            }
            else {
                updateUserStatus("online");
                verifyUserExistance();
            }
        }

        @Override
        protected void onStop() {
            super.onStop();

            if (user!=null){
                updateUserStatus("offline");
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();

            if (user!=null){
                updateUserStatus("offline");
            }
        }

        private void verifyUserExistance() {

            String currentUserID=mAuth.getCurrentUser().getUid();
            rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!(dataSnapshot.child("name").exists())){
                        sendUserToSettingActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        @Override
        public boolean onCreateOptionsMenu(Menu menu) {

            getMenuInflater().inflate(R.menu.option_menu,menu);
            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            if(item.getItemId()==R.id.main_logout_option){
                user=null;
                updateUserStatus("offline");
                mAuth.signOut();
                sendUserToLoginActivity();
            }
            if (item.getItemId()==R.id.main_create_group_option){
                updateUserStatus("online");
                requestNewGroup();
            }
            if (item.getItemId()==R.id.main_setting_option){
                sendUserToSettingActivity();

            }
            if (item.getItemId()==R.id.main_find_friend_option){
                sendUserToFindFriendActivity();
            }
            return super.onOptionsItemSelected(item);
        }

        private void requestNewGroup() {
            AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
            builder.setTitle("Enter Group Name : ");

            final EditText groupNameField =new EditText(MainActivity.this);
            groupNameField.setHint("e.g. Friends Forever");

            builder.setView(groupNameField);

            builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String groupName=groupNameField.getText().toString();

                    if (TextUtils.isEmpty(groupName)){
                        Toast.makeText(MainActivity.this, "Please Write Group Name..", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        createNewGroup(groupName);
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }

        private void createNewGroup(final String groupName) {
            rootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(MainActivity.this, groupName+" is Created successfully..", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void sendUserToLoginActivity() {
            Intent intent =new Intent(MainActivity.this,LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        private void sendUserToSettingActivity() {
            Intent intent =new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        }

        private void sendUserToFindFriendActivity() {
            Intent intent =new Intent(MainActivity.this,FindFriendsActivity.class);
            startActivity(intent);
        }

        private void updateUserStatus(String state){
            String saveCurrentTime,saveCurrentDate;

            Calendar calendar=Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
            saveCurrentDate=currentDate.format(calendar.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            saveCurrentTime=currentTime.format(calendar.getTime());

            HashMap<String,Object> onlineStateMap = new HashMap<>();
            onlineStateMap.put("time",saveCurrentTime);
            onlineStateMap.put("date",saveCurrentDate);
            onlineStateMap.put("state",state);
                if (mAuth!=null) {
                    currentUserID = mAuth.getCurrentUser().getUid();
                    if (currentUserID!=null) {
                        rootRef.child("Users").child(currentUserID).child("userState")
                                .updateChildren(onlineStateMap);
                    }
                }


        }

    }
