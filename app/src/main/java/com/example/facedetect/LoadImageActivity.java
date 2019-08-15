package com.example.facedetect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import android.app.ProgressDialog;

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
                    Toast.makeText(getApplicationContext(), "Unknown Error", Toast.LENGTH_LONG).show();
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}



