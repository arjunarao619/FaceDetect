package com.example.facedetect;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;


import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.app.AlertDialog;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.google.android.gms.vision.face.FaceDetector.ACCURATE_MODE;
import static com.google.android.gms.vision.face.FaceDetector.ALL_CLASSIFICATIONS;
import static com.google.android.gms.vision.face.FaceDetector.FAST_MODE;

public class DetectActivity extends AppCompatActivity {

    Camera camera;
    float xpos,ypos,width,height;
    EditText personid;
    String savedImagePath;


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
                        .setCompression(75)
                        .setImageHeight(1000)// it will try to achieve this height as close as possible maintaining the aspect ratio;
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
            Bitmap uploadImage = myBitmap; //test

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
            //TODO CALL POST FOR IMAGE TO BACKEND

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menudetect, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.send) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("POST Details");
            // set the custom layout
            final View customLayout = getLayoutInflater().inflate(R.layout.detailsdialog, null);
            builder.setView(customLayout);

            builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditText personname = customLayout.findViewById(R.id.personname);
                    //photoname = customLayout.findViewById(R.id.photoname);
                    //EditText photourl = customLayout.findViewById(R.id.photourl);
                    personid = customLayout.findViewById(R.id.personid);


                    JSONObject json = new JSONObject();
                    try{


                        json.put("person_name",personname.getText().toString());
                        json.put("person_id",personid.getText().toString());
                        json.put("photo_name",personid.getText().toString());
                        json.put("photo_url","http://192.168.7.115/api/v1/showface/image/" + personid.getText().toString());
                    }catch(JSONException e){
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

                    JSONObject face_contour = new JSONObject();
                    try{
                        face_contour.put("x",0.0);
                        face_contour.put("y",0.0);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                    JSONObject face_contour2 = new JSONObject();
                    try{
                        face_contour2.put("x",0.0);
                        face_contour2.put("y",0.0);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                    JSONArray faceContour = new JSONArray();
                    faceContour.put(face_contour);
                    faceContour.put(face_contour2);


                    /*JSONObject facecontour = new JSONObject();
                    try{
                    facecontour.put("face_contour",faceContour);}
                    catch (JSONException e){
                        e.printStackTrace();
                    }*/

                    JSONObject landmark_vector = new JSONObject();
                    try{


                        landmark_vector.put("face_contour",faceContour);
                        landmark_vector.put("outer_lips",new JSONArray());
                        landmark_vector.put("inner_lips",new JSONArray());
                        landmark_vector.put("left_eye",new JSONArray());
                        landmark_vector.put("right_eye",new JSONArray());
                        landmark_vector.put("left_pupil",new JSONArray());
                        landmark_vector.put("right_pupil",new JSONArray());
                        landmark_vector.put("left_eyebrow",new JSONArray());
                        landmark_vector.put("right_eyebrow",new JSONArray());
                        landmark_vector.put("nose",new JSONArray());
                        landmark_vector.put("nose_crest",new JSONArray());
                        landmark_vector.put("median_line",new JSONArray());

                        json.put("landmark_vector",landmark_vector);

                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                    sendPost(json);
                }
            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }


    public void sendPost(final JSONObject json) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlAddress = "http://192.168.7.115/api/v1/uploadface/profile/" + personid.getText().toString();
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
            }
        });

        thread.start();
    }

    //TODO POST MULTIPART IMAGE REQUEST

}
