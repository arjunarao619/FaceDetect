package com.example.facedetect;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;

import com.android.volley.Header;
import com.loopj.android.http.*;


import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;

import android.app.AlertDialog;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Debug;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import com.mindorks.paracamera.Camera;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


import static com.google.android.gms.vision.face.FaceDetector.ACCURATE_MODE;
import static com.google.android.gms.vision.face.FaceDetector.ALL_CLASSIFICATIONS;


public class DetectActivity extends AppCompatActivity {

    Camera camera;
    double xpos,ypos,width,height;
    EditText personid;
    String personurl_string;
    private static String TAG = "DetectActivity";
    String savedImagePath;
    Bitmap uploadImage;
    JSONObject json;
    String personid_string;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Build the camera
                camera = new Camera.Builder()
                        .resetToCorrectOrientation(true)// it will rotate the camera bitmap to the correct orientation from meta data
                        .setTakePhotoRequestCode(1)
                        .setDirectory("pics")
                        .setName("ali_" + System.currentTimeMillis())
                        .setImageFormat(Camera.IMAGE_JPEG)
                        .setCompression(0)
                       // .setImageHeight(1000)// it will try to achieve this height as close as possible maintaining the aspect ratio;
                        .build(DetectActivity.this);

                try {
                    camera.takePicture();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }); }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == Camera.REQUEST_TAKE_PHOTO){
            Bitmap myBitmap = camera.getCameraBitmap();
            uploadImage = myBitmap; //test


            if(myBitmap != null) {

                final ImageView imageView2 = (ImageView) findViewById(R.id.test);

                //imageView2.setImageBitmap(myBitmap);
                Paint myRectPaint = new Paint();
                myRectPaint.setStrokeWidth(5);
                myRectPaint.setColor(Color.RED);
                myRectPaint.setStyle(Paint.Style.STROKE);

                Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
                Canvas tempCanvas = new Canvas(tempBitmap);
                tempCanvas.drawBitmap(myBitmap, 0, 0, null);

                FaceDetector faceDetector = new
                        FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(true).setClassificationType(ALL_CLASSIFICATIONS)
                        .setMode(ACCURATE_MODE)
                        .build();
                if(!faceDetector.isOperational()){
                    return;
                }


                Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                SparseArray<Face> faces = faceDetector.detect(frame);

                for(int i=0; i<faces.size(); i++) {
                    Face thisFace = faces.valueAt(i);
                    float x1 = thisFace.getPosition().x;
                    float y1 = thisFace.getPosition().y;
                    float x2 = x1 + thisFace.getWidth();
                    float y2 = y1 + thisFace.getHeight();
                    float smile = thisFace.getIsSmilingProbability();
                    float lefteye = thisFace.getIsLeftEyeOpenProbability();
                    float righteye = thisFace.getIsRightEyeOpenProbability();


                    TextView textView = findViewById(R.id.details);
                    String details = "Smiling Probability: "+ smile + "\n   Right Eye Open Probability "+ righteye + "\n    Left Eye Open Probability: "+ lefteye;
                    textView.setText(details);

                    //for POST
                     xpos = x1;
                     ypos = y1;
                     width = x2;
                     height = y2;

                     tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);


                }
                imageView2.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
            //end of detection process


            }else{
                Toast.makeText(this.getApplicationContext(),"Picture not taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menudetect, menu);
       // menu.findItem(R.id.send).setEnabled(false);
        return super.onCreateOptionsMenu(menu);

    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.send) {

            if(xpos == 0.0 && ypos == 0.0 && width == 0.0 && height == 0.0){ //If no face is detected, dimensions are -1. Now displaying error message
                AlertDialog.Builder builder1 = new AlertDialog.Builder(DetectActivity.this);
                builder1.setTitle("No Face Detected!");
                builder1.setMessage("Please retake the picture with a face present");
                builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                        startActivity(getIntent());
                    }
                }).show();
            }
            else{

                Resources res = getResources();

                Drawable drawable = res.getDrawable(R.drawable.test);
                ImageView faceimage = findViewById(R.id.test);

                if(faceimage.getDrawable() == drawable){
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetectActivity.this);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).setTitle("Please Take a Picture before sending!").show();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("POST Details");

                final View customLayout = getLayoutInflater().inflate(R.layout.detailsdialog, null);
                builder.setView(customLayout);

                builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        EditText personname = customLayout.findViewById(R.id.personname);

                        personid_string = personname.getText().toString().replaceAll("\\s+","");

                        EditText personurl = customLayout.findViewById(R.id.photourl);
                        if(personurl.getText().toString().equals("")){
                            personurl_string = "http://192.168.7.115/api/v1/showface/image/" + personid_string + "/";
                        }
                        else personurl_string = personurl.getText().toString();

                        json = new JSONObject();
                        try {

                            json.put("person_name", personname.getText().toString());
                            json.put("person_id", personid_string);
                            json.put("photo_name", personid_string);
                            json.put("photo_url", personurl_string);

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        JSONObject rectangle_vector = new JSONObject();
                        try{
                            rectangle_vector.put("x",xpos);
                            rectangle_vector.put("y",ypos);
                            rectangle_vector.put("width",width);
                            rectangle_vector.put("height",height);
                            json.put("rectangle_vector",rectangle_vector);

                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                        //sendPost(json); //FUNCTION CALL 1
                        new SendJSON(DetectActivity.this).execute();

                        File f = new File(getApplicationContext().getCacheDir(), "file.jpeg");
                        try {
                            f.createNewFile();

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            uploadImage.compress(Bitmap.CompressFormat.JPEG, 90 , bos);
                            byte[] bitmapdata = bos.toByteArray();

                            FileOutputStream fos = new FileOutputStream(f);
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();

                            postImage(f, personid_string); //FUCNTION CALL 2

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.show();

            }


        }
        return super.onOptionsItemSelected(item);
    }

    private class SendJSON extends AsyncTask<Void,Void,Void>{
        private ProgressDialog progressDialog;

        SendJSON(DetectActivity activity){
            progressDialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Sending information to backend...");
            progressDialog.setTitle("Hold On!");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                String urlAddress = "http://192.168.7.115/api/v1/uploadface/profile/" + personid_string;
                URL url = new URL(urlAddress);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);




                Log.i("JSON", json.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(json.toString());

                os.flush();
                os.close();

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG" , conn.getResponseMessage());

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // do UI work here
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public void postImage(File f,String id){ //multipart request

        RequestParams params = new RequestParams();
        try {
            params.put("file", f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        client.post("http://192.168.7.115/api/v1/uploadface/image/" + id , params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                Log.i("DetectActivity","!!!Success!!!");

                AlertDialog.Builder builder = new AlertDialog.Builder(DetectActivity.this);
                builder.setTitle("Profile successfully sent!");
                builder.setMessage("Successfully sent to FaceME API! Thank you!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();

            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Log.i("DetectActivity","********Failure*********");

                AlertDialog.Builder builder = new AlertDialog.Builder(DetectActivity.this);
                builder.setTitle("Failure");
                builder.setMessage("Could not send your profile.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }

        });
    }





}
