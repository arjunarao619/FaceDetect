package com.example.facedetect;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

public class LoadImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_image);

        Intent intent = getIntent();
        String message = intent.getStringExtra("ID");




        final ImageView imageView = findViewById(R.id.load);
        String url_image ="http://192.168.7.115/api/v1/showface/image/" + message;
        String url_text ="http://192.168.7.115/api/v1/showface/profile/" + message;



        //Load the image
        Glide.with(LoadImageActivity.this).load(url_image).into(imageView);

    }
}
