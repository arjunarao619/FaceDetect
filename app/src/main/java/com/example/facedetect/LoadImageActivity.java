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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import org.json.JSONException;
import org.json.JSONObject;

public class LoadImageActivity extends AppCompatActivity {
    TextView textView;
    String message;
    String url_text;
    private RequestQueue requestQueue;
    private VolleySingleton volleySingleton;
    ProgressDialog progressDialog;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_image);

        Intent intent = getIntent();
        message = intent.getStringExtra("ID");


        imageView = findViewById(R.id.load);
        textView = findViewById(R.id.faceid);
        String url_image = "http://192.168.7.115/api/v1/showface/image/" + message;
        url_text = "http://192.168.7.115/api/v1/showface/profile/" + message;

       // new GetImageTask().execute();


        Glide.with(LoadImageActivity.this).load(url_image).into(imageView);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        getData();
    }

    public void getData(){
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        try {
            String url = url_text;
            JSONObject object = new JSONObject();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        textView.setText("Face ID: " + response.getString("person_name"));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        progressDialog.dismiss();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        progressDialog.cancel();
    }

}



