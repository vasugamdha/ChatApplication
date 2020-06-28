package com.example.chatapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ImageViwer extends AppCompatActivity {

    private ImageView imageView;
    String imageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viwer);

        imageView=(ImageView)findViewById(R.id.imageViewer) ;
        imageUrl=getIntent().getStringExtra("url");

        Picasso.get().load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(imageUrl).into(imageView);
            }
        });
    }
}
