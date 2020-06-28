package com.example.chatapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;


    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText,senderDate,receiverDate;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPic,messageRecevierPic;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageSenderPic=(ImageView)itemView.findViewById(R.id.message_sender_image_view);
            messageRecevierPic=(ImageView)itemView.findViewById(R.id.message_receiver_image_view);
            senderDate=(TextView)itemView.findViewById(R.id.sender_date);
            receiverDate=(TextView)itemView.findViewById(R.id.receiver_date);
        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_message_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();

        final Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    final String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPic.setVisibility(View.GONE);
        messageViewHolder.messageRecevierPic.setVisibility(View.GONE);
        messageViewHolder.senderDate.setVisibility(View.GONE);
        messageViewHolder.receiverDate.setVisibility(View.GONE);



        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setText(Html.fromHtml(messages.getMessage()+"<br /><br />"+ "<font color='#006257' size='2'><small>" + messages.getDate()+" - "+messages.getTime() + "</small></font>"));
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setText(Html.fromHtml(messages.getMessage()+"<br /><br />"+ "<font color='#006257' size='2'><small>" + messages.getDate()+" - "+messages.getTime() + "</small></font>"));
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
            }
        }
        else if (fromMessageType.equals("image")){
            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.messageSenderPic.setVisibility(View.VISIBLE);
                messageViewHolder.senderDate.setVisibility(View.VISIBLE);
                messageViewHolder.senderDate.setText(Html.fromHtml("<font color='#006257' size='2'><small>" + messages.getDate()+" - "+messages.getTime() + "</small></font>"));
                Picasso.get().load(messages.getMessage()).networkPolicy(NetworkPolicy.OFFLINE).into(messageViewHolder.messageSenderPic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPic);
                    }
                });

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViwer.class);
                        intent.putExtra("url",userMessagesList.get(position).getMessage());
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageRecevierPic.setVisibility(View.VISIBLE);
                messageViewHolder.receiverDate.setVisibility(View.VISIBLE);
                messageViewHolder.receiverDate.setText(Html.fromHtml("<font color='#006257' size='2'><small>" + messages.getDate()+" - "+messages.getTime() + "</small></font>"));
                Picasso.get().load(messages.getMessage()).networkPolicy(NetworkPolicy.OFFLINE).into(messageViewHolder.messageRecevierPic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageRecevierPic);
                    }
                });
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViwer.class);
                        intent.putExtra("url",userMessagesList.get(position).getMessage());
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }
        }
        else if (fromMessageType.equals("video")){
            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.messageSenderPic.setVisibility(View.VISIBLE);
                messageViewHolder.senderDate.setVisibility(View.VISIBLE);
                messageViewHolder.senderDate.setText(Html.fromHtml("<font color='#006257' size='2'><small>" + messages.getDate()+" - "+messages.getTime() + "</small></font>"));
                messageViewHolder.messageSenderPic.setImageResource(R.drawable.video);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(messageViewHolder.itemView.getContext(),VideoViwer.class);
                        intent.putExtra("url",userMessagesList.get(position).getMessage());
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageRecevierPic.setVisibility(View.VISIBLE);
                messageViewHolder.receiverDate.setVisibility(View.VISIBLE);
                messageViewHolder.receiverDate.setText(Html.fromHtml("<font color='#006257' size='2'><small>" + messages.getDate()+" - "+messages.getTime() + "</small></font>"));
                messageViewHolder.messageRecevierPic.setImageResource(R.drawable.video);
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(messageViewHolder.itemView.getContext(),VideoViwer.class);
                        intent.putExtra("url",userMessagesList.get(position).getMessage());
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }
        }
        else if (fromMessageType.equals("pdf") || fromMessageType.equals("dcx")){
            if (fromUserID.equals(messageSenderId)){
                messageViewHolder.messageSenderPic.setVisibility(View.VISIBLE);
                messageViewHolder.messageSenderPic.setImageResource(R.drawable.file);
                messageViewHolder.senderDate.setVisibility(View.VISIBLE);
                messageViewHolder.senderDate.setText(Html.fromHtml("<font color='#006257' size='2'><small>" + messages.getDate()+" - "+messages.getTime() + "</small></font>"));

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }
            else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageRecevierPic.setVisibility(View.VISIBLE);
                messageViewHolder.messageRecevierPic.setImageResource(R.drawable.file);
                messageViewHolder.receiverDate.setVisibility(View.VISIBLE);
                messageViewHolder.receiverDate.setText(Html.fromHtml("<font color='#006257' size='2'><small>" + messages.getDate()+" - "+messages.getTime() + "</small></font>"));

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }


        if (fromUserID.equals(messageSenderId)){
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (userMessagesList.get(position).getType().equals("pdf")  ||  userMessagesList.get(position).getType().equals("docx")){
                        CharSequence option[] = new CharSequence[]{
                                "Cancel",
                                "Download And View",
                                "Delete For Me",
                                "Delete For Everyone"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which==0){

                                        }
                                        else if (which==1){
                                            Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                            messageViewHolder.itemView.getContext().startActivity(intent);
                                        }
                                        else if (which==2){
                                            deleteSentMessages(position,messageViewHolder);
                                        }
                                        else if (which==3){
                                            deleteEveryoneMessages(position,messageViewHolder);
                                        }
                                    }
                                });
                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence option[] = new CharSequence[]{
                                "Cancel",
                                "Delete For Me",
                                "Delete For Everyone"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which==0){

                                        }
                                        else if (which==1){
                                            deleteSentMessages(position,messageViewHolder);
                                        }
                                        else if (which==2){
                                            deleteEveryoneMessages(position,messageViewHolder);


                                        }
                                    }
                                });
                        builder.show();
                    }


                    else if (userMessagesList.get(position).getType().equals("image") ){
                        CharSequence option[] = new CharSequence[]{
                                "Cancel",
                                "View This Image",
                                "Delete For Me",
                                "Delete For Everyone"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which==0){

                                        }
                                        else if (which==1){
                                            Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViwer.class);
                                            intent.putExtra("url",userMessagesList.get(position).getMessage());
                                            messageViewHolder.itemView.getContext().startActivity(intent);
                                        }
                                        else if (which==2){
                                            deleteSentMessages(position,messageViewHolder);
                                        }
                                        else if (which==3){
                                            deleteEveryoneMessages(position,messageViewHolder);
                                        }
                                    }
                                });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("video") ){
                        CharSequence option[] = new CharSequence[]{
                                "Cancel",
                                "View This Video",
                                "Delete For Me",
                                "Delete For Everyone",
                                "View And Download"

                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which==0){

                                        }
                                        else if (which==1){
                                            Intent intent = new Intent(messageViewHolder.itemView.getContext(),VideoViwer.class);
                                            intent.putExtra("url",userMessagesList.get(position).getMessage());
                                            messageViewHolder.itemView.getContext().startActivity(intent);
                                        }
                                        else if (which==2){
                                            deleteSentMessages(position,messageViewHolder);
                                        }
                                        else if (which==3){
                                            deleteEveryoneMessages(position,messageViewHolder);
                                        }
                                        else if (which==4){
                                            Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                            messageViewHolder.itemView.getContext().startActivity(intent);
                                        }
                                    }
                                });
                        builder.show();
                    }


                    return false;
                }
            });
        }

        else {
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (userMessagesList.get(position).getType().equals("pdf")  ||  userMessagesList.get(position).getType().equals("docx")){
                        CharSequence option[] = new CharSequence[]{
                                "Cancel",
                                "Download And View",
                                "Delete For Me"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which==0){

                                        }
                                        else if (which==1){
                                            Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                            messageViewHolder.itemView.getContext().startActivity(intent);
                                        }
                                        else if (which==2){
                                            deleteReceiveMessages(position,messageViewHolder);
                                        }
                                    }
                                });
                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence option[] = new CharSequence[]{
                                "Cancel",
                                "Delete For Me"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which==0){

                                        }
                                        else if (which==1){
                                            deleteReceiveMessages(position,messageViewHolder);
                                        }
                                    }
                                });
                        builder.show();
                    }


                    else if (userMessagesList.get(position).getType().equals("image") ){
                        CharSequence option[] = new CharSequence[]{
                                "Cancel",
                                "View This Image",
                                "Delete For Me"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which==0){

                                        }
                                        else if (which==1){
                                            Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViwer.class);
                                            intent.putExtra("url",userMessagesList.get(position).getMessage());
                                            messageViewHolder.itemView.getContext().startActivity(intent);
                                        }
                                        else if (which==2){
                                            deleteReceiveMessages(position,messageViewHolder);
                                        }
                                    }
                                });
                        builder.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("video") ){
                        CharSequence option[] = new CharSequence[]{
                                "Cancel",
                                "View This Image",
                                "Delete For Me",
                                "View And Download"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which==0){

                                        }
                                        else if (which==1){
                                            Intent intent = new Intent(messageViewHolder.itemView.getContext(),VideoViwer.class);
                                            intent.putExtra("url",userMessagesList.get(position).getMessage());
                                            messageViewHolder.itemView.getContext().startActivity(intent);
                                        }
                                        else if (which==2){
                                            deleteReceiveMessages(position,messageViewHolder);
                                        }
                                        else if (which==3){
                                            Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                            messageViewHolder.itemView.getContext().startActivity(intent);
                                        }
                                    }
                                });
                        builder.show();
                    }


                    return false;
                }
            });
        }
    }




    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }


    private void deleteSentMessages(final int position , final MessageViewHolder holder){
        DatabaseReference rootRef =FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(holder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceiveMessages(final int position , final MessageViewHolder holder){
        DatabaseReference rootRef =FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(holder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteEveryoneMessages(final int position , final MessageViewHolder holder){
        final DatabaseReference rootRef =FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootRef.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Toast.makeText(holder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}